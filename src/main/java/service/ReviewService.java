package service;

import data.Dao;
import domain.Availability;
import domain.Booking;
import domain.Listing;
import domain.Review;
import exception.DataAccessException;
import exception.ServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReviewService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    public ReviewService(Dao dao) {
        this.dao = dao;
    }

    public void addReview(Review review) throws ServiceException {
        dao.startTransaction();

        // TODO make sure booking services calls this function
        Booking booking = dao.getBooking(review.bookings_booking_id());

        // Check if bookingId is valid
        if (booking != null) {

            // Check if there is already a review for the Booking
            if (dao.reviewExists(booking.booking_id())) {
                throw new ServiceException("Review already exists");
            }

            dao.insertReview(review);
        }
        else {
            throw new ServiceException("Booking id does not exist");
        }

        dao.commitTransaction();
    }

    public void tenantRateListing(Long tenant_id, Integer rating, Long booking_id) throws ServiceException {
        dao.startTransaction();

        // Check tenant_id matches in booking
        if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
            throw new ServiceException("Tenant does not match with booking");

        // Rating must be between 1 and 5 (inclusive)
        if (rating < 1 || rating > 5)
            throw new ServiceException("Rating must be and including 1 - 5");

        dao.tenantRateListing(tenant_id, rating, booking_id);
        dao.commitTransaction();
    }

    public void tenantRateHost(Long tenant_id, Integer rating, Long booking_id) throws ServiceException {
        dao.startTransaction();

        // Check tenant_id matches in booking
        if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
            throw new ServiceException("Tenant does not match with booking");

        // Rating must be between 1 and 5 (inclusive)
        if (rating < 1 || rating > 5)
            throw new ServiceException("Rating must be and including 1 - 5");

        dao.tenantRateHost(tenant_id, rating, booking_id);
        dao.commitTransaction();
    }

    public void tenantCommentsOnHost(Long tenant_id, String comment, Long booking_id) throws ServiceException {
        dao.startTransaction();

        // Check tenant_id matches in booking
        if (!dao.tenantSinMatchesBookingId(tenant_id, booking_id))
            throw new ServiceException("Tenant does not match with booking");

        if (comment.length() > 500)
            throw new ServiceException("Comment must be within 500 characters");

        dao.tenantCommentsOnHost(tenant_id, comment, booking_id);
        dao.commitTransaction();
    }

    public void hostRateTenant(Long host_id, Integer rating, Long booking_id) throws ServiceException {
        dao.startTransaction();

        // Check host_id matches in listing
        if (!dao.hostSinMatchesBookingId(host_id, booking_id))
            throw new ServiceException("Host does not match with booking");
        
        // Rating must be between 1 and 5 (inclusive)
        if (rating < 1 || rating > 5)
            throw new ServiceException("Rating must be and including 1 - 5");        
        
        dao.hostRateTenant(host_id, rating, booking_id);
        dao.commitTransaction();
    }

    public void HostCommentsOnTenant(Long host_id, String comment, Long booking_id) throws ServiceException {
        dao.startTransaction();

        // Check host_id matches in listing
        if (!dao.hostSinMatchesBookingId(host_id, booking_id))
            throw new ServiceException("Host does not match with booking");

        if (comment.length() > 500)
            throw new ServiceException("Comment must be within 500 characters");

        dao.HostCommentsOnTenant(host_id, comment, booking_id);
        dao.commitTransaction();
    }
}
