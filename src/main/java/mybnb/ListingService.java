package mybnb;

import data.Dao;
import domain.Listing;
import exceptions.ServiceException;
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
                        listing.country(), listing.city(), listing.postal_code()
                )
            );
        }
    }

    public void deleteListing(long listingId) {
        // TODO: Implement method
    }

    public void updateListingPrice(long listingId, BigDecimal newPrice) {
        // TODO: Implement method
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
