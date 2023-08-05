import java.sql.*;
import java.util.Random;

public class JDBCExample {

    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1/mydb";

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

            // Delete table if exists
            String dropTableSQL = "DROP TABLE IF EXISTS Sailors";
            PreparedStatement dropPreparedStatement = conn.prepareStatement(dropTableSQL);
            dropPreparedStatement.executeUpdate();
            dropPreparedStatement.close();

            System.out.println("Table Sailors dropped");

            //Creating table
            String createTableSQL = "CREATE TABLE Sailors ("
                    + "sid INTEGER not NULL, "
                    + "sname VARCHAR(50), "
                    + "rating INTEGER, "
                    + "age INTEGER, "
                    + "PRIMARY KEY(sid))";
            PreparedStatement createPreparedStatement = conn.prepareStatement(createTableSQL);
            createPreparedStatement.executeUpdate();
            createPreparedStatement.close();

            System.out.println("Table Sailors created");

            // Add a rows to the table
            Random rand = new Random();
            for (int i = 0; i < 10; i++) {
                String insertTableSQL = "INSERT INTO Sailors"
                        + "(sid, sname, rating, age) VALUES"
                        + "(?,?,?,?)";
                PreparedStatement insertPreparedStatement = conn.prepareStatement(insertTableSQL);
                insertPreparedStatement.setInt(1, i); // Set sid value
                insertPreparedStatement.setString(2, getRandomName()); // Set sname value
                insertPreparedStatement.setInt(3, rand.nextInt(200)); // Set rating value
                insertPreparedStatement.setInt(4, rand.nextInt(60)); // Set age value
                insertPreparedStatement.executeUpdate();
                insertPreparedStatement.close();
                System.out.println("Record inserted into Sailors table.");
            }

            //Execute a query
            System.out.println("Preparing a statement...");
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM Sailors;";
            ResultSet rs = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int sid  = rs.getInt("sid");
                String sname = rs.getString("sname");
                int rating = rs.getInt("rating");
                int age = rs.getInt("age");

                //Display values
                System.out.print("ID: " + sid);
                System.out.print(", Name: " + sname);
                System.out.print(", Rating: " + rating);
                System.out.println(", Age: " + age);
            }


            System.out.println("Closing connection...");
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("Success!");
        } catch (SQLException e) {
            System.err.println("Connection error occured!");
            System.err.println(e.getMessage());
        }
    }

    // Method to generate a random name
    public static String getRandomName() {
        String[] names = {
                "John", "Jane", "Bob", "Alice", "Tom", "Lucy",
                "David", "Emily", "Michael", "Sara", "Brian", "Rebecca",
                "Chris", "Laura", "Mark", "Katie", "James", "Jennifer",
                "Robert", "Anna", "Joseph", "Ella", "William", "Megan",
                "Richard", "Olivia", "Henry", "Sophia", "Peter", "Grace"
        };
        String[] surnames = {
                "Doe", "Smith", "Johnson", "Brown", "Taylor", "Green",
                "Jones", "Clark", "Wilson", "Miller", "Hall", "White",
                "Lewis", "Harris", "Walker", "Young", "Nelson", "Carter",
                "Mitchell", "Allen", "Wright", "Adams", "Baker", "Hill",
                "Moore", "Roberts", "King", "Jackson", "Davis", "Parker"
        };

        Random random = new Random();
        String name = names[random.nextInt(names.length)];
        String surname = surnames[random.nextInt(surnames.length)];

        return name + " " + surname;
    }

}
