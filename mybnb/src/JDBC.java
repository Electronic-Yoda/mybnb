import java.sql.*;
import java.util.Random;

public class Tester {

    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://localhost:3306/testdb";

    public static void main(String[] args) throws ClassNotFoundException {
        //Register JDBC driver
        Class.forName(dbClassName);
        //Database credentials
        final String USER = "root";
        final String PASS = "";
        System.out.println("Connecting to database...");

        

        try {
            //Establish connection
            Connection conn = DriverManager.getConnection(CONNECTION,USER,PASS);
            System.out.println("Successfully connected to MySQL!");

            deleteTables(conn);

            createTables(conn);

            // Add a rows to the table
            // Random rand = new Random();
            // for (int i = 0; i < 10; i++) {
            //     String insertTableSQL = "INSERT INTO Sailors"
            //             + "(sid, sname, rating, age) VALUES"
            //             + "(?,?,?,?)";
            //     PreparedStatement insertPreparedStatement = conn.prepareStatement(insertTableSQL);
            //     insertPreparedStatement.setInt(1, i); // Set sid value
            //     insertPreparedStatement.setString(2, getRandomName()); // Set sname value
            //     insertPreparedStatement.setInt(3, rand.nextInt(200)); // Set rating value
            //     insertPreparedStatement.setInt(4, rand.nextInt(60)); // Set age value
            //     insertPreparedStatement.executeUpdate();
            //     insertPreparedStatement.close();
            //     System.out.println("Record inserted into Sailors table.");
            // }

            // //Execute a query
            // System.out.println("Preparing a statement...");
            // Statement stmt = conn.createStatement();
            // String sql = "SELECT * FROM Sailors;";
            // ResultSet rs = stmt.executeQuery(sql);

            // //STEP 5: Extract data from result set
            // while(rs.next()){
            //     //Retrieve by column name
            //     int sid  = rs.getInt("sid");
            //     String sname = rs.getString("sname");
            //     int rating = rs.getInt("rating");
            //     int age = rs.getInt("age");

            //     //Display values
            //     System.out.print("ID: " + sid);
            //     System.out.print(", Name: " + sname);
            //     System.out.print(", Rating: " + rating);
            //     System.out.println(", Age: " + age);
            // }


            System.out.println("Closing connection...");
            // rs.close();
            // stmt.close();
            conn.close();
            System.out.println("Success!");
        } catch (SQLException e) {
            System.err.println("Connection error occured!");
            System.err.println(e.getMessage());
        }
    }

    // Method to generate a random name
    public static String getRandomName() {
        String[] names = {"John", "Jane", "Bob", "Alice", "Tom", "Lucy"};
        String[] surnames = {"Doe", "Smith", "Johnson", "Brown", "Taylor", "Green"};

        Random random = new Random();
        String name = names[random.nextInt(names.length)];
        String surname = surnames[random.nextInt(surnames.length)];

        return name + " " + surname;
    }

    public static void deleteTables(Connection conn) {
        // Delete table if exists

        try {
            String [] tables = {"availability", "listings", "rentings", "reviews", "users"};
            for (int i = 0; i < 5; i++) {
                String dropTableSQL = "DROP TABLE IF EXISTS " + tables[i];
                PreparedStatement dropPreparedStatement = conn.prepareStatement(dropTableSQL);
                dropPreparedStatement.executeUpdate();
                dropPreparedStatement.close();

                System.out.println("Table " + tables[i] + " dropped");                
            }
        }   catch (SQLException e) {
            System.err.println("Connection error occured!");
            System.err.println(e.getMessage());
        }

    }

    public static void createTables(Connection conn) {

        String createAvailabilityTableString = "CREATE TABLE availability (\r\n" + //
                "    availability_id serial  NOT NULL,\r\n" + //
                "    start_date date  NOT NULL,\r\n" + //
                "    end_date date  NOT NULL,\r\n" + //
                "    listings_listing_id BIGINT UNSIGNED NOT NULL,\r\n" + //
                "    CONSTRAINT availability_pk PRIMARY KEY (availability_id)\r\n" + //
                ");";
        
        String createRentingTableString = "CREATE TABLE listings (\r\n" + //
                "    listing_id serial  NOT NULL AUTO_INCREMENT,\r\n" + //
                "    listing_type char(10)  NOT NULL,\r\n" + //
                "    longitude decimal(9,6)  NOT NULL,\r\n" + //
                "    postal_code char(12)  NOT NULL,\r\n" + //
                "    latitude decimal(9,6)  NOT NULL,\r\n" + //
                "    city varchar(20)  NOT NULL,\r\n" + //
                "    country varchar(20)  NOT NULL,\r\n" + //
                "    amenities varchar(100)  NOT NULL,\r\n" + //
                "    users_sin int  NOT NULL,\r\n" + //
                "    CONSTRAINT listings_pk PRIMARY KEY (listing_id)\r\n" + //
                ");";

        String createListingTableString = "CREATE TABLE rentings (\r\n" + //
                "    renting_id serial NOT NULL AUTO_INCREMENT,\r\n" + //
                "    start_date date  NOT NULL,\r\n" + //
                "    end_date date  NOT NULL,\r\n" + //
                "    transaction_date date  NOT NULL,\r\n" + //
                "    amount decimal(10,2)  NOT NULL,\r\n" + //
                "    currency varchar(3)  NOT NULL,\r\n" + //
                "    payment_method varchar(20)  NOT NULL,\r\n" + //
                "    users_sin int  NOT NULL,\r\n" + //
                "    listings_listing_id BIGINT UNSIGNED NOT NULL UNIQUE,\r\n" + //
                "    CONSTRAINT rentings_pk PRIMARY KEY (renting_id)\r\n" + //
                ");";

        String createReviewTableString = "CREATE TABLE reviews (\r\n" + //
                "    review_id serial NOT NULL AUTO_INCREMENT,\r\n" + //
                "    rating_of_listing int  NULL,\r\n" + //
                "    rating_of_host int  NULL,\r\n" + //
                "    rating_of_renter int  NULL,\r\n" + //
                "    comment_from_rentor varchar(500)  NULL,\r\n" + //
                "    comment_from_host varchar(500)  NULL,\r\n" + //
                "    rentings_renting_id BIGINT UNSIGNED NOT NULL UNIQUE,\r\n" + //
                "    CONSTRAINT reviews_pk PRIMARY KEY (review_id)\r\n" + //
                ");";
                                    
        String createUserTableString = "CREATE TABLE users ("
                                    + "sin int NOT NULL,"
                                    + "name varchar (20) NOT NULL,"
                                    + "address varchar(3) NOT NULL,"
                                    + "birthdate date NOT NULL,"
                                    + "occupation varchar(20)  NOT NULL,"
                                    + "CONSTRAINT users_pk PRIMARY KEY (sin)"
                                    + ");";

        String [] createTableStrings = {createAvailabilityTableString, createListingTableString, createRentingTableString, createReviewTableString, createUserTableString};
        
        try {

            for (int i = 0; i<5; i++) {
                PreparedStatement createPreparedStatement = conn.prepareStatement(createTableStrings[i]);
                createPreparedStatement.executeUpdate();
                createPreparedStatement.close();

                System.out.println(String.format("Table %s created", createTableStrings[i].split(" ")[2]));
            }

        } catch (SQLException e) {
            System.err.println("Connection error occured!");
            System.err.println(e.getMessage());
        }


    }

}
