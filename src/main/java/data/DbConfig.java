package data;

import domain.Amenity;
import exception.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbConfig {
    private final String url;
    private final String username;
    private final String password;
    private final List<String> tables = Arrays.asList(
            "listing_amenities",
            "amenities",
            "availabilities",
            "reviews",
            "bookings",
            "cancelled_bookings",
            "listings",
            "users");

    public final List<String> amenities = Arrays.asList(
            "wifi",
            "washer",
            "air conditioning",
            "dedicated workspace",
            "hair dryer",
            "kitchen",
            "dryer",
            "heating",
            "tv",
            "iron",
            "pool",
            "free parking",
            "crib",
            "bbq grill",
            "indoor fireplace",
            "hot tub",
            "ev charger",
            "gym",
            "breakfast",
            "smoking allowed",
            "beachfront",
            "ski-in/ski-out", "waterfront",
            "smoke alarm",
            "carbon monoxide alarm");

    public DbConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // For testing purposes
    public DbConfig() {
        this.url = "jdbc:mysql://localhost:3307/mydb";
        this.username = "root";
        this.password = "";
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
                    "price_per_night decimal(10,2)  NOT NULL," +
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
                    "address varchar(30)  NOT NULL," +
                    "postal_code varchar(12)  NOT NULL," +
                    "longitude decimal(9,6)  NOT NULL," +
                    "latitude decimal(9,6)  NOT NULL," +
                    "city varchar(20)  NOT NULL," +
                    "country varchar(20)  NOT NULL," +
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
                    "payment_method varchar(20)  NOT NULL," +
                    "card_number BIGINT NOT NULL," +
                    "tenant_sin BIGINT  NOT NULL," +
                    "listings_listing_id BIGINT UNSIGNED NOT NULL," +
                    "PRIMARY KEY (booking_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // cancelled bookings table
            createTableSql = "CREATE TABLE cancelled_bookings (" +
                    "cancelled_booking_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "start_date date  NOT NULL," +
                    "end_date date  NOT NULL," +
                    "transaction_date date  NOT NULL," +
                    "amount decimal(10,2)  NOT NULL," +
                    "payment_method varchar(20)  NOT NULL," +
                    "card_number BIGINT NOT NULL," +
                    "tenant_sin BIGINT  NOT NULL," +
                    "listings_listing_id BIGINT UNSIGNED NOT NULL," +
                    "PRIMARY KEY (cancelled_booking_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // reviews table
            createTableSql = "CREATE TABLE reviews (" +
                    "review_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "rating_of_listing int  NULL," +
                    "rating_of_host int  NULL," +
                    "rating_of_tenant int  NULL," +
                    "comment_from_tenant varchar(500)  NULL," +
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
                    "address varchar(30)  NULL," +
                    "birthdate date  NOT NULL," +
                    "occupation varchar(20)  NULL," +
                    "PRIMARY KEY (sin)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // amenities table
            createTableSql = "CREATE TABLE amenities (" +
                    "amenity_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "amenity_name varchar(50)  NOT NULL UNIQUE," +
                    "PRIMARY KEY (amenity_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // listing_amenities table
            createTableSql = "CREATE TABLE listing_amenities (" +
                    "listing_id BIGINT UNSIGNED NOT NULL," +
                    "amenity_id BIGINT UNSIGNED NOT NULL," +
                    "PRIMARY KEY (listing_id, amenity_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // add foreign key constraints
            String addForeignKeySql;

            addForeignKeySql = "ALTER TABLE availabilities " +
                    "ADD CONSTRAINT availability_listings " +
                    "FOREIGN KEY (listings_listing_id) " +
                    "REFERENCES listings (listing_id)" +
                    "ON DELETE CASCADE;";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE bookings " +
                    "ADD CONSTRAINT bookings_users " +
                    "FOREIGN KEY (tenant_sin) " +
                    "REFERENCES users (sin);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE cancelled_bookings " +
                    "ADD CONSTRAINT cancelled_bookings_users " +
                    "FOREIGN KEY (tenant_sin) " +
                    "REFERENCES users (sin);";

            addForeignKeySql = "ALTER TABLE reviews " +
                    "ADD CONSTRAINT reviews_bookings " +
                    "FOREIGN KEY (bookings_booking_id) " +
                    "REFERENCES bookings (booking_id)" +
                    "ON DELETE CASCADE;";
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

            addForeignKeySql = "ALTER TABLE cancelled_bookings " +
                    "ADD CONSTRAINT cancelled_bookings_listings " +
                    "FOREIGN KEY (listings_listing_id) " +
                    "REFERENCES listings (listing_id);";

            addForeignKeySql = "ALTER TABLE listings " +
                    "ADD CONSTRAINT users_listings " +
                    "FOREIGN KEY (users_sin) " +
                    "REFERENCES users (sin);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE listing_amenities " +
                    "ADD CONSTRAINT listing_amenities_listings " +
                    "FOREIGN KEY (listing_id) " +
                    "REFERENCES listings (listing_id)" +
                    "ON DELETE CASCADE;";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE listing_amenities " +
                    "ADD CONSTRAINT listing_amenities_amenities " +
                    "FOREIGN KEY (amenity_id) " +
                    "REFERENCES amenities (amenity_id);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            // insert amenities
            String insertAmenitySql = "INSERT INTO amenities (amenity_name) VALUES (?);";
            stmt = conn.prepareStatement(insertAmenitySql);
            for (String amenity : amenities) {
                System.out.println(amenity);
                stmt.setString(1, amenity);
                stmt.executeUpdate();
            }
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

    public List<String> getOriginalTables() {
        return new ArrayList<>(tables);
    }

    public List<String> getTables() {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData metaData = connection.getMetaData();
            String schema = connection.getSchema();
            ResultSet rs = metaData.getTables(null, schema, "%", new String[] { "TABLE" });
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (!tableName.equals("sys_config")) {
                    tables.add(tableName);
                }
            }
            return tables;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving table list", e);
        }
    }

    public void resetTables() {
        dropTables();
        createTables();
    }

    public List<Amenity> getAmenities() {
        String sql = "SELECT * FROM amenities";
        try (Connection conn = DriverManager.getConnection(url, username, password);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<Amenity> amenities = new ArrayList<>();
            while (rs.next()) {
                amenities.add(new Amenity(rs.getLong("amenity_id"), rs.getString("amenity_name")));
            }
            return amenities;
        } catch (SQLException e) {
            throw new DataAccessException("Error getting amenities", e);
        }
    }

}
