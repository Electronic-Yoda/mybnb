package mybnb;

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

    public void addListing(Listing listing) {

        if (!dao.listingExists(listing)) {
            dao.insertListing(listing);
            logger.info("Listing added successfully");
        } else {
            throw new ServiceException(
                    String.format(
                            "Unable to add listing because listing at %s, %s. %s already exists",
                            listing.country(), listing.city(), listing.postal_code()));
        }
    }

    public void deleteListing(long listingId) {
        if (!dao.listingIdExists(listingId)) {
            // TODO Check if future bookings exist

            dao.deleteListing(listingId);
        } else {
            throw new ServiceException(
                    String.format(
                            "Unable to add listing because listing with id, %d, doesnt exists",
                            listingId));
        }
    }

    public void updateListingPrice(long listingId, BigDecimal newPrice) {
        // compareTo returns -1 if newPrice < 0
        // TODO double check requirements
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(
                    String.format("Unable to update listing price, %d. Price must be a non-negative number.",
                            newPrice));
        }

        if (!dao.listingIdExists(listingId)) {
            dao.updateListingPrice(listingId, newPrice);
        } else {
            throw new ServiceException(
                    String.format(
                            "Unable to add listing because listing with id, %d, doesnt exists",
                            listingId));
        }
    }

    public void changeListingAvailability(long listingId, LocalDate startDate, LocalDate endDate, boolean isAvailable) {
        // TODO: Implement method
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
