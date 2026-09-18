import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BookingManagerTest {
    private Booking booking(int id, String name, int table, LocalDate date, LocalTime time) {
        return new Booking(id, name, "0400000000", date, time, 2, table);
    }

    @Test
    void addsSearchesAndDeletesBooking() {
        BookingManager manager = new BookingManager();
        Booking booking = booking(10, "Amina Rahman", 4, LocalDate.of(2026, 9, 20), LocalTime.of(19, 30));

        manager.addBooking(booking);

        assertSame(booking, manager.searchBookingById(10));
        assertEquals(1, manager.searchByCustomerName("amina").size());
        assertTrue(manager.deleteBooking(10));
        assertNull(manager.searchBookingById(10));
    }

    @Test
    void rejectsDuplicateIdsAndTableConflicts() {
        BookingManager manager = new BookingManager();
        LocalDate date = LocalDate.of(2026, 9, 20);
        LocalTime time = LocalTime.of(19, 30);
        manager.addBooking(booking(1, "First", 2, date, time));

        assertThrows(IllegalArgumentException.class,
                () -> manager.addBooking(booking(1, "Duplicate ID", 3, date, time)));
        assertThrows(IllegalArgumentException.class,
                () -> manager.addBooking(booking(2, "Table clash", 2, date, time)));
    }

    @Test
    void sortsAndPersistsBookings() throws Exception {
        BookingManager manager = new BookingManager();
        manager.addBooking(booking(20, "Later", 2, LocalDate.of(2026, 10, 2), LocalTime.of(20, 0)));
        manager.addBooking(booking(5, "Earlier", 1, LocalDate.of(2026, 9, 20), LocalTime.of(18, 0)));

        manager.sortBookingsById();
        assertEquals(5, manager.getBookings().get(0).getBookingId());
        manager.sortBookingsByDateTime();
        assertEquals("Earlier", manager.getBookings().get(0).getCustomerName());

        Path file = Files.createTempFile("rbms-bookings", ".csv");
        try {
            manager.saveToFile(file.toString());
            BookingManager reloaded = new BookingManager();
            reloaded.loadFromFile(file.toString());
            assertEquals(2, reloaded.getBookings().size());
            assertNotNull(reloaded.searchBookingById(20));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
