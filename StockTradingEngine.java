import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class StockTradingEngine 
{
    private static final int MAX_TICKERS = 100;
    private static final int MAX_ORDERS = 10000;
    private static final long START_TIME = System.currentTimeMillis();

    private static final Order[] orderBook = new Order[MAX_ORDERS];
    private static final AtomicInteger orderCount = new AtomicInteger(0);
    private static final int[] tickerMap = new int[MAX_TICKERS];

    static class Order 
    {
        boolean isBuy;
        int tickerId;
        int quantity;
        float price;
        boolean isActive;
        AtomicBoolean inProcess;

        Order(boolean isBuy, int tickerId, int quantity, float price) 
        {
            this.isBuy = isBuy;
            this.tickerId = tickerId;
            this.quantity = quantity;
            this.price = price;
            this.isActive = true;
            this.inProcess = new AtomicBoolean(false);
        }
    }

    public static int addOrder(boolean orderType, int tickerSymbol, int quantity, float price) 
    {
        int newIndex = orderCount.getAndIncrement();
        if (newIndex >= MAX_ORDERS) 
        {
            orderCount.getAndDecrement();
            System.out.println(getTimestamp() + " Order book capacity reached: " + newIndex);
            return -1;
        }
        orderBook[newIndex] = new Order(orderType, tickerSymbol, quantity, price);
        System.out.printf("%s %s order placed: %d shares of Ticker %d @ %.2f%n",
                         getTimestamp(), orderType ? "Buy" : "Sell", quantity, tickerSymbol, price);
        return newIndex;
    }

    public static void matchOrder(int tickerId) 
    {
        int bestSellIdx = -1;
        int bestBuyIdx = -1;
        float bestSellPrice = Float.MAX_VALUE;
        float bestBuyPrice = 0;

        int currentCount = orderCount.get();
        for (int i = 0; i < currentCount; i++) 
        {
            Order order = orderBook[i];
            if (order == null || !order.isActive || order.tickerId != tickerId) continue;
            if (!order.inProcess.compareAndSet(false, true)) continue;

            if (order.isBuy) 
            {
                if (order.price > bestBuyPrice) 
                {
                    bestBuyPrice = order.price;
                    bestBuyIdx = i;
                }
            } 
            else 
            {
                if (order.price < bestSellPrice) 
                {
                    bestSellPrice = order.price;
                    bestSellIdx = i;
                }
            }
        }

        if (bestBuyIdx != -1 && bestSellIdx != -1 && bestBuyPrice >= bestSellPrice) 
        {
            Order buyOrder = orderBook[bestBuyIdx];
            Order sellOrder = orderBook[bestSellIdx];
            int tradeQty = Math.min(buyOrder.quantity, sellOrder.quantity);
            buyOrder.quantity -= tradeQty;
            sellOrder.quantity -= tradeQty;

            System.out.printf("%s Trade executed: %d shares of Ticker %d at %.2f (Buy) / %.2f (Sell)%n",
                             getTimestamp(), tradeQty, tickerId, bestBuyPrice, bestSellPrice);

            if (buyOrder.quantity == 0) buyOrder.isActive = false;
            if (sellOrder.quantity == 0) sellOrder.isActive = false;
            buyOrder.inProcess.set(false);
            sellOrder.inProcess.set(false);
        } 
        else 
        {
            if (bestBuyIdx != -1 && bestSellIdx == -1) 
            {
                System.out.printf("%s No match for Ticker %d: Best Buy price %.2f%n",
                                 getTimestamp(), tickerId, bestBuyPrice);
            } 
            else if (bestBuyIdx == -1 && bestSellIdx != -1) 
            {
                System.out.printf("%s No match for Ticker %d: Best Sell price %.2f%n",
                                 getTimestamp(), tickerId, bestSellPrice);
            }

            if (bestBuyIdx != -1) orderBook[bestBuyIdx].inProcess.set(false);
            if (bestSellIdx != -1) orderBook[bestSellIdx].inProcess.set(false);
        }
    }

    static class TradingSimulator implements Runnable 
    {
        private final Random random = new Random();

        @Override
        public void run() 
        {
            try 
            {
                while (!Thread.currentThread().isInterrupted()) 
                {
                    boolean orderType = random.nextBoolean();
                    int ticker = random.nextInt(MAX_TICKERS);
                    int qty = random.nextInt(100) + 1;
                    float price = random.nextInt(1000) / 10.0f;

                    int orderId = addOrder(orderType, ticker, qty, price);
                    if (orderId != -1) 
                    {
                        matchOrder(ticker);
                    }
                    Thread.sleep(10);
                }
                System.out.println(getTimestamp() + " " + Thread.currentThread().getName() + " stopped due to interrupt");
            } 
            catch (Exception e) 
            {
                System.out.println(getTimestamp() + " Simulator error in " + Thread.currentThread().getName() + ": " + e.getMessage());
                e.printStackTrace();
            } 
            finally 
            {
                System.out.println(getTimestamp() + " " + Thread.currentThread().getName() + " exiting. Order count: " + orderCount.get());
            }
        }
    }

    private static String getTimestamp() 
    {
        long currentTime = System.currentTimeMillis() - START_TIME;
        return String.format("[%d ms]", currentTime);
    }

    public static void main(String[] args) 
    {
        for (int i = 0; i < MAX_TICKERS; i++) 
        {
            tickerMap[i] = 0;
        }

        Thread t1 = new Thread(new TradingSimulator(), "T1");
        Thread t2 = new Thread(new TradingSimulator(), "T2");
        Thread t3 = new Thread(new TradingSimulator(), "T3");

        t1.start();
        t2.start();
        t3.start();

        try 
        {
            t1.join();
            t2.join();
            t3.join();
        } 
        catch (InterruptedException e) 
        {
            System.out.println(getTimestamp() + " Main interrupted. Order count: " + orderCount.get());
            e.printStackTrace();
        } 
        finally 
        {
            System.out.println(getTimestamp() + " Main exiting. Final order count: " + orderCount.get());
        }
    }
}