# StockTradingEngine
Stock trading engine simulation for Onymos

This code implements a real-time stock trading engine in Java to match buy and sell orders, designed for the Onymos interview. It simulates active stock transactions using a multi-threaded approach with lock-free data structures, adhering to the specified constraints.

## Overview
The `StockTradingEngine` class provides a lightweight, thread-safe solution for adding and matching stock orders without relying on dictionaries, maps, or external data structure libraries. It uses a fixed-size array as the order book and atomic operations to manage concurrency, meeting the requirement to handle race conditions.

## Features
1. **`addOrder` Function**:
   - **Parameters**: 
     - `Order Type` (Buy or Sell)
     - `Ticker Symbol` (0 to 4 in this implementation; scalable to 1,024)
     - `Quantity` (1 to 100 shares)
     - `Price` (0.0 to 99.9)
   - **Implementation**: Adds orders to a fixed array (`orderBook`) of size 10,000, supporting up to 1,024 tickers (though limited to 5 here for simplicity). Uses an `AtomicInteger` for thread-safe order indexing.
   - **Simulation**: Wrapped in `TradingSimulator`, which generates random orders across 3 threads to mimic real-time stockbroker activity.

2. **`matchOrder` Function**:
   - **Matching Criteria**: Matches a buy order with the lowest sell price for the same ticker if the buy price is greater than or equal to the sell price.
   - **Concurrency**: Uses `AtomicBoolean` per order to prevent race conditions without locks, ensuring thread safety as multiple threads (simulating stockbrokers) access the order book.
   - **Time Complexity**: O(n), where `n` is the number of orders, achieved by a single linear scan of the order book to find the best buy and sell prices.
   - **No Maps/Dictionaries**: Relies solely on an array (`orderBook`) and basic variables, avoiding any imported data structures beyond standard Java utilities (`Random`, `Thread`, `Atomic*`).

## Output Meaning
The program outputs trading activity in a natural, timestamped format:
- **`[X ms] Buy/Sell order placed: QTY shares of Ticker ID @ PRICE`**:
  - Indicates a new order added to the book with a random ticker (0-4), quantity (1-100), and price (0.0-99.9).
- **`[X ms] Trade executed: QTY shares of Ticker ID at BUY_PRICE (Buy) / SELL_PRICE (Sell)`**:
  - Shows a successful match where a buy order’s price ≥ the lowest sell price for the same ticker, with the traded quantity being the minimum of the two orders.
- **`[X ms] No match for Ticker ID: Best Buy/Sell price PRICE`**:
  - Indicates no matching sell (or buy) order exists for the ticker, showing the best available price on one side of the book.

The timestamp (`[X ms]`) reflects milliseconds since the program started, simulating real-time execution. Multiple threads (T1, T2, T3) run concurrently, producing interleaved output that demonstrates thread-safe order processing.

## How to Run
- **Compile**: `javac StockTradingEngine.java`
- **Execute**: `java StockTradingEngine`
- **Output**: Logs appear in the console; redirect to a file (e.g., `java StockTradingEngine > output.txt`) for longer runs, as it continues until interrupted.

## Notes
- **Ticker Limit**: Set to 5 (`MAX_TICKERS = 5`) for demonstration; easily scalable to 1,024 by adjusting the constant.
- **Order Book Size**: Fixed at 10,000 (`MAX_ORDERS`), sufficient for the simulation but adjustable.
- **Concurrency**: Uses atomic operations instead of locks, meeting the lock-free requirement while maintaining O(n) matching complexity.

This implementation balances simplicity, performance, and adherence to the problem constraints, showcasing a practical stock trading engine.
