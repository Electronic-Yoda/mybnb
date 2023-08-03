package service;

import data.Dao;
import data.DbConfig;
import domain.*;
import exception.ServiceException;
import service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        List <User> users = null;
        try {
            userService.addUser(user1);
            userService.addUser(user2);
            users = userService.getUsers();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        for (User user : users) {
            System.out.println(user);
        }
        assertTrue(users.size() == 2);

        // Let user1 create two listings
        List<Listing> listings = null;
        Listing listing1 = new Listing(
                null,
                "house",
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
            listings = listingService.getListings();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        for (Listing listing : listings) {
            System.out.println(listing);
        }
        assertTrue(listings.size() == 2);

        // let user1 add availability to listing1
        List<Availability> availabilities = null;
        Availability availability1 = new Availability(
                null,
                LocalDate.parse("2023-09-10"),
                LocalDate.parse("2023-09-20"),
                new BigDecimal("300.00"),
                1L
        );
        try {
            listingService.addAvailability(availability1, 1L);
            availabilities = listingService.getAvailabilities();
        } catch (ServiceException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        for (Availability availability : availabilities) {
            System.out.println(availability);
        }
        assertTrue(availabilities.size() == 1);

        // let user2 book listing1
        List<Booking> bookings = null;
        try {
            bookingService.addBooking(
                    new Booking(
                            null,
                            LocalDate.parse("2023-09-12"),
                            LocalDate.parse("2023-09-15"),
                            LocalDate.parse("2023-07-31"),
                            new BigDecimal("300.00"),
                            "visa",
                            1324567890123456L,
                            user2.sin(),
                            1L
                    )
            );
            bookings = bookingService.getBookings();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        System.out.println("inserted Bookings:");
        for (Booking booking : bookings) {
            System.out.println(booking);
        }
        assertTrue(bookings.size() == 1);

        // check if availability is updated
        System.out.println("Availability after booking:");
        List<Availability> list1Availabilities = null;
        try {
            list1Availabilities = listingService.getAvailabilitiesOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        list1Availabilities.forEach(System.out::println);

        for (Availability availability : list1Availabilities) {
            if (availability.start_date().equals(LocalDate.parse("2023-09-11"))) {
                assertTrue(availability.end_date().equals(LocalDate.parse("2023-09-12")));
            }
            if (availability.end_date().equals(LocalDate.parse("2023-09-20"))) {
                assertTrue(availability.start_date().equals(LocalDate.parse("2023-09-15")));
            }
        }

        // check if user2 can book listing1 again
        try {
            bookingService.addBooking(
                    new Booking(
                            null,
                            LocalDate.parse("2023-09-12"),
                            LocalDate.parse("2023-09-15"),
                            LocalDate.parse("2023-07-31"),
                            new BigDecimal("300.00"),
                            "visa",
                            1324567890123456L,
                            user2.sin(),
                            1L
                    )
            );
            bookings = bookingService.getBookings();
        } catch (ServiceException e) {
            // code should reach here since user2 cannot book listing1 again
            assertTrue(true);
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }

        // cancel booking
        List<CancelledBooking> cancelledBookings = null;
        try {
            bookingService.tenantCancelBooking(1L, 2L, LocalDate.parse("2023-07-31"));
            bookings = bookingService.getBookings();
            cancelledBookings = bookingService.getCancelledBookings();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        assertTrue(bookings.size() == 0);
        assertTrue(cancelledBookings.size() == 1);
        System.out.println("Cancelled bookings:");
        cancelledBookings.forEach(System.out::println);

        // check if availability is updated
        System.out.println("Availability after cancelling booking:");
        try {
            list1Availabilities = listingService.getAvailabilitiesOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        list1Availabilities.forEach(System.out::println);
        assertTrue(list1Availabilities.size() == 1);
        assertTrue(list1Availabilities.get(0).start_date().equals(LocalDate.parse("2023-09-10")));
        assertTrue(list1Availabilities.get(0).end_date().equals(LocalDate.parse("2023-09-20")));

        // change listing1 availability
        try {
            listingService.changeListingAvailability(
                    1L,
                    1L,
                    LocalDate.parse("2023-09-10"),
                    LocalDate.parse("2023-09-20"),
                    LocalDate.parse("2023-09-12"),
                    LocalDate.parse("2023-09-20")
            );
            list1Availabilities = listingService.getAvailabilitiesOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        System.out.println("Availability after changing availability:");
        list1Availabilities.forEach(System.out::println);

        // check if user2 can book listing1 again
        Long bookingID = null;
        try {
            bookingID = bookingService.addBooking(
                    new Booking(
                            null,
                            LocalDate.parse("2023-09-12"),
                            LocalDate.parse("2023-09-15"),
                            LocalDate.parse("2023-07-31"),
                            new BigDecimal("300.00"),
                            "visa",
                            1324567890123456L,
                            user2.sin(),
                            1L
                    )
            );
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        // check if availability is updated
        System.out.println("Availability after booking:");
        try {
            list1Availabilities = listingService.getAvailabilitiesOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        list1Availabilities.forEach(System.out::println);
        assertTrue(list1Availabilities.size() == 1);
        assertTrue(list1Availabilities.get(0).start_date().equals(LocalDate.parse("2023-09-15")));

        // get current reviews (fields should be null)
        List<Review> reviews = null;
        try {
            reviews = bookingService.getReviewsOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        assertTrue(reviews.size() == 1);
        System.out.println("Current reviews(fields should be null):");
        reviews.forEach(System.out::println);

        // assume user2 finished the stay and wants to leave ratings and reviews, and host wants to leave rating and review
        LocalDate currentDate = LocalDate.parse("2023-09-16");
        try {
            bookingService.tenantRateHost(2L, 5, bookingID, currentDate);
            bookingService.addCommentFromTenant(2L, "Great Service!", bookingID, currentDate);
            bookingService.tenantRateListing(2L, 4, bookingID, currentDate);
            bookingService.hostRateTenant(1L, 5, bookingID, currentDate);
            bookingService.addCommentFromHost(1L, "Great guest!", bookingID, currentDate);
            reviews = bookingService.getReviewsOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        assertTrue(reviews.size() == 1);
        reviews.forEach(System.out::println);
        assertTrue(reviews.get(0).rating_of_host() == 5);
        assertTrue(reviews.get(0).rating_of_listing() == 4);
        assertTrue(reviews.get(0).comment_from_tenant().equals("Great Service!"));
        assertTrue(reviews.get(0).comment_from_host().equals("Great guest!"));

        // change listing1 availability price
        try {
            listingService.changeListingAvailabilityPrice(
                    1L,
                    1L,
                    LocalDate.parse("2023-09-15"),
                    LocalDate.parse("2023-09-20"),
                    new BigDecimal("400.00")
            );
            list1Availabilities = listingService.getAvailabilitiesOfListing(1L);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        System.out.println("Availability after changing availability price:");
        list1Availabilities.forEach(System.out::println);
        assertTrue(list1Availabilities.get(0).price_per_night().equals(new BigDecimal("400.00")));

        // change current date to before booking end date
        currentDate = LocalDate.parse("2023-09-14");
        // try delete listing1
        try {
            listingService.deleteListing(1L, 1L, currentDate);
        } catch (ServiceException e) {
            // code should reach here since user2 cannot delete listing1
            assertTrue(true);
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }

        // change current date to after booking end date
        currentDate = LocalDate.parse("2023-09-21");
        // try delete listing1
        try {
            listingService.deleteListing(1L, 1L, currentDate);
            listings = listingService.getListings();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        assertTrue(listings.size() == 1);
    }
}
