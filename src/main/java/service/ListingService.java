package service;

import data.Dao;
import domain.Listing;
import exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ListingService {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(ListingService.class);

    public ListingService(Dao dao) {
        this.dao = dao;
    }

    public void addListing(Listing listing) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (dao.listingExists(listing)) {
                throw new ServiceException(
                        String.format(
                                "Unable to add listing because listing at %s, %s. %s already exists",
                                listing.country(), listing.city(), listing.postal_code()));
            }
            dao.insertListing(listing);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to add listing", e);
        }
    }

    public void deleteListing(long listingId) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to delete listing because listing with id, %d, doesn't exist", listingId));
            }
            if (dao.hasFutureBookings(listingId)) {
                throw new ServiceException(
                        String.format("Unable to delete listing because there are future bookings."));
            }
            dao.deleteListing(listingId);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to delete listing", e);
        }
    }



    public void updateListingPrice(long listingId, BigDecimal newPrice) throws ServiceException {
        try {
            dao.startTransaction();
            // compareTo returns -1 if newPrice < 0
            // TODO double check requirements
            if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException(
                        String.format("Unable to update listing price, %d. Price must be a non-negative number.",
                                newPrice));
            }
            if (dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format(
                                "Unable to add listing because listing with id, %d, doesnt exists",
                                listingId));
            }
            dao.updateListingPrice(listingId, newPrice);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while updating Listing price", e);
        }
    }

    public void changeListingAvailability(long listingId, LocalDate prevStartDate, LocalDate prevEndDate,
            LocalDate newStartDate, LocalDate newEndDate) throws ServiceException {
//        if (!dao.listingAvailabilityExists(listingId, prevStartDate, prevEndDate)) {
//            dao.changeListingAvailability(listingId, prevStartDate, prevEndDate, newStartDate, newEndDate);
//        } else {
//            throw new ServiceException(
//                    String.format(
//                            "Unable to add listing because listing with id, %d, with start date," +
//                            "%t, and end date, %t, doesnt exists",
//                            listingId, prevStartDate, prevEndDate));
//        }
    }

    // ============= Search methods =============
    public List<Listing> searchListingsByLocation(double latitude, double longitude, double distance) {
        // TODO: Implement method
        return null;
    }

    public List<Listing> searchListingsByLocationAndPrice(double latitude, double longitude, double distance,
            boolean priceAscending) {
        // TODO: Implement method
        return null;
    }

    public List<Listing> searchListingsByPostalCode(String postalCode) {
        // TODO: Implement method
        return null;
    }

    public Listing searchListingByAddress(String address) {
        // TODO: Implement method
        return null;
    }

    public List<Listing> searchListingsByDateRange(LocalDate startDate, LocalDate endDate) {
        // TODO: Implement method
        return null;
    }

    public List<Listing> searchListingsByPostalCodeAndAmenitiesAndAvailabilityAndPriceRange(
            String postalCode, List<String> amenities, LocalDate startDate, LocalDate endDate,
            BigDecimal minPrice, BigDecimal maxPrice) {
        // TODO: Implement method
        return null;
    }
}
