package service;

import data.Dao;
import domain.*;
import exception.DataAccessException;
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
        try {
            dao.startTransaction();
            // Standard check-in is 3 PM and checkout-out is 11 AM
            // checks for at least one day in between and booking.start_date() cannot be before EndDate
            if (!booking.start_date().isBefore(booking.end_date())) {
                throw new ServiceException(String.format("start date must be before end date."));
            }

            // check if booking exists for the given date range and listing
            if (dao.bookingExists(booking.listings_listing_id(), booking.start_date(), booking.end_date())) {
                throw new ServiceException(String.format("Booking already exists for the given date range."));
            }

            // Check if listing exists
            if (!dao.listingIdExists(booking.listings_listing_id())) {
                throw new ServiceException(String.format("listing with id, %d, does not exist.", booking.listings_listing_id()));
            }

            Availability affectedAvailability = null;
            try {
                affectedAvailability = dao.getAffectedAvailability(booking.listings_listing_id(), booking.start_date(), booking.end_date());
            } catch (DataAccessException e) {
                throw new ServiceException(String.format("There is no availability for the given date range."), e);
            }
            dao.deleteAvailability(affectedAvailability.availability_id());
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

            // Case 4: Booking date range exactly matches to availability date range, so no availabilities to insert

            // calculate booking cost
            Listing listing = dao.getListingById(booking.listings_listing_id());
            BigDecimal amount = listing.price_per_night().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(booking.start_date(), booking.end_date())), new MathContext(2));

           Booking bookingToInsert = new Booking(null, booking.start_date(), booking.end_date(), LocalDate.now(),
                   amount, booking.payment_method(), booking.card_number(), booking.tenant_sin(), booking.listings_listing_id());
            // Insert booking
            Long bookingId = dao.insertBooking(bookingToInsert);

            // Create Review

            // insert review with all null values
            Review review = new Review(null, null, null, null, null, null, bookingId);
            dao.insertReview(review);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to insert booking."), e);
        }
    }

    public void hostCancelBooking(Long booking_id, Long host_sin) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.hostSinMatchesBookingId(host_sin, booking_id)) {
                throw new ServiceException(String.format("Unable to cancel booking because host sin does not match. "));
            }
            dao.commitTransaction();
            cancelBooking(booking_id);
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to cancel booking."), e);
        }
    }

    public void tenantCancelBooking(Long booking_id, Long tenant_sin) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.tenantSinMatchesBookingId(tenant_sin, booking_id)) {
                throw new ServiceException(String.format("Unable to cancel booking because tenant sin does not match. "));
            }
            dao.commitTransaction();
            cancelBooking(booking_id);
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to cancel booking."), e);
        }
    }

    private void cancelBooking(Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();
            // check if booking exists
            if (!dao.bookingExists(booking_id)) {
                throw new ServiceException(String.format("Booking with id, %d, does not exist.", booking_id));
            }

            // get booking
            Booking booking = null;
            try {
                booking = dao.getBooking(booking_id);
            } catch (DataAccessException e) {
                throw new ServiceException(String.format("Unable to retrieve booking."), e);
            }
            if (booking.start_date().isBefore(LocalDate.now())) {
                throw new ServiceException(String.format("Not allowed to cancel booking because booking has already started."));
            }

            // delete booking
            dao.deleteBooking(booking_id);

            // insert cancelled booking
            dao.insertCancelledBooking(new CancelledBooking(null, booking.start_date(), booking.end_date(), LocalDate.now(),
                    booking.amount(), booking.payment_method(), booking.card_number(), booking.tenant_sin(), booking.listings_listing_id()));

            // re-insert availability
            LocalDate newAvailabilityStartDate = booking.start_date(); // To be reset if affectedAvailability exists
            LocalDate newAvailabilityEndDate = booking.end_date(); // To be reset if affectedAvailability exists

            // get affected availabilities (two possible cases)
            // Case 1: there is an availability whose end_date is the same as booking start_date
            Availability affectedAvailability1 = dao.getAvailability(booking.listings_listing_id(), booking.start_date(), booking.end_date());
            if (affectedAvailability1 != null) {
                newAvailabilityStartDate = affectedAvailability1.start_date();
                dao.deleteAvailability(affectedAvailability1.availability_id());
            }
            // Case 2: there is an availability whose start_date is the same as booking end_date
            Availability affectedAvailability2 = dao.getAvailability(booking.listings_listing_id(), booking.start_date(), booking.end_date());
            if (affectedAvailability2 != null) {
                newAvailabilityEndDate = affectedAvailability2.end_date();
                dao.deleteAvailability(affectedAvailability2.availability_id());
            }
            dao.insertAvailability(new Availability(null, newAvailabilityStartDate, newAvailabilityEndDate, booking.listings_listing_id()));
            dao.commitTransaction();
        } catch (DataAccessException e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to cancel booking."), e);
        }
    }


    public void tenantRateListing(Long tenant_id, Integer rating, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check tenant_id matches in booking
            if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
                throw new ServiceException("Tenant does not match with booking");

            // Rating must be between 1 and 5 (inclusive)
            if (rating < 1 || rating > 5)
                throw new ServiceException("Rating must be and including 1 - 5");

            dao.tenantRateListing(tenant_id, rating, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to rate listing."), e);
        }
    }

    public void deleteTenantRateListing(Long tenant_id, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check tenant_id matches in booking
            if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
                throw new ServiceException("Tenant does not match with booking");

            dao.tenantRateListing(tenant_id, null, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to delete tenant rating for listing."), e);
        }
        dao.startTransaction();

        // Check tenant_id matches in booking
        if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
            throw new ServiceException("Tenant does not match with booking");

        dao.tenantRateListing(tenant_id, -1, booking_id);
        dao.commitTransaction();
    }

    public void tenantRateHost(Long tenant_id, Integer rating, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check tenant_id matches in booking
            if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
                throw new ServiceException("Tenant does not match with booking");

            // Rating must be between 1 and 5 (inclusive)
            if (rating < 1 || rating > 5)
                throw new ServiceException("Rating must be and including 1 - 5");

            dao.tenantRateHost(tenant_id, rating, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to rate host."), e);
        }
    }

    public void deleteTenantRateHost(Long tenant_id, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check tenant_id matches in booking
            if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
                throw new ServiceException("Tenant does not match with booking");

            dao.tenantRateHost(tenant_id, null, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to delete tenant rating for host."), e);
        }
    }

    public void tenantCommentsOnHost(Long tenant_id, String comment, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check tenant_id matches in booking
            if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
                throw new ServiceException("Tenant does not match with booking");

            if (comment.length() > 500)
                throw new ServiceException("Comment must be within 500 characters");

            dao.tenantCommentsOnHost(tenant_id, comment, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to comment on host."), e);
        }
    }

    public void deleteTenantCommentsOnHost(Long tenant_id, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check tenant_id matches in booking
            if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
                throw new ServiceException("Tenant does not match with booking");

            dao.tenantCommentsOnHost(tenant_id, "", booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to delete tenant comment on host."), e);
        }
    }

    public void hostRateTenant(Long host_id, Integer rating, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check host_id matches in listing
            if (!dao.hostSinMatchesBookingId(host_id, booking_id))
                throw new ServiceException("Host does not match with booking");

            // Rating must be between 1 and 5 (inclusive)
            if (rating < 1 || rating > 5)
                throw new ServiceException("Rating must be and including 1 - 5");

            dao.hostRateTenant(host_id, rating, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to rate tenant."), e);
        }
    }

    public void deleteHostRateTenant(Long host_id, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check host_id matches in listing
            if (!dao.hostSinMatchesBookingId(host_id, booking_id))
                throw new ServiceException("Host does not match with booking");

            dao.hostRateTenant(host_id, null, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to delete host rating for tenant."), e);
        }
        dao.startTransaction();
    }

    public void hostCommentsOnTenant(Long host_id, String comment, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check host_id matches in listing
            if (!dao.hostSinMatchesBookingId(host_id, booking_id))
                throw new ServiceException("Host does not match with booking");

            if (comment.length() > 500)
                throw new ServiceException("Comment must be within 500 characters");

            dao.hostCommentsOnTenant(host_id, comment, booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to comment on tenant."), e);
        }
    }

    public void deleteHostCommentsOnTenant(Long host_id, Long booking_id) throws ServiceException {
        try {
            dao.startTransaction();

            // Check host_id matches in listing
            if (!dao.hostSinMatchesBookingId(host_id, booking_id))
                throw new ServiceException("Host does not match with booking");

            dao.hostCommentsOnTenant(host_id, "", booking_id);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw new ServiceException(String.format("Unable to delete host comment on tenant."), e);
        }
    }
}

