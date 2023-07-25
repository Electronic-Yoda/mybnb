package data;

import domain.Listing;
import domain.User;
import exceptions.DataAccessException;

import java.sql.*;
import java.util.ArrayList;

public class Dao {
    private final String url = "jdbc:mysql://localhost/mydb";
    private final String username = "root";
    private final String password = "";
    private ArrayList<String> tables;

    public Dao() {
        tables = new ArrayList<>();

        // Note: The order of the tables in the ArrayList is important for the dropTables() method.
        // The tables must be dropped in the reverse order of their dependencies.
        // If one table has a foreign key reference to another, first drop the table that has the foreign key reference,
        // and then drop the table that is being referenced.
        tables.add("availability"); // old name for availabilities table
        tables.add("availabilities");
        tables.add("reviews");
        tables.add("rentings"); // old name for bookings table
        tables.add("bookings");
        tables.add("listings");
        tables.add("users");
    }
    public void createTables() {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String createTableSql;
            PreparedStatement stmt;

            // availability table
            createTableSql = "CREATE TABLE availabilities (" +
                    "availability_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "start_date date  NOT NULL," +
                    "end_date date  NOT NULL," +
                    "listings_listing_id BIGINT UNSIGNED NOT NULL," +
                    "PRIMARY KEY (availability_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();


            // listings table
            createTableSql = "CREATE TABLE listings (" +
                    "listing_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "listing_type char(10)  NOT NULL," +
                    "price_per_night decimal(10,2)  NOT NULL," +
                    "longitude decimal(9,6)  NOT NULL," +
                    "postal_code char(12)  NOT NULL," +
                    "latitude decimal(9,6)  NOT NULL," +
                    "city varchar(20)  NOT NULL," +
                    "country varchar(20)  NOT NULL," +
                    "amenities varchar(100)  NOT NULL," +
                    "users_sin BIGINT  NOT NULL," +
                    "PRIMARY KEY (listing_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // bookings table
            createTableSql = "CREATE TABLE bookings (" +
                    "booking_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "start_date date  NOT NULL," +
                    "end_date date  NOT NULL," +
                    "transaction_date date  NOT NULL," +
                    "amount decimal(10,2)  NOT NULL," +
                    "currency varchar(3)  NOT NULL," +
                    "payment_method varchar(20)  NOT NULL," +
                    "users_sin BIGINT  NOT NULL," +
                    "listings_listing_id BIGINT UNSIGNED NOT NULL," +
                    "PRIMARY KEY (booking_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // reviews table
            createTableSql = "CREATE TABLE reviews (" +
                    "review_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "rating_of_listing int  NULL," +
                    "rating_of_host int  NULL," +
                    "rating_of_renter int  NULL," +
                    "comment_from_renter varchar(500)  NULL," +
                    "comment_from_host varchar(500)  NULL," +
                    "bookings_booking_id BIGINT UNSIGNED NOT NULL UNIQUE," +
                    "PRIMARY KEY (review_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // users table
            createTableSql = "CREATE TABLE users (" +
                    "sin BIGINT NOT NULL," +
                    "name varchar(20)  NOT NULL," +
                    "address varchar(30)  NOT NULL," +
                    "birthdate date  NOT NULL," +
                    "occupation varchar(20)  NOT NULL," +
                    "PRIMARY KEY (sin)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // add foreign key constraints
            String addForeignKeySql;

            addForeignKeySql = "ALTER TABLE availabilities " +
                    "ADD CONSTRAINT availability_listings " +
                    "FOREIGN KEY (listings_listing_id) " +
                    "REFERENCES listings (listing_id);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE bookings " +
                    "ADD CONSTRAINT bookings_users " +
                    "FOREIGN KEY (users_sin) " +
                    "REFERENCES users (sin);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE reviews " +
                    "ADD CONSTRAINT reviews_bookings " +
                    "FOREIGN KEY (bookings_booking_id) " +
                    "REFERENCES bookings (booking_id);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE bookings " +
                    "ADD CONSTRAINT user_bookings_listings " +
                    "FOREIGN KEY (listings_listing_id) " +
                    "REFERENCES listings (listing_id);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE listings " +
                    "ADD CONSTRAINT users_listings " +
                    "FOREIGN KEY (users_sin) " +
                    "REFERENCES users (sin);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables", e);
        }
    }

    public void dropTables() {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            for (String table : tables) {
                try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE IF EXISTS " + table)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting tables", e);
        }
    }

    public void insertUser(User user) {
        String sql = "INSERT INTO users (sin, name, address, birthdate, occupation) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.sin());
            stmt.setString(2, user.name());
            stmt.setString(3, user.address());
            stmt.setDate(4, Date.valueOf(user.birthdate()));
            stmt.setString(5, user.occupation());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user", e);
        }
    }

    public void deleteUser(Long sin) {
        String sql = "DELETE FROM users WHERE sin=?";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sin);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user", e);
        }
    }

    public boolean userExists(Long sin) {
        String sql = "SELECT * FROM users WHERE sin=?";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sin);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking user exists", e);
        }
    }

    public long insertListing(Listing listing) {
        // Note: we disregard the `listing_id` column as it is an auto-increment column whose value is automatically generated.
        String sql = "INSERT INTO listings (listing_type, price_per_night, longitude, postal_code," +
                "latitude, city, country, amenities, user) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Here, set the parameters for the PreparedStatement.
            // Note that the index starts from 1.
            stmt.setString(1, listing.listing_type());
            stmt.setBigDecimal(2, listing.price_per_night());
            stmt.setBigDecimal(3, listing.longitude());
            stmt.setString(4, listing.postal_code());
            stmt.setBigDecimal(5, listing.latitude());
            stmt.setString(6, listing.city());
            stmt.setString(7, listing.country());
            stmt.setString(8, listing.amenities());
            stmt.setLong(9, listing.users_sin());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new DataAccessException("Creating listing failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new DataAccessException("Creating listing failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting listing", e);
        }
    }

    public boolean listingExists(Listing listing) {
        String sql = "SELECT * FROM listings WHERE postal_code = ? AND city = ? AND country = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, listing.postal_code());
            stmt.setString(2, listing.city());
            stmt.setString(3, listing.country());

            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if listing exists", e);
        }
    }

}
