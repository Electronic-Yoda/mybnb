package tools;

import data.Dao;
import mybnb.BookingService;
import mybnb.ListingService;
import mybnb.UserService;
import mylogger.ConsoleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceCli {
    // Command Line Interface to interact with services. Handles user input and output relevant information to user
    private static final Logger logger = LogManager.getLogger(ServiceCli.class);
    private final Dao dao = new Dao(
            "jdbc:mysql://localhost:3307/mydb",
            "root",
            "password"
    );
    private final UserService userService = new UserService(dao);
    private final ListingService listingService = new ListingService(dao);
    private final BookingService bookingService = new BookingService(dao);

    public void run() {
        // Start CLI
        while(true) {
            // Get user input
            // Call Services methods
            // Print result
        }
    }
    public static void main(String[] args) throws ClassNotFoundException {
        ConsoleLogger.setup();
        ServiceCli serviceCli = new ServiceCli();
        serviceCli.run();
    }
}
