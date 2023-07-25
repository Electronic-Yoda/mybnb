package mybnb;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import mylogger.ConsoleLogger;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import domain.User;
import domain.Listing;
import domain.Availability;
import domain.Renting;

public class Service {
    private final Dao dao = new Dao();
    private static final Logger logger = LogManager.getLogger(Service.class);

    // ============= Setups and resets =============
    public void setUpDb() {
        try {
            dao.createTables();
        } catch (Exception e) {
            logger.error("Error while setting up database: " + e.getMessage(), e);
        }
        logger.info("Database setup successfully");
    }

    public void resetDb() {
        try {
            dao.dropTables();
            dao.createTables();
        } catch (Exception e) {
            logger.error("Error while resetting database: " + e.getMessage(), e);
        }
        logger.info("Database reset successfully");
    }

    // ============= Operations =============
    public void createUser(User user) {
        //TODO: Implement method
    }

    public void deleteUser(int sin) {
        //TODO: Implement method
    }

    public void createListing(Listing listing) {
        //TODO: Implement method
    }

    public void deleteListing(long listingId) {
        //TODO: Implement method
    }

    public void bookListing(long listingId, LocalDate startDate, LocalDate endDate) {
        //TODO: Implement method
    }

    public void cancelBooking(long rentingId) {
        //TODO: Implement method
    }

    public void updateListingPrice(long listingId, BigDecimal newPrice) {
        //TODO: Implement method
    }

    public void changeListingAvailability(long listingId, LocalDate startDate, LocalDate endDate, boolean isAvailable) {
        //TODO: Implement method
    }

    public void insertReviewFromRenter(long rentingId, int ratingOfListing, int ratingOfHost, String commentFromRenter) {
        //TODO: Implement method
    }

    public void insertReviewFromHost(long rentingId, int ratingOfRenter, String commentFromHost) {
        //TODO: Implement method
    }
    public List<Listing> searchListingsByLocation(double latitude, double longitude, double distance) {
        // TODO: Implement method
        return null;
    }


    // ============= Search methods =============
    public List<Listing> searchListingsByLocationAndPrice(double latitude, double longitude, double distance, boolean priceAscending) {
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


    // === Only for testing purposes
    public static void main(String[] args) throws ClassNotFoundException {
        ConsoleLogger.setup();
        Service service = new Service();
        service.resetDb();
    }

}
