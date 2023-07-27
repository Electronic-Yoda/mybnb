package data;

import domain.Listing;
import domain.User;
import filter.UserFilter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DaoTest {


    @org.junit.jupiter.api.Test
    void userTest() {
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();
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
    }

    @org.junit.jupiter.api.Test
    void userFilterTest() {
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();

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
    }

    @org.junit.jupiter.api.Test
    void listingBasicTest() {
        Dao dao = new Dao();
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();

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
                new BigDecimal(100),
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

    }
}