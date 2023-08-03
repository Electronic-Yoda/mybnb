package service;

import data.Dao;
import domain.Availability;
import domain.Listing;
import exception.ServiceException;
import filter.ListingFilter;
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

    public Long addListing(Listing listing) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (dao.listingExists(listing)) {
                throw new ServiceException(
                        String.format(
                                "Unable to add listing because listing at %s, %s. %s already exists",
                                listing.country(), listing.city(), listing.postal_code()));
            }
            Long listingID = dao.insertListing(listing);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return listingID;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to add listing", e);
        }
    }

    public void deleteListing(Long listingId) throws ServiceException {
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

    public Listing getListing(Long listingId) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to get listing because listing with id, %d, doesn't exist", listingId));
            }
            Listing listing = dao.getListingById(listingId);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return listing;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get listing", e);
        }
    }

    public boolean doesListingExist(Long listingId) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            boolean exists = dao.listingIdExists(listingId);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return exists;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while checking if listing exists", e);
        }
    }

    public boolean doesListingHaveFutureBookings(Long listingId) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to check if listing has future bookings because listing with id, %d, doesn't exist", listingId));
            }
            boolean hasFutureBookings = dao.hasFutureBookings(listingId);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return hasFutureBookings;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while checking if listing has future bookings", e);
        }
    }

    public List<Listing> getListings() throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            List<Listing> listings = dao.getListings();
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return listings;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get listings", e);
        }
    }

    public List<Listing> getListingsOfUser(Long sin) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (!dao.userExists(sin)) {
                throw new ServiceException(
                        String.format("Unable to get listings because user with sin, %d, doesn't exist", sin));
            }
            List<Listing> listings = dao.getListingsByFilter(
                    new ListingFilter(
                            new Listing(
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    sin
                            ),
                            null,
                            null
                    )
            );
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return listings;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get listings", e);
        }
    }

    public void addAmenityToListing(Listing listing, String amenity) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingExists(listing)) {
                throw new ServiceException(
                        String.format(
                                "Unable to add amenity because listing at %s, %s. %s doesn't exist",
                                listing.country(), listing.city(), listing.postal_code()));
            }
            if (dao.listingHasAmenity(listing.listing_id(), amenity)) {
                throw new ServiceException(
                        String.format(
                                "Unable to add amenity because listing at %s, %s. %s already has amenity %s",
                                listing.country(), listing.city(), listing.postal_code(), amenity));
            }
            
            dao.insertAmenityForListing(listing.listing_id(), amenity);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while adding amenity", e);
        }
    }

    public Float getListingPricePerNight(Long listingId) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to get listing price because listing with id, %d, doesn't exist",
                                listingId));
            }
            Float price = dao.getListingPricePerNight(listingId);
            dao.commitTransaction();
            return price;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while getting listing price", e);
        }
    }

    public boolean doesCityExists(String city) throws ServiceException {
        try {
            dao.startTransaction();
            boolean exists = dao.doesCityExists(city);
            dao.commitTransaction();
            return exists;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while checking if city exists", e);
        }
    }

    public String getRecommendedPricePerNight(Long listingId) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to get listing price because listing with id, %d, doesn't exist",
                                listingId));
            }
            String city = dao.getListingById(listingId).city();
            Float price = dao.getAverageListingPriceByCity(city);

            if (price == null) {
                throw new ServiceException(
                        String.format("Unable to get recommended price no listings in city, %s, have a price",
                                city));
            }

            dao.commitTransaction();
            return price.toString();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            System.out.println(e.getMessage());
            throw new ServiceException("An error occured while getting listing price", e);
        }
    }

    public void changeListingAvailabilityPrice(Long listingId, LocalDate start_date, LocalDate end_date, BigDecimal newPrice) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingAvailabilityExists(listingId, start_date, end_date)) {
                throw new ServiceException(
                        String.format(
                                "Unable to change availability price because availability doesnt exist"));
            }
            if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException(
                        String.format("Unable to update listing price, %d. Price must be a non-negative number.",
                                newPrice));
            }
            dao.changeListingAvailabilityPrice(listingId, start_date, end_date, newPrice);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("Unable to change availability price", e);
        }
    }

    public void addAvailability(Availability availability) throws ServiceException {
        try {
            dao.startTransaction();
            if (dao.listingAvailabilityExists(availability.listings_listing_id(),
                    availability.start_date(), availability.end_date())) {
                throw new ServiceException(
                        String.format(
                                "Unable to add availability because availability already exists",
                                availability.listings_listing_id(),
                                availability.start_date(), availability.end_date()));
            }
            dao.insertAvailability(availability);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while adding availability", e);
        }
    }

    public void deleteAvailability(Long listingId, LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingAvailabilityExists(listingId, startDate, endDate)) {
                throw new ServiceException(
                        String.format(
                                "Unable to delete availability because availability doesnt exist",
                                listingId, startDate, endDate));
            }
            dao.deleteAvailability(listingId, startDate, endDate);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while deleting availability", e);
        }
    }

    public boolean doesAvailabilityExist(Long listingId, LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();
            boolean exists = dao.listingAvailabilityExists(listingId, startDate, endDate);
            dao.commitTransaction();
            return exists;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while checking if availability exists", e);
        }
    }

    public boolean doesDateOverlapWithExistingAvailability(Long listingId, LocalDate startDate, LocalDate endDate) throws ServiceException {
        try {
            dao.startTransaction();
            
            // Check if listing exists
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to check for overlap because listing with id, %d, doesn't exist",
                                listingId));
            }

            // Check if date range is valid
            if (startDate.compareTo(endDate) >= 0) {
                throw new ServiceException(
                        String.format("Unable to check for overlap because start date, %s, is after end date, %s",
                                startDate, endDate));
            }

            List<Availability> availability = getAvailabilitiesOfListing(listingId);

            for (int i = 0; i<availability.size(); i++) {
                // Check if date range overlaps with existing availability
                // Case 1: Start date is between existing availability
                if (startDate.compareTo(availability.get(i).start_date()) >= 0 && startDate.compareTo(availability.get(i).end_date()) <= 0) {
                    return true;
                }

                // Case 2: End date is between existing availability
                if (endDate.compareTo(availability.get(i).start_date()) >= 0 && endDate.compareTo(availability.get(i).end_date()) <= 0) {
                    return true;
                }

                // Case 3: Existing availability is between date range
                if (startDate.compareTo(availability.get(i).start_date()) <= 0 && endDate.compareTo(availability.get(i).end_date()) >= 0) {
                    return true;
                }
            }
            dao.commitTransaction();
            return false;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while checking for overlap", e);
        }
    }

    public boolean isHostOfListing(Long sin, Long listingId) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingIdExists(listingId)) {
                throw new ServiceException(
                        String.format("Unable to check if user is host because listing with id, %d, doesn't exist",
                                listingId));
            }
            boolean isHost = dao.doesListingIdHaveHostSin(listingId, sin);
            dao.commitTransaction();
            return isHost;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while checking if user is host", e);
        }
    }

    public void changeListingAvailability(long listingId, LocalDate prevStartDate, LocalDate prevEndDate,
            LocalDate newStartDate, LocalDate newEndDate) throws ServiceException {
        try {
            dao.startTransaction();
            if (!dao.listingAvailabilityExists(listingId, prevStartDate, prevEndDate)) {
                throw new ServiceException(
                        String.format(
                                "Unable to change availability because availability doesnt exist",
                                listingId, prevStartDate, prevEndDate));
            }
            dao.changeListingAvailability(listingId, prevStartDate, prevEndDate, newStartDate, newEndDate);
            dao.commitTransaction();
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occured while changing availability", e);
        }
    }

    public List<Availability> getAvailabilities() throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            List<Availability> availabilities = dao.getAvailabilities();
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return availabilities;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get availabilities", e);
        }
    }

    public List<Availability> getAvailabilitiesOfListing(Long listing_id) throws ServiceException {
        try {
            dao.startTransaction();  // Begin transaction
            if (!dao.listingIdExists(listing_id)) {
                throw new ServiceException(
                        String.format("Unable to get availabilities because listing with id, %d, doesn't exist",
                                listing_id));
            }
            List<Availability> availabilities = dao.getAvailabilitiesOfListing(listing_id);
            dao.commitTransaction();  // Commit transaction if all operations succeeded
            return availabilities;
        } catch (Exception e) {
            dao.rollbackTransaction();  // Rollback transaction if any operation failed
            throw new ServiceException("An error occurred while trying to get availabilities", e);
        }
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
