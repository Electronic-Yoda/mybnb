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
        // Check if tenant sin, host sin, listing id, and availabilty dates match
        

    }


}
