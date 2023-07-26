package data;

import domain.Amenity;
import domain.Listing;
import domain.User;
import exception.DataAccessException;
import filter.UserFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dao {
    private final String url;
    private final String username;
    private final String password;
    private List<String> tables = Arrays.asList(
        "listing_amenities",
        "amenities",
        "availabilities",
        "reviews",
        "bookings",
        "listings",
        "users"
    );

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
                    "postal_code char(12)  NOT NULL," +
                    "longitude decimal(9,6)  NOT NULL," +
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

            addForeignKeySql = "ALTER TABLE listing_amenities " +
                    "ADD CONSTRAINT listing_amenities_listings " +
                    "FOREIGN KEY (listing_id) " +
                    "REFERENCES listings (listing_id);";
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
            List<String> amenities = Arrays.asList("wifi", "tv", "kitchen", "parking", "elevator", "gym", "hot tub", "pool", "washer", "dryer");
            String insertAmenitySql = "INSERT INTO amenities (amenity_name) VALUES (?);";
            stmt = conn.prepareStatement(insertAmenitySql);
            for (String amenity : amenities) {
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
            ResultSet rs = metaData.getTables(null, schema, "%", new String[]{"TABLE"});
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
        SqlQuery query = new SqlQuery("INSERT INTO users (sin, name, address, birthdate, occupation) VALUES (?, ?, ?, ?, ?)",
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

    private List<User> executeUserQuery(SqlQuery query) throws SQLException{
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
        if (filter.sin() != null) {
            sql.append(" AND sin = ?");
            parameters.add(filter.sin());
        }
        if (filter.name() != null) {
            sql.append(" AND name = ?");
            parameters.add(filter.name());
        }
        if (filter.address() != null) {
            sql.append(" AND address = ?");
            parameters.add(filter.address());
        }
        if (filter.birthdate() != null) {
            sql.append(" AND birthdate = ?");
            parameters.add(filter.birthdate());
        }
        if (filter.occupation() != null) {
            sql.append(" AND occupation = ?");
            parameters.add(filter.occupation());
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
                listings.add(new Listing(rs.getLong("listing_id"), rs.getString("listing_type"), rs.getBigDecimal("price_per_night"),
                        rs.getString("postal_code"), rs.getBigDecimal("longitude"), rs.getBigDecimal("latitude"),
                        rs.getString("city"), rs.getString("country"), rs.getString("amenities"), rs.getLong("users_sin")));
            }
            return listings;
        }
    }

    public void insertListing(Listing listing) {
        // Note: we disregard the `listing_id` column as it is an auto-increment column whose value is automatically generated.
        SqlQuery query = new SqlQuery("INSERT INTO listings (listing_type, price_per_night, postal_code, longitude," +
                "latitude, city, country, amenities, users_sin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                listing.listing_type(), listing.price_per_night(), listing.postal_code(), listing.longitude(),
                listing.latitude(), listing.city(), listing.country(), listing.amenities(), listing.users_sin());
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
            throw new DataAccessException("Error checking if listing exists", e);
        }
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
