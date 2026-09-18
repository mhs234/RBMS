# RBMS — Restaurant Booking Management System

A functional command-line Java application for managing restaurant table bookings. Booking data is saved to `bookings.csv` and loaded the next time the application starts.

## Features

- Add bookings with customer, date, time, guest and table details
- Prevent duplicate booking IDs and table/time conflicts
- View and delete bookings
- Search by booking ID or customer name
- Sort by booking ID or date/time
- Save and reload bookings from disk
- Validate all user input

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Run

```bash
mvn clean compile exec:java
```

Choose an option from the displayed menu. Select **7** to save all bookings and exit.

## Test

```bash
mvn test
```

GitHub Actions also compiles and tests every push and pull request.

## Data

Runtime data is stored locally in `bookings.csv`. This file is ignored by Git so real customer information is not committed.
