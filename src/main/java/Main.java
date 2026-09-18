import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    private static final String DATA_FILE = "bookings.csv";

    public static void main(String[] args) {
        BookingManager manager = new BookingManager();
        try {
            manager.loadFromFile(DATA_FILE);
        } catch (IOException e) {
            System.out.println("Note: existing booking data could not be loaded.");
        }

        // JDoodle runs in a console/headless environment, so start the text interface.
        TextBasedInterface app = new TextBasedInterface(manager, DATA_FILE);
        app.start();
    }
}

class Booking {
    private final int bookingId;
    private final String customerName;
    private final String phoneNumber;
    private final LocalDate bookingDate;
    private final LocalTime bookingTime;
    private final int numberOfGuests;
    private final int tableNumber;

    public Booking(int bookingId, String customerName, String phoneNumber,
                   LocalDate bookingDate, LocalTime bookingTime,
                   int numberOfGuests, int tableNumber) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.numberOfGuests = numberOfGuests;
        this.tableNumber = tableNumber;
    }

    public int getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalTime getBookingTime() { return bookingTime; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public int getTableNumber() { return tableNumber; }

    public String toCsv() {
        return bookingId + "," + clean(customerName) + "," + clean(phoneNumber) + "," +
                bookingDate + "," + bookingTime + "," + numberOfGuests + "," + tableNumber;
    }

    private String clean(String value) {
        return value.replace(",", " ");
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d | Name: %s | Phone: %s | Date: %s | Time: %s | Guests: %d | Table: %d",
                bookingId, customerName, phoneNumber, bookingDate, bookingTime,
                numberOfGuests, tableNumber);
    }
}

class BookingManager {
    private final ArrayList<Booking> bookings = new ArrayList<Booking>();

    public void addBooking(Booking booking) {
        validateBooking(booking);
        if (searchBookingById(booking.getBookingId()) != null) {
            throw new IllegalArgumentException("Booking ID already exists.");
        }
        if (isTableOccupied(booking.getTableNumber(), booking.getBookingDate(), booking.getBookingTime())) {
            throw new IllegalArgumentException("That table is already booked for the selected date and time.");
        }
        bookings.add(booking);
    }

    public boolean deleteBooking(int bookingId) {
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId() == bookingId) {
                bookings.remove(i);
                return true;
            }
        }
        return false;
    }

    // Linear search: O(n)
    public Booking searchBookingById(int bookingId) {
        for (Booking booking : bookings) {
            if (booking.getBookingId() == bookingId) {
                return booking;
            }
        }
        return null;
    }

    public List<Booking> searchByCustomerName(String name) {
        ArrayList<Booking> matches = new ArrayList<Booking>();
        String target = name == null ? "" : name.trim().toLowerCase();
        for (Booking booking : bookings) {
            if (booking.getCustomerName().toLowerCase().contains(target)) {
                matches.add(booking);
            }
        }
        return matches;
    }

    // Insertion sort by ID: best O(n), average/worst O(n^2)
    public void sortBookingsById() {
        for (int i = 1; i < bookings.size(); i++) {
            Booking key = bookings.get(i);
            int j = i - 1;
            while (j >= 0 && bookings.get(j).getBookingId() > key.getBookingId()) {
                bookings.set(j + 1, bookings.get(j));
                j--;
            }
            bookings.set(j + 1, key);
        }
    }

    // Insertion sort by date and time.
    public void sortBookingsByDateTime() {
        for (int i = 1; i < bookings.size(); i++) {
            Booking key = bookings.get(i);
            int j = i - 1;
            while (j >= 0 && compareDateTime(bookings.get(j), key) > 0) {
                bookings.set(j + 1, bookings.get(j));
                j--;
            }
            bookings.set(j + 1, key);
        }
    }

    private int compareDateTime(Booking a, Booking b) {
        int result = a.getBookingDate().compareTo(b.getBookingDate());
        if (result != 0) return result;
        result = a.getBookingTime().compareTo(b.getBookingTime());
        if (result != 0) return result;
        return Integer.compare(a.getTableNumber(), b.getTableNumber());
    }

    public List<Booking> getBookings() {
        return new ArrayList<Booking>(bookings);
    }

    public void saveToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        try {
            for (Booking booking : bookings) {
                writer.write(booking.toCsv());
                writer.newLine();
            }
        } finally {
            writer.close();
        }
    }

    public void loadFromFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) return;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                String[] p = line.split(",", -1);
                if (p.length != 7) continue;
                try {
                    Booking b = new Booking(
                            Integer.parseInt(p[0]), p[1], p[2],
                            LocalDate.parse(p[3]), LocalTime.parse(p[4]),
                            Integer.parseInt(p[5]), Integer.parseInt(p[6]));
                    validateBooking(b);
                    if (searchBookingById(b.getBookingId()) == null &&
                            !isTableOccupied(b.getTableNumber(), b.getBookingDate(), b.getBookingTime())) {
                        bookings.add(b);
                    }
                } catch (RuntimeException ignored) {
                    // Skip invalid saved rows.
                }
            }
        } finally {
            reader.close();
        }
    }

    private boolean isTableOccupied(int table, LocalDate date, LocalTime time) {
        for (Booking booking : bookings) {
            if (booking.getTableNumber() == table &&
                    booking.getBookingDate().equals(date) &&
                    booking.getBookingTime().equals(time)) {
                return true;
            }
        }
        return false;
    }

    private void validateBooking(Booking b) {
        if (b == null) throw new IllegalArgumentException("Booking cannot be null.");
        if (b.getBookingId() <= 0) throw new IllegalArgumentException("Booking ID must be positive.");
        if (b.getCustomerName() == null || b.getCustomerName().trim().length() == 0)
            throw new IllegalArgumentException("Customer name is required.");
        if (b.getPhoneNumber() == null || b.getPhoneNumber().trim().length() == 0)
            throw new IllegalArgumentException("Phone number is required.");
        if (b.getBookingDate() == null) throw new IllegalArgumentException("Booking date is required.");
        if (b.getBookingTime() == null) throw new IllegalArgumentException("Booking time is required.");
        if (b.getNumberOfGuests() <= 0) throw new IllegalArgumentException("Number of guests must be positive.");
        if (b.getTableNumber() <= 0) throw new IllegalArgumentException("Table number must be positive.");
    }
}

class TextBasedInterface {
    private final BookingManager manager;
    private final Scanner scanner;
    private final String dataFile;

    public TextBasedInterface(BookingManager manager, String dataFile) {
        this.manager = manager;
        this.dataFile = dataFile;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("============================================");
        System.out.println("   RESTAURANT BOOKING MANAGEMENT SYSTEM");
        System.out.println("============================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1: addBooking(); break;
                    case 2: showBookings(manager.getBookings()); break;
                    case 3: deleteBooking(); break;
                    case 4: searchBooking(); break;
                    case 5: sortBookings(); break;
                    case 6: saveBookings(); break;
                    case 7:
                        saveBookings();
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1-7.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Application closed.");
    }

    private void printMenu() {
        System.out.println("\n1. Add Booking");
        System.out.println("2. View All Bookings");
        System.out.println("3. Delete Booking");
        System.out.println("4. Search Booking");
        System.out.println("5. Sort Bookings");
        System.out.println("6. Save Bookings");
        System.out.println("7. Exit");
    }

    private void addBooking() {
        int id = readInt("Booking ID: ");
        System.out.print("Customer name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Phone number: ");
        String phone = scanner.nextLine().trim();
        LocalDate date = readDate("Booking date (YYYY-MM-DD): ");
        LocalTime time = readTime("Booking time (HH:MM): ");
        int guests = readInt("Number of guests: ");
        int table = readInt("Table number: ");

        manager.addBooking(new Booking(id, name, phone, date, time, guests, table));
        System.out.println("Booking added successfully.");
    }

    private void deleteBooking() {
        int id = readInt("Booking ID to delete: ");
        if (manager.deleteBooking(id)) {
            System.out.println("Booking deleted successfully.");
        } else {
            System.out.println("Booking not found.");
        }
    }

    private void searchBooking() {
        System.out.println("\n1. Search by booking ID");
        System.out.println("2. Search by customer name");
        int option = readInt("Choose search type: ");
        if (option == 1) {
            int id = readInt("Booking ID: ");
            Booking result = manager.searchBookingById(id);
            System.out.println(result == null ? "Booking not found." : result.toString());
        } else if (option == 2) {
            System.out.print("Customer name: ");
            showBookings(manager.searchByCustomerName(scanner.nextLine()));
        } else {
            System.out.println("Invalid search option.");
        }
    }

    private void sortBookings() {
        System.out.println("\n1. Sort by booking ID");
        System.out.println("2. Sort by date and time");
        int option = readInt("Choose sort type: ");
        if (option == 1) {
            manager.sortBookingsById();
        } else if (option == 2) {
            manager.sortBookingsByDateTime();
        } else {
            System.out.println("Invalid sort option.");
            return;
        }
        System.out.println("Bookings sorted successfully.");
        showBookings(manager.getBookings());
    }

    private void showBookings(List<Booking> list) {
        if (list.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        System.out.println("\n---------------- BOOKINGS ----------------");
        for (Booking b : list) {
            System.out.println(b);
        }
    }

    private void saveBookings() throws IOException {
        manager.saveToFile(dataFile);
        System.out.println("Bookings saved successfully.");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use YYYY-MM-DD, for example 2026-09-18.");
            }
        }
    }

    private LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalTime.parse(scanner.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use HH:MM, for example 19:30.");
            }
        }
    }
}

