package mybnb;

import data.Dao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

public class BookingService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    public BookingService(Dao dao) {
        this.dao = dao;
    }

    public void addBooking(long listingId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement method
    }

    public void cancelBooking(long rentingId) {
        // TODO: Implement method
    }
}
