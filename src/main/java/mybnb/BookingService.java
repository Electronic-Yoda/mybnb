package mybnb;

import data.Dao;
import domain.Availability;
import domain.Booking;
import domain.Listing;
import exception.ServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    public BookingService(Dao dao) {
        this.dao = dao;
    }

    public void addBooking(Booking booking) throws ServiceException {
        // Standard check-in is 3 PM and checkout-out is 11 AM

        // checks for at least one day in between and booking.start_date() cannot be before EndDate
        if (!booking.start_date().isBefore(booking.end_date())) {
            throw new ServiceException(String.format("start date must be before end date."));
        }

        // Check if listing exists
        if (!dao.listingIdExists(booking.listings_listing_id())) {
            throw new ServiceException(String.format("listing with id, %d, does not exist.", booking.listings_listing_id()));
        }

        Availability affectedAvailability = null;
        try {
            affectedAvailability = dao.getAffectedAvailability(booking.listings_listing_id(), booking.start_date(), booking.end_date());
            dao.deleteAvailability(booking.listings_listing_id(), affectedAvailability.start_date(), affectedAvailability.end_date());
        } catch (Exception e) {
            throw new ServiceException(String.format("There is no availability for the given date range."));
        }

        // Insert availabilities which are not affected by booking
        // Case 1: Booking is in between affectedAvailability range
        if (affectedAvailability.start_date().isBefore(booking.start_date()) && affectedAvailability.end_date().isAfter(booking.end_date())) {
            dao.insertAvailability(new Availability(null, affectedAvailability.start_date(), booking.start_date(), booking.listings_listing_id()));
            dao.insertAvailability(new Availability(null, booking.end_date(), affectedAvailability.end_date(), booking.listings_listing_id()));
        }

        // Case 2: Booking is in the first half of availability range
        else if (affectedAvailability.start_date().isEqual(booking.start_date()) && affectedAvailability.end_date().isAfter(booking.end_date())) {
            dao.insertAvailability(new Availability(null, booking.end_date(), affectedAvailability.end_date(), booking.listings_listing_id()));
        }

        // Case 3: Booking is in last half of availability range
        else if (affectedAvailability.start_date().isBefore(booking.start_date()) && affectedAvailability.end_date().isEqual(booking.end_date())) {
            dao.insertAvailability(new Availability(null, affectedAvailability.start_date(), booking.start_date(), booking.listings_listing_id()));
        }

        // Case 4: Booking date range exactly matches to availability date range
        //  --- No need to create new availability

        // calculate booking cost
        Listing listing = dao.getListingById(booking.listings_listing_id());
        BigDecimal amount = listing.price_per_night().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(booking.start_date(), booking.end_date())), new MathContext(2));

       Booking bookingToInsert = new Booking(null, booking.start_date(), booking.end_date(), LocalDate.now(),
               amount, booking.payment_method(), booking.card_number(), booking.tenant_sin(), booking.listings_listing_id());
        // Insert booking
        try {
            dao.insertBooking(bookingToInsert);
        } catch (Exception e) {
            throw new ServiceException(String.format("Unable to insert booking."));
        }
    }


    public void cancelBooking(Long rentingId) {
        // TODO: Implement method


    }
}
