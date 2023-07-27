package data;

import domain.*;
import domain.Amenity;
import domain.Booking;
import domain.Listing;
import domain.User;

import exception.DataAccessException;
import filter.UserFilter;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dao {
    private final String url;
    private final String username;
    private final String password;

    public Dao(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // For testing purposes
    public Dao() {
        this.url = "jdbc:mysql://localhost:3307/mydb";
        this.username = "root";
        this.password = "";
    }

    private void executeStatement(SqlQuery query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            stmt.executeUpdate();
        }
    }

    public void insertUser(User user) {
        SqlQuery query = new SqlQuery(
                "INSERT INTO users (sin, name, address, birthdate, occupation) VALUES (?, ?, ?, ?, ?)",
                user.sin(), user.name(), user.address(), Date.valueOf(user.birthdate()), user.occupation());
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user", e);
        }
    }

    public void deleteUser(Long sin) {
        SqlQuery query = new SqlQuery("DELETE FROM users WHERE sin=?", sin);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user", e);
        }
    }

    private List<User> executeUserQuery(SqlQuery query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(rs.getLong("sin"), rs.getString("name"), rs.getString("address"),
                        rs.getDate("birthdate").toLocalDate(), rs.getString("occupation")));
            }
            return users;
        }
    }

    public boolean userExists(Long sin) {
        SqlQuery query = new SqlQuery("SELECT * FROM users WHERE sin=?", sin);
        try {
            return !executeUserQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking user exists", e);
        }
    }

    public List<User> getUsers() {
        SqlQuery query = new SqlQuery("SELECT * FROM users");
        try {
            return executeUserQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all users", e);
        }
    }

    public List<User> getUsersByFilter(UserFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (filter.user().sin() != null) {
            sql.append(" AND sin = ?");
            parameters.add(filter.user().sin());
        }
        if (filter.user().name() != null) {
            sql.append(" AND name = ?");
            parameters.add(filter.user().name());
        }
        if (filter.user().address() != null) {
            sql.append(" AND address = ?");
            parameters.add(filter.user().address());
        }
        if (filter.user().birthdate() != null) {
            sql.append(" AND birthdate = ?");
            parameters.add(filter.user().birthdate());
        }
        if (filter.user().occupation() != null) {
            sql.append(" AND occupation = ?");
            parameters.add(filter.user().occupation());
        }
        SqlQuery query = new SqlQuery(sql.toString(), parameters.toArray());
        try {
            return executeUserQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting users by filter", e);
        }
    }

    public User getUser(Long sin) {
        SqlQuery query = new SqlQuery("SELECT * FROM users WHERE sin=?", sin);
        try {
            return executeUserQuery(query).get(0);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user", e);
        }
    }

    public List<Listing> executeListingQuery(SqlQuery query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            ResultSet rs = stmt.executeQuery();
            List<Listing> listings = new ArrayList<>();
            while (rs.next()) {
                listings.add(new Listing(rs.getLong("listing_id"), rs.getString("listing_type"),
                        rs.getBigDecimal("price_per_night"), rs.getString("address"),
                        rs.getString("postal_code"), rs.getBigDecimal("longitude"), rs.getBigDecimal("latitude"),
                        rs.getString("city"), rs.getString("country"),
                        rs.getLong("users_sin")));
            }
            return listings;
        }
    }

    public void insertListing(Listing listing) {
        // Note: we disregard the `listing_id` column as it is an auto-increment column
        // whose value is automatically generated.
        SqlQuery query = new SqlQuery("INSERT INTO listings (listing_type, price_per_night, address, postal_code, longitude," +
                "latitude, city, country, users_sin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                listing.listing_type(), listing.price_per_night(), listing.address(), listing.postal_code(), listing.longitude(),
                listing.latitude(), listing.city(), listing.country(), listing.users_sin());
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting listing", e);
        }
    }

    public boolean listingExists(Listing listing) {
        SqlQuery query = new SqlQuery("SELECT * FROM listings WHERE postal_code = ? AND city = ? AND country = ?",
                listing.postal_code(), listing.city(), listing.country());
        try {
            return !executeListingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing exists", e);
        }
    }

    public boolean listingIdExists(long listingId) {
        SqlQuery query = new SqlQuery("SELECT * FROM listings WHERE listing_id = ?", listingId);
        try {
            return !executeListingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing_id exists", e);
        }
    }

    public void deleteListing(long listingId) {
        SqlQuery query = new SqlQuery("DELETE FROM listings WHERE listing_id = ?", listingId);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting listing", e);
        }
    }

    public List<Listing> getListings() {
        SqlQuery query = new SqlQuery("SELECT * FROM listings");
        try {
            return executeListingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all listings", e);
        }
    }

    public Date getCurrentDate() {
        SqlQuery query = new SqlQuery("SELECT CURRENT_DATE()");

        try (Connection conn = DriverManager.getConnection(url, username, password);
            PreparedStatement stmt = conn.prepareStatement(query.sql())) {

            ResultSet rs = stmt.executeQuery();
            return rs.getDate(0);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting date", e);
        }
    }

    public boolean hasFutureBookings(long listingId) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE listings_listing_id = ?", listingId);

        try (Connection conn = DriverManager.getConnection(url, username, password);
            PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            List<Booking> bookings = executeBookingQuery(query);

            // There are no bookings for this listing
            if (bookings.isEmpty()) 
                return false;

            LocalDate current_Date = getCurrentDate().toLocalDate();

            for (int i = 0; i < bookings.size(); i++) {
                Booking booking = bookings.get(i);

                if (booking.end_date().isAfter(current_Date))
                    return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Error getting bookings with listing id, " + listingId, e);
        }
    }

    public List<Booking> executeBookingQuery(SqlQuery query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            ResultSet rs = stmt.executeQuery();
            List<Booking> bookings = new ArrayList<>();
            while (rs.next()) {
                bookings.add(new Booking(rs.getLong("booking_id"), rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(), rs.getDate("transaction_date").toLocalDate(),
                        rs.getBigDecimal("amount"), rs.getString("currency"),
                        rs.getString("payment_method"), rs.getLong("users_sin"),
                        rs.getLong("listings_listing_id")));
            }
            return bookings;
        }
    }

    public boolean bookingExists(Booking booking) {
        // TODO
        // Get bookings related to listingId
        // Compare booking end date to today, if end date is in the future, return True
        return false;
    }

    public void updateListingPrice(long listingId, BigDecimal newPrice) {
        SqlQuery query = new SqlQuery("UPDATE listings SET price_per_night = ? WHERE listing_id = ?", newPrice,
                listingId);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating existing listing price", e);
        }
    }

    public void insertAvailability(Availability availability) {
        SqlQuery query = new SqlQuery("INSERT INTO availabilities (start_date, end_date, listings_listing_id) VALUES (?, ?, ?)",
                availability.start_date(), availability.end_date(), availability.listings_listing_id());
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting availability", e);
        }
    }

    public void deleteAvailability(long availabilityId) {
        SqlQuery query = new SqlQuery("DELETE FROM availabilities WHERE availability_id = ?", availabilityId);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting availability", e);
        }
    }

    private List<Availability> executeAvailabilityQuery(SqlQuery query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            ResultSet rs = stmt.executeQuery();
            List<Availability> availabilities = new ArrayList<>();
            while (rs.next()) {
                availabilities.add(new Availability(rs.getLong("availability_id"), rs.getDate("start_date").toLocalDate(), rs.getDate("end_date").toLocalDate(), rs.getLong("listings_listing_id")));
            }
            return availabilities;
        }
    }

    public List<Availability> getAvailabilities() {
        SqlQuery query = new SqlQuery("SELECT * FROM availabilities");
        try {
            return executeAvailabilityQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all availabilities", e);
        }
    }

    public void insertBooking(Booking booking) {
        SqlQuery query = new SqlQuery("INSERT INTO bookings (start_date, end_date, transaction_date, amount, currency, payment_method, users_sin, listings_listing_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                booking.start_date(), booking.end_date(), booking.transaction_date(), booking.amount(), booking.currency(), booking.payment_method(), booking.users_sin(), booking.listings_listing_id());
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting booking", e);
        }
    }

    public void deleteBooking(long bookingId) {
        SqlQuery query = new SqlQuery("DELETE FROM bookings WHERE booking_id = ?", bookingId);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting booking", e);
        }
    }


    public List<Booking> getBookings() {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings");
        try {
            return executeBookingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all bookings", e);
        }
    }

    public void insertReview(Review review) {
        SqlQuery query = new SqlQuery("INSERT INTO reviews (rating_of_listing, rating_of_host, rating_of_renter, comment_from_renter, comment_from_host, bookings_booking_id) VALUES (?, ?, ?, ?, ?, ?)",
                review.rating_of_listing(), review.rating_of_host(), review.rating_of_renter(), review.comment_from_renter(), review.comment_from_host(), review.bookings_booking_id());
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting review", e);
        }
    }

    public void deleteReview(long reviewId) {
        SqlQuery query = new SqlQuery("DELETE FROM reviews WHERE review_id = ?", reviewId);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting review", e);
        }
    }

    private List<Review> executeReviewQuery(SqlQuery query) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            ResultSet rs = stmt.executeQuery();
            List<Review> reviews = new ArrayList<>();
            while (rs.next()) {
                reviews.add(new Review(rs.getLong("review_id"), rs.getInt("rating_of_listing"), rs.getInt("rating_of_host"), rs.getInt("rating_of_renter"), rs.getString("comment_from_renter"), rs.getString("comment_from_host"), rs.getLong("bookings_booking_id")));
            }
            return reviews;
        }
    }

    public List<Review> getReviews() {
        SqlQuery query = new SqlQuery("SELECT * FROM reviews");
        try {
            return executeReviewQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all reviews", e);
        }
    }


}
