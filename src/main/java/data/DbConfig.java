package data;

import domain.Amenity;
import exception.DataAccessException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

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

    private Map<String, BigDecimal> amenityImpactMap = new HashMap<>();

    public DbConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        initializeAmenities();
    }

    // For testing purposes
    public DbConfig() {
        this.url = "jdbc:mysql://localhost:3307/mydb";
        this.username = "root";
        this.password = "";
        initializeAmenities();
    }

    private void initializeAmenities() {
        amenityImpactMap.put("wifi", BigDecimal.valueOf(3.0));
        amenityImpactMap.put("washer", BigDecimal.valueOf(2.0));
        amenityImpactMap.put("air conditioning", BigDecimal.valueOf(5.0));
        amenityImpactMap.put("dedicated workspace", BigDecimal.valueOf(4.0));
        amenityImpactMap.put("hair dryer", BigDecimal.valueOf(1.0));
        amenityImpactMap.put("kitchen", BigDecimal.valueOf(6.0));
        amenityImpactMap.put("dryer", BigDecimal.valueOf(2.0));
        amenityImpactMap.put("heating", BigDecimal.valueOf(4.0));
        amenityImpactMap.put("tv", BigDecimal.valueOf(2.0));
        amenityImpactMap.put("iron", BigDecimal.valueOf(1.0));
        amenityImpactMap.put("pool", BigDecimal.valueOf(10.0));
        amenityImpactMap.put("free parking", BigDecimal.valueOf(3.0));
        amenityImpactMap.put("crib", BigDecimal.valueOf(1.5));
        amenityImpactMap.put("bbq grill", BigDecimal.valueOf(2.5));
        amenityImpactMap.put("indoor fireplace", BigDecimal.valueOf(3.0));
        amenityImpactMap.put("hot tub", BigDecimal.valueOf(7.0));
        amenityImpactMap.put("ev charger", BigDecimal.valueOf(2.0));
        amenityImpactMap.put("gym", BigDecimal.valueOf(5.0));
        amenityImpactMap.put("breakfast", BigDecimal.valueOf(3.5));
        amenityImpactMap.put("smoking allowed", BigDecimal.valueOf(-1.0)); // Can be negative for some guests
        amenityImpactMap.put("beachfront", BigDecimal.valueOf(15.0));
        amenityImpactMap.put("ski-in/ski-out", BigDecimal.valueOf(8.0));
        amenityImpactMap.put("waterfront", BigDecimal.valueOf(12.0));
        amenityImpactMap.put("smoke alarm", BigDecimal.valueOf(0.5));
        amenityImpactMap.put("carbon monoxide alarm", BigDecimal.valueOf(0.5));
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
                    "address varchar(100)  NOT NULL," +
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
                    "address varchar(100)  NULL," +
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
                    "impact_on_revenue decimal(10,2)  NULL," +
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

            // Note: we chose to not enforce foreign key constraints on the listings table
            // because we want booking tables to remain after a listing is deleted
//            addForeignKeySql = "ALTER TABLE bookings " +
//                    "ADD CONSTRAINT user_bookings_listings " +
//                    "FOREIGN KEY (listings_listing_id) " +
//                    "REFERENCES listings (listing_id);";
//            stmt = conn.prepareStatement(addForeignKeySql);
//            stmt.executeUpdate();
//            stmt.close();

//            addForeignKeySql = "ALTER TABLE cancelled_bookings " +
//                    "ADD CONSTRAINT cancelled_bookings_listings " +
//                    "FOREIGN KEY (listings_listing_id) " +
//                    "REFERENCES listings (listing_id);";

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
            String insertAmenitySql = "INSERT INTO amenities (amenity_name, impact_on_revenue) VALUES (?, ?);";
            stmt = conn.prepareStatement(insertAmenitySql);
            for (var entry : amenityImpactMap.entrySet()) {
                stmt.setString(1, entry.getKey());
                stmt.setBigDecimal(2, entry.getValue());
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

}
