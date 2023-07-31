package data;

import exception.ServiceException;
import service.*;
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
public class serviceTest {
    @org.junit.jupiter.api.Test
    void operationsTest() {
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();
        Dao dao = new Dao();
        UserService userService = new UserService(dao);
        ListingService listingService = new ListingService(dao);
        BookingService bookingService = new BookingService(dao);

        // create two users
        User user1 = new User(
                1L,
                "Donald Doe",
                "32 Main St. Toronto, ON",
                LocalDate.parse("2001-03-12"),
                "Student"
        );
        User user2 = new User(
                2L,
                "John Doe",
                null, // can be null (for user privacy)
                LocalDate.parse("1976-01-09"),
                "Doctor"
        );
        try {
            userService.addUser(user1);
            userService.addUser(user2);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        // Let user1 create two listings
        Listing listing1 = new Listing(
                null,
                "house",
                new BigDecimal("100.00"),
                "123 Main St.",
                "M5S 1A1",
                new BigDecimal("43.66"),
                new BigDecimal("79.40"),
                "Toronto",
                "Canada",
                user1.sin()
        );
        Listing listing2 = new Listing(
                null,
                "condo",
                new BigDecimal("50"),
                "111 Main St.",
                "M5T 1C1",
                new BigDecimal("42.11"),
                new BigDecimal("79.40"),
                "Toronto",
                "Canada",
                user1.sin()
        );
        try {
            listingService.addListing(listing1);
            listingService.addListing(listing2);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        // let user2 book listing1





    }
}
