package mybnb;
import java.sql.*;
import java.util.ArrayList;

public class Dao {
    private final String url = "jdbc:mysql://localhost/mydb";
    private final String user = "root";
    private final String password = "";
    private ArrayList<String> tables;

    public Dao() {
        tables = new ArrayList<>();

        // Note: The order of the tables in the ArrayList is important for the dropTables() method.
        // The tables must be dropped in the reverse order of their dependencies.
        tables.add("reviews");
        tables.add("rentings");
        tables.add("availabilities");
        tables.add("listings");
        tables.add("users");
    }
    public void createTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
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
                    "longitude decimal(9,6)  NOT NULL," +
                    "postal_code char(12)  NOT NULL," +
                    "latitude decimal(9,6)  NOT NULL," +
                    "city varchar(20)  NOT NULL," +
                    "country varchar(20)  NOT NULL," +
                    "amenities varchar(100)  NOT NULL," +
                    "users_sin int  NOT NULL," +
                    "PRIMARY KEY (listing_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // rentings table
            createTableSql = "CREATE TABLE rentings (" +
                    "renting_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE," +
                    "start_date date  NOT NULL," +
                    "end_date date  NOT NULL," +
                    "transaction_date date  NOT NULL," +
                    "amount decimal(10,2)  NOT NULL," +
                    "currency varchar(3)  NOT NULL," +
                    "payment_method varchar(20)  NOT NULL," +
                    "users_sin int  NOT NULL," +
                    "listings_listing_id BIGINT UNSIGNED NOT NULL," +
                    "PRIMARY KEY (renting_id)" +
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
                    "rentings_renting_id BIGINT UNSIGNED NOT NULL UNIQUE," +
                    "PRIMARY KEY (review_id)" +
                    ");";
            stmt = conn.prepareStatement(createTableSql);
            stmt.executeUpdate();
            stmt.close();

            // users table
            createTableSql = "CREATE TABLE users (" +
                    "sin int NOT NULL," +
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

            addForeignKeySql = "ALTER TABLE rentings " +
                    "ADD CONSTRAINT rentings_users " +
                    "FOREIGN KEY (users_sin) " +
                    "REFERENCES users (sin);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE reviews " +
                    "ADD CONSTRAINT reviews_rentings " +
                    "FOREIGN KEY (rentings_renting_id) " +
                    "REFERENCES rentings (renting_id);";
            stmt = conn.prepareStatement(addForeignKeySql);
            stmt.executeUpdate();
            stmt.close();

            addForeignKeySql = "ALTER TABLE rentings " +
                    "ADD CONSTRAINT user_rentings_listings " +
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
        }
    }

    public void dropTables() throws SQLException{
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            for (String table : tables) {
                try (PreparedStatement stmt = conn.prepareStatement("DROP TABLE IF EXISTS " + table)) {
                    stmt.executeUpdate();
                }
            }
        }
    }
}
