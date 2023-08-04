package data;

import domain.Availability;
import domain.Listing;
import domain.User;
import filter.ListingFilter;
import filter.UserFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DaoTest {


    @org.junit.jupiter.api.Test
    void userTest() {
        System.out.println("\nuserTest");
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();

        dao.startTransaction();
        assertTrue(dao.getUsers().isEmpty());

        User user = new User(
                123456789L,
                "John Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        dao.insertUser(user);
        User userRetrieved = dao.getUsers().get(0);
        assertTrue(dao.getUsers().size() == 1);
        assertTrue(user.equals(userRetrieved));
        dao.deleteUser(123456789L);
        assertTrue(dao.getUsers().isEmpty());
        dao.commitTransaction();
    }

    @org.junit.jupiter.api.Test
    void userFilterTest() {
        System.out.println("\nuserFilterTest");
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();
        dao.startTransaction();
        assertTrue(dao.getUsers().isEmpty());

        User user1 = new User(
                1L,
                "John Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        dao.insertUser(user1);
        User user2 = new User(
                2L,
                "John Doe",
                null, // can be null (for user privacy)
                LocalDate.parse("1976-01-09"),
                "Doctor"
        );
        dao.insertUser(user2);

        assertTrue(dao.getUsers().size() == 2);
        List<User> usersRetrieved = dao.getUsersByFilter(
                new UserFilter(
                    new User(
                            1L,
                            null,
                            null,
                            null,
                            null
                    )
                )
        );
        assertTrue(usersRetrieved.size() == 1);
        assertTrue(usersRetrieved.get(0).equals(user1));

        usersRetrieved = dao.getUsersByFilter(
                new UserFilter(
                        new User(
                                null,
                                "John Doe",
                                null,
                                null,
                                null
                        )
                )
        );
        assertTrue(usersRetrieved.size() == 2);
        for (User user : usersRetrieved) {
            assertTrue(user.name().equals("John Doe"));
            System.out.println(user);
        }
        dao.commitTransaction();
    }

    @org.junit.jupiter.api.Test
    void listingBasicTest() {
        System.out.println("\nlistingBasicTest");
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();
        dao.startTransaction();
        User user = new User(
                123456789L,
                "John Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        dao.insertUser(user);

        Listing listing = new Listing(
                null,
                "house",
                "123 Main St.",
                "M5S 1A1",
                new BigDecimal(43.66),
                new BigDecimal(79.40),
                "Toronto",
                "Canada",
                123456789L
        );
        dao.insertListing(listing);
        assertTrue(dao.listingExists(listing));
        List<Listing> retrievedListings = dao.getListings();
        assertTrue(retrievedListings.size() == 1);
        System.out.println(retrievedListings.get(0));
        dao.deleteListing(retrievedListings.get(0).listing_id());
        assertFalse(dao.listingExists(retrievedListings.get(0)));
        dao.commitTransaction();
        dbConfig.resetTables();
    }

    @org.junit.jupiter.api.Test
    void listingTest() {
        System.out.println("\nlistingTest");
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();

        dao.startTransaction();

        User user = new User(
                1L,
                "John Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        dao.insertUser(user);

        // let user have 2 listings
        Listing listing1 = new Listing(
                null,
                "house",
                "123 Main St.",
                "M5S 1A1",
                new BigDecimal("43.66"),
                new BigDecimal("79.40"),
                "Toronto",
                "Canada",
                user.sin()
        );
        Long listing_id1 = dao.insertListing(listing1);

        Listing listing2 = new Listing(
                null,
                "condo",
                "111 Main St.",
                "M5T 1C1",
                new BigDecimal("42.11"),
                new BigDecimal("79.40"),
                "Toronto",
                "Canada",
                user.sin()
        );
        Long listing_id2 = dao.insertListing(listing2);

        assertTrue(dao.listingExists(listing1));
        assertTrue(dao.listingExists(listing2));

        assertTrue(dao.getListingByLocation(
                listing1.postal_code(),
                listing1.city(),
                listing1.country()
        ).listing_id().equals(listing_id1));

        assertTrue(dao.getListingByLocation(
                listing2.postal_code(),
                listing2.city(),
                listing2.country()
        ).listing_id().equals(listing_id2));

        Availability listing1Availability1 = new Availability(
                null,
                LocalDate.parse("2021-03-12"),
                LocalDate.parse("2021-03-15"),
                new BigDecimal("200.00"),
                listing_id1
        );
        dao.insertAvailability(listing1Availability1);

        Availability listing1Availability2 = new Availability(
                null,
                LocalDate.parse("2021-03-20"),
                LocalDate.parse("2021-03-25"),
                new BigDecimal("100.00"),
                listing_id1
        );
        dao.insertAvailability(listing1Availability2);

        Availability listing2Availability1 = new Availability(
                null,
                LocalDate.parse("2021-03-12"),
                LocalDate.parse("2021-03-15"),
                new BigDecimal("200.00"),
                listing_id2
        );
        dao.insertAvailability(listing2Availability1);

        Availability listing2Availability2 = new Availability(
                null,
                LocalDate.parse("2021-03-15"),
                LocalDate.parse("2021-03-17"),
                new BigDecimal("200.00"),
                listing_id2
        );
        dao.insertAvailability(listing2Availability2);

        // check if all availabilities were inserted
        assertTrue(dao.getAvailabilities().size() == 4);

        dao.getAllAmenities().forEach(System.out::println);
        // insert amenities
        dao.insertAmenityForListing(listing_id1, "wifi");
        dao.insertAmenityForListing(listing_id1, "tv");

        dao.insertAmenityForListing(listing_id2, "wifi");
        dao.insertAmenityForListing(listing_id2, "kitchen");

        List<String> amenities1 = dao.getAmenitiesByListingId(listing_id1);
        List<String> amenities2 = dao.getAmenitiesByListingId(listing_id2);
        assertTrue(amenities1.size() == 2);
        assertTrue(amenities2.size() == 2);

        dao.getUsers().forEach(System.out::println);
        dao.getListings().forEach(System.out::println);
        dao.getAvailabilities().forEach(System.out::println);
        System.out.println("Amenities for listing 1:");
        System.out.println(amenities1);
        System.out.println("Amenities for listing 2:");
        System.out.println(amenities2);
        System.out.println();

        // use filter to get listings with availability between 2021-03-12 and 2021-03-15
        List<Listing> listingsRetrieved = dao.getListingsByFilter(
                new ListingFilter(
                        null,
                        new Availability(
                                null,
                                LocalDate.parse("2021-03-12"),
                                LocalDate.parse("2021-03-16"),
                                null,
                                null
                        ),
                        null
                )
        );
        assertTrue(listingsRetrieved.size() == 2);
        System.out.println("Listings retrieved with availability between 2021-03-12 and 2021-03-16:");
        listingsRetrieved.forEach(System.out::println);

        // use filter to get listings with amenities wifi
        listingsRetrieved = dao.getListingsByFilter(
                new ListingFilter(
                        null,
                        null,
                        Collections.singletonList("wifi")
                )
        );
        assertTrue(listingsRetrieved.size() == 2);
        System.out.println("Listings retrieved by filtering with wifi:");
        listingsRetrieved.forEach(System.out::println);

        // use filter to get listings with amenities wifi and tv
        listingsRetrieved = dao.getListingsByFilter(
                new ListingFilter(
                        null,
                        null,
                        List.of("wifi", "tv")
                )
        );
        assertTrue(listingsRetrieved.size() == 1);
        System.out.println("Listings retrieved by filtering with wifi and tv:");
        listingsRetrieved.forEach(System.out::println);

        // use filter to get listings with amenities backyard
        listingsRetrieved = dao.getListingsByFilter(
                new ListingFilter(
                        null,
                        null,
                        Collections.singletonList("backyard")
                )
        );
        assertTrue(listingsRetrieved.isEmpty());

        // use filter to get listings with wifi and is a house
        listingsRetrieved = dao.getListingsByFilter(
                new ListingFilter(
                        new Listing(
                                null,
                                "house",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        null,
                        Collections.singletonList("wifi")
                )
        );
        assertTrue(listingsRetrieved.size() == 1);
        System.out.println("Listings retrieved by filtering with wifi and is a house:");
        listingsRetrieved.forEach(System.out::println);

        // Test Cascade delete. If a listing is deleted, all its availabilities and amenities should be deleted
        dao.deleteListing(listing_id1);
        assertTrue(dao.getListings().size() == 1);
        assertTrue(dao.getAvailabilities().size() == 2);
        assertTrue(dao.getAmenitiesByListingId(listing_id1).isEmpty());
        assertTrue(dao.getAmenitiesByListingId(listing_id2).size() == 2);
        assertTrue(dao.getListingByLocation(
                listing1.postal_code(),
                listing1.city(),
                listing1.country()
        ) == null);

        System.out.println(dao.getAvailabilities());
        // Test delete availability
        dao.deleteAvailability(listing_id2, listing2Availability1.start_date(), listing2Availability1.end_date());
        System.out.println(dao.getAvailabilities());

        assertTrue(dao.getAvailabilities().size() == 1);
        System.out.println("Availabilities: " +  dao.getAvailabilities());

        dao.commitTransaction();

        dbConfig.resetTables();
    }
}