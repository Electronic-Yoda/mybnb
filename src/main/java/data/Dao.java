package data;

import domain.*;

import exception.DataAccessException;
import filter.ListingFilter;
import filter.UserFilter;

import java.awt.geom.Point2D;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dao {
    private final String url;
    private final String username;
    private final String password;
    private static ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

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

    public void startTransaction() {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            threadLocalConnection.set(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Error starting transaction", e);
        }
    }

    public void commitTransaction() {
        Connection conn = threadLocalConnection.get();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.commit();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error committing transaction", e);
        } finally {
            closeConnection();
        }
    }


    public void rollbackTransaction() {
        Connection conn = threadLocalConnection.get();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error rolling back transaction", e);
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        Connection conn = threadLocalConnection.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new DataAccessException("Error closing threadLocalConnection", e);
            } finally {
                threadLocalConnection.remove(); // clean up thread-local variable
            }
        }
    }


    // if the query is an insert statement where the key is automatically inserted, return the generated id
    // otherwise, return null
    private Long executeStatement(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get(); // Note: this is the thread-local connection! We close this at the end of the transaction
        try (PreparedStatement stmt = conn.prepareStatement(query.sql(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < query.parameters().length; i++) {
                if (query.parameters()[i] instanceof Point2D) {
                    String pointWkt = String.format("POINT(%f %f)", ((Point2D) query.parameters()[i]).getX(), ((Point2D) query.parameters()[i]).getY());
                    stmt.setString(i + 1, pointWkt);
                } else {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
            }
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    return null; // no keys generated
                }
            }
        }
    }

    // Note: will not work for geometry types
    private SqlQuery getInsertStatement(Object domainObject, String tableName) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName + " (");
        ArrayList<Object> parameters = new ArrayList<>();

        int count = 0;
        RecordComponent[] components = domainObject.getClass().getRecordComponents();
        for (RecordComponent component : components) {
            try {
                Object name = component.getName();
                Object value = component.getAccessor().invoke(domainObject);
                if (count < components.length - 1) {
                    sql.append(name + ", ");
                } else {
                    sql.append(name + ")");
                }
                count++;

                parameters.add(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (int i = 0; i < components.length; i++) {
            if (i == 0) {
                sql.append(" VALUES (");
            }
            if (i < components.length - 1) {
                sql.append("?, ");
            } else {
                sql.append("?)");
            }
        }
        return new SqlQuery(sql.toString(), parameters.toArray());
    }

    public Long insertUser(User user) {
        try {
            return executeStatement(getInsertStatement(user, "users"));
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
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(new User(rs.getLong("sin"), rs.getString("name"), rs.getString("address"),
                            rs.getDate("birthdate").toLocalDate(), rs.getString("occupation")));
                }
                return users;
            }
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
        if (filter.user() != null) {
            for (RecordComponent component : filter.user().getClass().getRecordComponents()) {
                try {
                    Object value = component.getAccessor().invoke(filter.user());
                    if (value != null) {
                        sql.append(" AND " + component.getName() + " = ?");
                        parameters.add(value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
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
            List<User> users = executeUserQuery(query);
            if (users.isEmpty()) {
                return null;
            } else {
                return users.get(0);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user", e);
        }
    }

    public List<Listing> executeListingQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                if (query.parameters()[i] instanceof Point2D) {
                    String pointWkt = String.format("POINT(%f %f)", ((Point2D) query.parameters()[i]).getX(), ((Point2D) query.parameters()[i]).getY());
                    stmt.setString(i + 1, pointWkt);
                } else {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Listing> listings = new ArrayList<>();
                while (rs.next()) {
                    String wkt = rs.getString("location_wkt");
                    Matcher matcher = Pattern.compile("POINT\\s*\\(\\s*(-?\\d+\\.?\\d*)\\s+(-?\\d+\\.?\\d*)\\s*\\)").matcher(wkt);
                    Point2D location = null;
                    if (matcher.find()) {
                        double longitude = Double.parseDouble(matcher.group(1));
                        double latitude = Double.parseDouble(matcher.group(2));
                        location = new Point2D.Double(longitude, latitude);
                    }
                    listings.add(new Listing(rs.getLong("listing_id"), rs.getString("listing_type"),
                            rs.getString("address"), rs.getString("postal_code"),
                            location, rs.getString("city"), rs.getString("country"),
                            rs.getLong("users_sin")));
                }
                return listings;
            }
        }
    }


    public Long insertListing(Listing listing) {
        // Not using getInsertStatement because of the location field is not supported by JDBC
        SqlQuery sql = new SqlQuery("INSERT INTO listings (listing_type, address, postal_code, location, city, country, users_sin) " +
                "VALUES (?, ?, ?, ST_GeomFromText(?), ?, ?, ?)",
                listing.listing_type(), listing.address(), listing.postal_code(),
                listing.location(), listing.city(), listing.country(), listing.users_sin());
        try {
            return executeStatement(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting listing", e);
        }
    }

    public boolean listingExists(Listing listing) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE postal_code = ? " +
                "AND city = ? AND country = ? AND address = ?",
                listing.postal_code(), listing.city(), listing.country(), listing.address());
        try {
            return !executeListingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing exists", e);
        }
    }

    public Listing getListingByLocation(String postal_code, String city, String country) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE postal_code = ? AND city = ? AND country = ?",
                postal_code, city, country);
        try {
            List<Listing> listings = executeListingQuery(query);
            if (listings.isEmpty()) {
                return null;
            } else {
                return listings.get(0);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting listing by location", e);
        }
    }

    public List<Listing> getListingsByHostSin(Long host_sin) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE users_sin = ?", host_sin);
        try {
            return executeListingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting listings from host id");
        }
    }

    public boolean listingIdExists(Long listing_id) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE listing_id = ?", listing_id);
        try {
            return !executeListingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing_id exists", e);
        }
    }

    public Listing getListingById(Long listing_id) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE listing_id = ?", listing_id);
        try {
            return executeListingQuery(query).get(0);
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing_id exists", e);
        }
    }

    public Float getListingPricePerNight(Long listing_id) {
        SqlQuery query = new SqlQuery("SELECT price_per_night FROM availabilities INNER JOIN listings ON availabilities.listings_listing_id = listings.listing_id  WHERE listing_id = ?", listing_id);
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            stmt.setObject(1, listing_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("price_per_night");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting listing price per night", e);
        }
    }

    public Float getAverageListingPriceByCity(String city) {
        SqlQuery query = new SqlQuery("SELECT AVG(price_per_night) FROM availabilities INNER JOIN listings ON availabilities.listings_listing_id = listings.listing_id  WHERE city = ?", city);
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            stmt.setObject(1, city);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("AVG(price_per_night)");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
            throw new DataAccessException("Error getting average listing price by city", e);
        }
    }

    public boolean doesCityExists(String city) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE city = ?", city);
        try {
            return !executeListingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if city exists", e);
        }
    }

    public boolean doesListingIdHaveHostSin(Long listing_id, Long host_sin) {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings WHERE listing_id = ? AND users_sin = ?", listing_id, host_sin);

        try {
            return !executeListingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking listing id matches with host sin", e);
        }
    }

    public void deleteListing(Long listing_id) {
        SqlQuery query = new SqlQuery("DELETE FROM listings WHERE listing_id = ?", listing_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting listing", e);
        }
    }

    public List<Listing> getListings() {
        SqlQuery query = new SqlQuery("SELECT *, ST_AsText(location) as location_wkt FROM listings");
        try {
            return executeListingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all listings", e);
        }
    }


    public List<Listing> getListingsByFilter(ListingFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT listings.*, ST_AsText(location) as location_wkt");

        List<Object> parameters = new ArrayList<>();

        if (filter.groupByPriceAscend() || filter.groupByPriceDescend()) {
            sql.append(", availabilities.price_per_night AS price_per_night");
        } else if (filter.listing() != null && filter.listing().location() != null && filter.searchRadius() != null) {
            sql.append(", ST_Distance_Sphere(location, ST_GeomFromText(?)) AS distance");
            parameters.add(filter.listing().location());
        }
        // FROM listings ");
        sql.append(" FROM listings ");

        // join with availabilities table if availability filter is not empty
        if (filter.availability() != null || filter.groupByPriceAscend() || filter.groupByPriceDescend()) {
            sql.append("JOIN availabilities ON listings.listing_id = availabilities.listings_listing_id ");
        }

        // join with amenities table if amenities filter is not empty
        if (filter.amenities() != null && !filter.amenities().isEmpty()) {
            sql.append("JOIN listing_amenities ON listings.listing_id = listing_amenities.listing_id ");
            sql.append("JOIN amenities ON listing_amenities.amenity_id = amenities.amenity_id ");
        }

        sql.append("WHERE 1 = 1"); // This is always true, and allows us to use AND in the following statements

        // filter by listing fields
        if (filter.listing() != null) {
            for (RecordComponent component : filter.listing().getClass().getRecordComponents()) {
                try {
                    Object value = component.getAccessor().invoke(filter.listing());
                    if (value != null) {
                        if (component.getName().equals("listing_type") && filter.listingTypes() != null) {
                            sql.append(" AND listing_type IN (");
                            for (int i = 0; i < filter.listingTypes().size(); i++) {
                                sql.append("?");
                                parameters.add(filter.listingTypes().get(i));
                                if (i < filter.listingTypes().size() - 1) {
                                    sql.append(", ");
                                }
                            }
                            sql.append(")");
                        } else if (component.getName().equals("location") && filter.searchRadius() != null) {
                            // sql.append(" AND ST_Distance_Sphere(location, ST_MakePoint(?, ?)) <= ?");
                            // parameters.add(((Point2D) value).getX());
                            // parameters.add(((Point2D) value).getY());
                            // ST_MakePoint is not supported by this version of mysql. Use ST_GeomFromText instead
                            sql.append(" AND ST_Distance_Sphere(location, ST_GeomFromText(?)) <= ?");
                            parameters.add(value);
                            parameters.add(filter.searchRadius().multiply(BigDecimal.valueOf(100))); // convert km to m since ST_Distance_Sphere returns distance in m
                        } else {
                            sql.append(" AND " + component.getName() + " = ?");
                            parameters.add(value);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // filter by availability fields
        if (filter.availability() != null) {
            for (RecordComponent component : filter.availability().getClass().getRecordComponents()) {
                try {
                    Object value = component.getAccessor().invoke(filter.availability());
                    if (component.getName().equals("start_date") && filter.startDateRange() != null) {
                        sql.append(" AND start_date >= ?");
                        parameters.add(filter.startDateRange());
                    } else if (component.getName().equals("end_date") && filter.endDateRange() != null) {
                        sql.append(" AND end_date <= ?");
                        parameters.add(filter.endDateRange());
                    } else if (component.getName().equals("price_per_night") && filter.minPricePerNight() != null && filter.maxPricePerNight() != null) {
                        sql.append(" AND price_per_night >= ?");
                        parameters.add(filter.minPricePerNight());
                        sql.append(" AND price_per_night <= ?");
                        parameters.add(filter.maxPricePerNight());
                    } else if (component.getName().equals("price_per_night") && filter.minPricePerNight() != null && filter.maxPricePerNight() == null) {
                        sql.append(" AND price_per_night >= ?");
                        parameters.add(filter.minPricePerNight());
                    } else if (component.getName().equals("price_per_night") && filter.minPricePerNight() == null && filter.maxPricePerNight() != null) {
                        sql.append(" AND price_per_night <= ?");
                        parameters.add(filter.maxPricePerNight());
                    } else if (value != null) {
                        sql.append(" AND " + component.getName() + " = ?");
                        parameters.add(value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // first, find all listings that have any of the amenities in the filter
        if (filter.amenities() != null && !filter.amenities().isEmpty()) {
            sql.append(" AND amenities.amenity_name IN (");
            for (int i = 0; i < filter.amenities().size(); i++) {
                sql.append("?");
                if (i < filter.amenities().size() - 1) {
                    sql.append(", ");
                }
                parameters.add(filter.amenities().get(i));
            }
            sql.append(")");
        }
        // then, only select listings that match all the amenities in the filter
        if (filter.amenities() != null && !filter.amenities().isEmpty()) {
            sql.append(" GROUP BY listings.listing_id");
            sql.append(" HAVING COUNT(DISTINCT amenities.amenity_name) = ?");
            parameters.add(filter.amenities().size());
        }

        if (filter.groupByPriceAscend()) {
            sql.append(" ORDER BY price_per_night");
        } else if (filter.groupByPriceDescend()) {
            sql.append(" ORDER BY price_per_night DESC");
        } else if (filter.listing() != null && filter.listing().location() != null && filter.searchRadius() != null) {
            sql.append(" ORDER BY distance");
        }

            SqlQuery query = new SqlQuery(sql.toString(), parameters.toArray());
        try {
            return executeListingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting listings by filter", e);
        }
    }

    private List<Amenity> executeAmenityQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Amenity> amenities = new ArrayList<>();
                while (rs.next()) {
                    amenities.add(new Amenity(rs.getLong("amenity_id"), rs.getString("amenity_name"),
                            rs.getBigDecimal("impact_on_revenue")));
                }
                return amenities;
            }
        }
    }

    public List<String> getAmenitiesByListingId(Long listing_id) {

        SqlQuery query = new SqlQuery("SELECT * FROM amenities " +
                "JOIN listing_amenities ON amenities.amenity_id = listing_amenities.amenity_id " +
                "WHERE listing_amenities.listing_id = ?", listing_id);
        try {
            List<Amenity> amenities = executeAmenityQuery(query);
            List<String> amenityNames = new ArrayList<>();
            for (Amenity amenity : amenities) {
                amenityNames.add(amenity.amenity_name());
            }
            return amenityNames;
        } catch (SQLException e) {
            throw new DataAccessException("Error getting amenities by listing id", e);
        }
    }

    public List<Amenity> getAllAmenities() {
        SqlQuery query = new SqlQuery("SELECT * FROM amenities");
        try {
            return executeAmenityQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all amenities", e);
        }
    }

    public Date getCurrentDate() {
        SqlQuery query = new SqlQuery("SELECT CURRENT_DATE()");
        Connection conn = threadLocalConnection.get();

        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate(1);
                } else {
                    throw new DataAccessException("No date returned");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting date", e);
        }
    }

    public boolean hasFutureBookings(Long listing_id, LocalDate currentDate) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE listings_listing_id = ? " +
                "AND end_date >= ?", listing_id, currentDate);

        try {
            return !executeBookingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error getting bookings with listing id, " + listing_id, e);
        }
    }

    public List<Booking> executeBookingQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rs.next()) {
                    bookings.add(new Booking(rs.getLong("booking_id"), rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(), rs.getDate("transaction_date").toLocalDate(),
                            rs.getBigDecimal("amount"), rs.getString("payment_method"),
                            rs.getLong("card_number"), rs.getLong("tenant_sin"),
                            rs.getLong("listings_listing_id")));
                }
                return bookings;
            }
        }
    }

    public boolean bookingExists(Long booking_id) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE booking_id = ?", booking_id);
        try {
            return !executeBookingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if booking exists", e);
        }
    }

    public boolean bookingExists(Long listing_id, LocalDate start_date, LocalDate end_date) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?", listing_id, start_date, end_date);
        try {
            return !executeBookingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if booking exists", e);
        }
    }

    public void insertCancelledBooking(CancelledBooking cancelledBooking) {
        try {
            executeStatement(getInsertStatement(cancelledBooking, "cancelled_bookings"));
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting cancelled booking", e);
        }
    }

    public List<CancelledBooking> executeCancelledBookingQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<CancelledBooking> cancelledBookings = new ArrayList<>();
                while (rs.next()) {
                    cancelledBookings.add(new CancelledBooking(rs.getLong("cancelled_booking_id"), rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(), rs.getDate("transaction_date").toLocalDate(),
                            rs.getBigDecimal("amount"), rs.getString("payment_method"),
                            rs.getLong("card_number"), rs.getLong("tenant_sin"),
                            rs.getLong("listings_listing_id")));
                }
                return cancelledBookings;
            }
        }
    }

    public List<CancelledBooking> getCancelledBookings() {
        SqlQuery query = new SqlQuery("SELECT * FROM cancelled_bookings");
        try {
            return executeCancelledBookingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all cancelled bookings", e);
        }
    }

    public void updateListingPrice(Long listing_id, BigDecimal newPrice) {
        SqlQuery query = new SqlQuery("UPDATE listings SET price_per_night = ? WHERE listing_id = ?", newPrice,
                listing_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating existing listing price", e);
        }
    }

    public Long insertAvailability(Availability availability) {
        try {
            return executeStatement(getInsertStatement(availability, "availabilities"));
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting availability", e);
        }
    }

    public void deleteAvailability(Long availability_id) {
        SqlQuery query = new SqlQuery("DELETE FROM availabilities WHERE availability_id = ?", availability_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting availability", e);
        }
    }

    public void deleteAvailability(Long listing_id, LocalDate start_date, LocalDate end_date) {
        SqlQuery query = new SqlQuery("DELETE FROM availabilities WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?",
                listing_id, start_date, end_date);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting availability", e);
        }
    }

    private List<Availability> executeAvailabilityQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Availability> availabilities = new ArrayList<>();
                while (rs.next()) {
                    availabilities.add(new Availability(rs.getLong("availability_id"),
                            rs.getDate("start_date").toLocalDate(), rs.getDate("end_date").toLocalDate(),
                            rs.getBigDecimal("price_per_night"), rs.getLong("listings_listing_id")));
                }
                return availabilities;
            }
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

    public List<Availability> getAvailabilitiesOfListing(Long listing_id, LocalDate currentDate) {
//        SqlQuery query = new SqlQuery("SELECT * FROM availabilities WHERE listings_listing_id = ?", listing_id);
        SqlQuery query = new SqlQuery("SELECT * FROM availabilities WHERE listings_listing_id = ? AND start_date >= ?",
                listing_id, currentDate);
        try {
            return executeAvailabilityQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting availabilities of listing", e);
        }
    }

    public Availability getAvailability(Long listing_id, LocalDate start_date, LocalDate end_date) {
        SqlQuery query = new SqlQuery(
                "SELECT * FROM availabilities WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?", listing_id, start_date, end_date);

        try {
            List<Availability> availabilities = executeAvailabilityQuery(query);
            if (availabilities.isEmpty()) {
                return null;
            } else {
                return availabilities.get(0);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting availabilities.", e);
        }
    }

    public Availability getAvailabilityByListingAndEndDate(Long listing_id, LocalDate end_date) {
        SqlQuery query = new SqlQuery(
                "SELECT * FROM availabilities WHERE listings_listing_id = ? AND end_date = ?", listing_id, end_date);

        try {
            List<Availability> availabilities = executeAvailabilityQuery(query);
            if (availabilities.isEmpty()) {
                return null;
            } else {
                return availabilities.get(0);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting availabilities.", e);
        }
    }

    public Availability getAvailabilityByListingAndStartDate(Long listing_id, LocalDate start_date) {
        SqlQuery query = new SqlQuery(
                "SELECT * FROM availabilities WHERE listings_listing_id = ? AND start_date = ?", listing_id, start_date);

        try {
            List<Availability> availabilities = executeAvailabilityQuery(query);
            if (availabilities.isEmpty()) {
                return null;
            } else {
                return availabilities.get(0);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting availabilities.", e);
        }
    }

    public Availability getAffectedAvailability(Long listing_id, LocalDate start_date, LocalDate end_date) {
        SqlQuery query = new SqlQuery(
                "SELECT * FROM availabilities WHERE listings_listing_id = ?", listing_id);

        try {
            List<Availability> availabilities = executeAvailabilityQuery(query);

            for (int i = 0; i < availabilities.size(); i++) {
                // Check if start_date and end_date is within range
                LocalDate availableStartDate = availabilities.get(i).start_date();
                LocalDate availableEndDate = availabilities.get(i).end_date();

                // !isAfter === isEqual and isBefore
                if (!availableStartDate.isAfter(start_date) && !availableEndDate.isBefore(end_date)) {
                    return availabilities.get(i);
                }
            }
            
            throw new DataAccessException(String.format("No availability between %tF to %tF", start_date, end_date));
        } catch (SQLException e) {
            throw new DataAccessException("Error getting availabilities.", e);
        }
    }

    public boolean listingAvailabilityExists(Long listing_id, LocalDate start_date, LocalDate end_date) {
        SqlQuery query = new SqlQuery(
                "SELECT * FROM availabilities WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?", listing_id, start_date, end_date);

        try {
            return !executeAvailabilityQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error getting availabilities.", e);
        }
    }

    public void changeListingAvailability(Long listing_id, LocalDate prevStartDate, LocalDate prevEndDate,
            LocalDate newStartDate, LocalDate newEndDate) {
        SqlQuery query = new SqlQuery(
                "UPDATE availabilities SET start_date = ?, end_date = ? WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?",
                newStartDate, newEndDate, listing_id, prevStartDate, prevEndDate);
        try {
            executeStatement(query);
        } catch (SQLException e) {
           throw new DataAccessException("Error updating listing availabilities.", e);
        }
    }

    public void changeListingAvailabilityAndPrice(Long listing_id, LocalDate prevStartDate, LocalDate prevEndDate,
                                          LocalDate newStartDate, LocalDate newEndDate, BigDecimal newPrice) {
        SqlQuery query = new SqlQuery(
                "UPDATE availabilities SET start_date = ?, end_date = ?, price_per_night = ? WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?",
                newStartDate, newEndDate, newPrice, listing_id, prevStartDate, prevEndDate);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating listing availabilities.", e);
        }
    }

    public void changeListingAvailabilityPrice(Long listing_id, LocalDate start_date, LocalDate end_date, BigDecimal newPrice) {
        SqlQuery query = new SqlQuery(
                "UPDATE availabilities SET price_per_night = ? WHERE listings_listing_id = ? AND start_date = ? AND end_date = ?",
                newPrice, listing_id, start_date, end_date);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating listing availabilities.", e);
        }
    }

    private List<Amenity> executeListingAmenityQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Amenity> amenities = new ArrayList<>();
                while (rs.next()) {
                    amenities.add(new Amenity(rs.getLong("amenity_id"), rs.getString("amenity_name"),
                            rs.getBigDecimal("impact_on_revenue")));
                }
                return amenities;
            }
        }
    }

    public void insertAmenityForListing(Long listing_id, String amenityName) {
        SqlQuery query = new SqlQuery("INSERT INTO listing_amenities (listing_id, amenity_id) " +
                "VALUES (?, (SELECT amenity_id FROM amenities WHERE amenity_name = ?))", listing_id, amenityName);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting amenity for listing", e);
        }
    }

    public void deleteAmenityForListing(Long listing_id, String amenityName) {
        SqlQuery query = new SqlQuery("DELETE FROM listing_amenities WHERE listing_id = ? AND amenity_id = " +
                "(SELECT amenity_id FROM amenities WHERE amenity_name = ?)", listing_id, amenityName);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting amenity for listing", e);
        }
    }

    public boolean listingHasAmenity(Long listing_id, String amenityName) {
        SqlQuery query = new SqlQuery("SELECT amenities.* FROM amenities " +
                "JOIN listing_amenities ON amenities.amenity_id = listing_amenities.amenity_id " +
                "WHERE listing_amenities.listing_id = ? AND amenities.amenity_name = ?", listing_id, amenityName);
        try {
            return !executeAmenityQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing has amenity", e);
        }
    }


    public Long insertBooking(Booking booking) {
        try {
            return executeStatement(getInsertStatement(booking, "bookings"));
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

    public Booking getBooking(Long booking_id) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE booking_id = ?", booking_id);
        try {
            List<Booking> bookings = executeBookingQuery(query);
            if (bookings.isEmpty()) {
                return null;
            } else {
                return bookings.get(0);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting booking", e);
        }
    }

    public List<Booking> getBookingsOfListing(Long listing_id) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE listings_listing_id = ?", listing_id);
        try {
            return executeBookingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting bookings of listing", e);
        }
    }

    public List<Booking> getTenenatBookings(Long tenant_sin) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE tenant_sin = ?", tenant_sin);
        try {
            return executeBookingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting tenant bookings", e);
        }
    }

    public List<Booking> getHostBookings(Long host_sin) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE listings_listing_id IN " +
                "(SELECT listing_id FROM listings WHERE users_sin = ?)", host_sin);
        try {
            return executeBookingQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting host bookings", e);
        }
    }

    public boolean tenantSinMatchesBookingId(Long tenant_sin, Long booking_id) {
        SqlQuery query = new SqlQuery("SELECT * FROM bookings WHERE tenant_sin = ? AND booking_id = ?", tenant_sin, booking_id);

        try {
            return !executeBookingQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if tenant sin matche to booking", e);
        }
    }

    public boolean hostSinMatchesBookingId(Long host_sin, Long booking_id) {
        // Get all the listings id in Listing table where host_sin = host_sin
        List<Listing> listings = getListingsByHostSin(host_sin);
        // Get listing id from Bookings table with booking id == booking id
        Long bookings_listing_id = getBooking(booking_id).listings_listing_id();

        if (listings.isEmpty()) {
            throw new DataAccessException("Host does not have any listings");
        }

        for (int i = 0; i < listings.size(); i++) {
            if (bookings_listing_id.equals(listings.get(i).listing_id()))
                return true;
        }

        return false;
    }

    public void insertReview(Review review) {
        try {
            executeStatement(getInsertStatement(review, "reviews"));
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting review", e);
        }
    }

    public void deleteReview(Long reviewId) {
        SqlQuery query = new SqlQuery("DELETE FROM reviews WHERE review_id = ?", reviewId);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting review", e);
        }
    }

    public boolean reviewExists(Long bookingId) {
        SqlQuery query = new SqlQuery("SELECT * FROM reviews WHERE booking_id = ?", bookingId);

        try {
            return !executeReviewQuery(query).isEmpty();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if review exists for booking", e);
        }
    }

    private List<Review> executeReviewQuery(SqlQuery query) throws SQLException {
        Connection conn = threadLocalConnection.get();
        try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
            for (int i = 0; i < query.parameters().length; i++) {
                stmt.setObject(i + 1, query.parameters()[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (rs.next()) {
                    // the follow handle the case where JDBC's getInt returns 0 for null
                    Integer ratingOfListing = rs.getInt("rating_of_listing");
                    if (rs.wasNull()) {
                        ratingOfListing = null;
                    }
                    Integer ratingOfHost = rs.getInt("rating_of_host");
                    if (rs.wasNull()) {
                        ratingOfHost = null;
                    }
                    Integer ratingOfRenter = rs.getInt("rating_of_tenant");
                    if (rs.wasNull()) {
                        ratingOfRenter = null;
                    }
                    reviews.add(new Review(rs.getLong("review_id"), ratingOfListing, ratingOfHost,
                            ratingOfRenter, rs.getString("comment_from_tenant"),
                            rs.getString("comment_from_host"), rs.getLong("bookings_booking_id")));
                }

                return reviews;
            }
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

    public List<Review> getReviewsAsTenant(Long tenantId) {
        SqlQuery query = new SqlQuery("SELECT * FROM reviews WHERE bookings_booking_id IN " +
                "(SELECT booking_id FROM bookings WHERE tenant_sin = ?)", tenantId);
        try {
            return executeReviewQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting reviews as tenant", e);
        }
    }

    public List<Review> getReviewsAsHost(Long hostId) {
        SqlQuery query = new SqlQuery("SELECT * FROM reviews WHERE bookings_booking_id IN " +
                "(SELECT booking_id FROM bookings WHERE listings_listing_id IN " +
                "(SELECT listing_id FROM listings WHERE users_sin = ?))", hostId);

        try {
            return executeReviewQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting reviews as host", e);
        }
    }

    public List<Review> getReviewsOfListing(Long listing_id) {
        SqlQuery query = new SqlQuery("SELECT * FROM reviews " +
                "JOIN bookings ON reviews.bookings_booking_id = bookings.booking_id " +
                "WHERE bookings.listings_listing_id = ?", listing_id);
        try {
            return executeReviewQuery(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting reviews by listing", e);
        }
    }

    public void tenantRateListing(Long tenant_id, Integer rating, Long booking_id) {
        SqlQuery query = new SqlQuery("UPDATE reviews SET rating_of_listing = ? WHERE bookings_booking_id = ?", rating, booking_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating rating of listing", e);
        }
    }

    public void tenantRateHost(Long tenant_id, Integer rating, Long booking_id) {
        SqlQuery query = new SqlQuery("UPDATE reviews SET rating_of_host = ? WHERE bookings_booking_id = ?", rating, booking_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating rating of host", e);
        }
    }

    public void addCommentFromTenant(Long tenant_id, String comment, Long booking_id) {
        SqlQuery query = new SqlQuery("UPDATE reviews SET comment_from_tenant = ? WHERE bookings_booking_id = ?", comment, booking_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating rating of host", e);
        }
    }

    public void deleteCommentFromTenant(Long tenant_id, Long booking_id) {
        SqlQuery query = new SqlQuery("UPDATE reviews SET comment_from_tenant = NULL WHERE bookings_booking_id = ?", booking_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating rating of host", e);
        }
    }

    public void hostRateTenant(Long host_id, Integer rating, Long booking_id) {
        SqlQuery query = new SqlQuery("UPDATE reviews SET rating_of_tenant = ? WHERE bookings_booking_id = ?", rating, booking_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating rating of host", e);
        }
    }

    public void addCommentFromHost(Long host_id, String comment, Long booking_id) {
        SqlQuery query = new SqlQuery("UPDATE reviews SET comment_from_host = ? WHERE bookings_booking_id = ?", comment, booking_id);
        try {
            executeStatement(query);
        } catch (SQLException e) {
            throw new DataAccessException("Error updating rating of host", e);
        }
    }

    public Map<String, Long> getNumberOfBookingsInDateRangePerCity(LocalDate startDate, LocalDate endDate) {
        SqlQuery query = new SqlQuery("SELECT city, COUNT(*) FROM bookings " +
                "JOIN listings ON bookings.listings_listing_id = listings.listing_id " +
                "WHERE start_date >= ? AND end_date <= ? " +
                "GROUP BY city", startDate, endDate);
        try {
            Connection conn = threadLocalConnection.get();
            try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
                for (int i = 0; i < query.parameters().length; i++) {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    Map<String, Long> bookingsByCity = new HashMap<>();
                    while (rs.next()) {
                        bookingsByCity.put(rs.getString("city"), rs.getLong("COUNT(*)"));
                    }
                    return bookingsByCity;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting number of bookings by date range and city", e);
        }
    }

    public Map<String, Map<String, Long>> getNumberOfBookingsInDateRangePerPostalCodePerCity (LocalDate startDate, LocalDate endDate) {
        SqlQuery query = new SqlQuery("SELECT city, postal_code, COUNT(*) FROM bookings " +
                "JOIN listings ON bookings.listings_listing_id = listings.listing_id " +
                "WHERE start_date >= ? AND end_date <= ? " +
                "GROUP BY city, postal_code", startDate, endDate);
        try {
            Connection conn = threadLocalConnection.get();
            try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
                for (int i = 0; i < query.parameters().length; i++) {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    Map<String, Map<String, Long>> bookingsByCityAndPostalCode = new HashMap<>();
                    while (rs.next()) {
                        String city = rs.getString("city");
                        String postalCode = rs.getString("postal_code");
                        Long count = rs.getLong("COUNT(*)");
                        if (bookingsByCityAndPostalCode.containsKey(city)) {
                            bookingsByCityAndPostalCode.get(city).put(postalCode, count);
                        } else {
                            HashMap<String, Long> postalCodeCount = new HashMap<>();
                            postalCodeCount.put(postalCode, count);
                            bookingsByCityAndPostalCode.put(city, postalCodeCount);
                        }
                    }
                    return bookingsByCityAndPostalCode;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting number of bookings by date range and city", e);
        }
    }


    public Map<User, Long> getNumberOfBookingsInDateRangePerRenter(LocalDate startDate, LocalDate endDate) {
        SqlQuery query = new SqlQuery("SELECT users.*, COUNT(*) FROM bookings " +
                "JOIN users ON bookings.tenant_sin = users.sin " +
                "WHERE start_date >= ? AND end_date <= ? " +
                "GROUP BY bookings.tenant_sin " +
                "ORDER by COUNT(*) DESC",
                startDate, endDate);
        try {
            List<User> users = executeUserQuery(query);
            Connection conn = threadLocalConnection.get();
            try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
                for (int i = 0; i < query.parameters().length; i++) {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    // Use ordered map to preserve order of results
                    Map<User, Long> bookingsByRenter = new LinkedHashMap<>();
                    int counter = 0;
                    while (rs.next() && counter < users.size()) {
                        bookingsByRenter.put(users.get(counter++), rs.getLong("COUNT(*)"));
                    }
                    return bookingsByRenter;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting number of bookings by date range and renter", e);
        }
    }

    // Map<City, Map<Renter, Count>>
    public Map<String, Map<User, Long>> getNumberOfBookingsInDateRangePerRenterPerCity(LocalDate startDate, LocalDate endDate) {
        SqlQuery query = new SqlQuery("SELECT city, users.*, COUNT(*) FROM bookings " +
                "JOIN listings ON bookings.listings_listing_id = listings.listing_id " +
                "JOIN users ON bookings.tenant_sin = users.sin " +
                "WHERE start_date >= ? AND end_date <= ? " +
                "GROUP BY city, bookings.tenant_sin " +
                "ORDER by COUNT(*) DESC",
                startDate, endDate);
        try {
            List<User> users = executeUserQuery(query);
            Connection conn = threadLocalConnection.get();

            try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
                for (int i = 0; i < query.parameters().length; i++) {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    // Use ordered map to preserve order of results
                    Map<String, Map<User, Long>> bookingsByRenterByCity = new LinkedHashMap<>();
                    int counter = 0;
                    while (rs.next() && counter < users.size()) {
                        String city = rs.getString("city");
                        User renter = users.get(counter++);
                        Long count = rs.getLong("COUNT(*)");
                        if (bookingsByRenterByCity.containsKey(city)) {
                            bookingsByRenterByCity.get(city).put(renter, count);
                        } else {
                            HashMap<User, Long> renterCount = new HashMap<>();
                            renterCount.put(renter, count);
                            bookingsByRenterByCity.put(city, renterCount);
                        }
                    }
                    return bookingsByRenterByCity;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting number of bookings by date range and renter", e);
        }
    }

    // Map<renter, number of cancelled bookings within a range>, ordered by number of cancelled bookings
    public Map<User, Long> getNumberOfCancelledBookingsInDateRangePerRenter(LocalDate startDate, LocalDate endDate) {
        SqlQuery query = new SqlQuery("SELECT users.*, COUNT(*) FROM cancelled_bookings " +
                "JOIN users ON cancelled_bookings.tenant_sin = users.sin " +
                "WHERE start_date >= ? AND end_date <= ? " +
                "GROUP BY cancelled_bookings.tenant_sin " +
                "ORDER by COUNT(*) DESC",
                startDate, endDate);
        try {
            List<User> users = executeUserQuery(query);
            Connection conn = threadLocalConnection.get();
            try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
                for (int i = 0; i < query.parameters().length; i++) {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    // Use ordered map to preserve order of results
                    Map<User, Long> bookingsByRenter = new LinkedHashMap<>();
                    int counter = 0;
                    while (rs.next() && counter < users.size()) {
                        bookingsByRenter.put(users.get(counter++), rs.getLong("COUNT(*)"));
                    }
                    return bookingsByRenter;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting number of cancelled bookings by date range and renter", e);
        }
    }

    public Map<User, Long> getNumberOfCancelledBookingsInDateRangePerHost(LocalDate startDate, LocalDate endDate) {
        SqlQuery query = new SqlQuery("SELECT users.*, COUNT(*) FROM cancelled_bookings " +
                "JOIN listings ON cancelled_bookings.listings_listing_id = listings.listing_id " +
                "JOIN users ON listings.users_sin = users.sin " +
                "WHERE start_date >= ? AND end_date <= ? " +
                "GROUP BY listings.users_sin " +
                "ORDER by COUNT(*) DESC",
                startDate, endDate);
        try {
            List<User> users = executeUserQuery(query);
            Connection conn = threadLocalConnection.get();
            try (PreparedStatement stmt = conn.prepareStatement(query.sql())) {
                for (int i = 0; i < query.parameters().length; i++) {
                    stmt.setObject(i + 1, query.parameters()[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    // Use ordered map to preserve order of results
                    Map<User, Long> bookingsByHost = new LinkedHashMap<>();
                    int counter = 0;
                    while (rs.next() && counter < users.size()) {
                        bookingsByHost.put(users.get(counter++), rs.getLong("COUNT(*)"));
                    }
                    return bookingsByHost;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting number of cancelled bookings by date range and host", e);
        }
    }

}
