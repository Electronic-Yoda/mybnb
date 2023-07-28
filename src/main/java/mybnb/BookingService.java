package mybnb;

import data.Dao;
import exception.ServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

public class BookingService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    public BookingService(Dao dao) {
        this.dao = dao;
    }

    public void addBooking(Long listingId, LocalDate startDate, LocalDate endDate, String payment_method, Long tenantSin) throws ServiceException {
        // Standard check-in is 3 PM and checkout-out is 11 AM

        // checks for at least one day in between and startDate cannot be before EndDate
        if (!startDate.isBefore(endDate)) {
            throw new ServiceException(String.format("Error booking must be before at least two days, where start date is before end date."));
        }

        // Check if listing exists
        if (!dao.listingIdExists(listingId)) {
            throw new ServiceException(String.format("Error listing with id, %d, does not exist.", listingId));
        }

        try {
            dao.addBooking(listingId, startDate, endDate, payment_method, tenantSin);
        } catch (Exception e) {
            // TODO Create user defined Exception instead of using expcetion in Dao.getAvailability
            throw new ServiceException(String.format("No availiability between %tF to %tF", startDate, endDate));
        }
    }

    public void cancelBooking(Long rentingId) {
        // TODO: Implement method
    }
}
