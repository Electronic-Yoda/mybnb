package tools;

import data.Dao;
import domain.Listing;
import domain.User;
import exception.DataAccessException;
import service.BookingService;
import service.ListingService;
import service.UserService;
import mylogger.ConsoleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.cli.*;
import org.jline.reader.*;
import org.jline.terminal.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class ServiceCli {
    // Command Line Interface to interact with services. Handles user input and output relevant information to user
    private static final Logger logger = LogManager.getLogger(ServiceCli.class);
    private final Dao dao = new Dao(
            "jdbc:mysql://localhost:3307/mydb",
            "root",
            ""
    );
    private final UserService userService = new UserService(dao);
    private final ListingService listingService = new ListingService(dao);
    private final BookingService bookingService = new BookingService(dao);
    private String logged_in_user_sin = "";

    public void run() {
        try {
            Terminal terminal = TerminalBuilder.terminal();

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new org.jline.reader.impl.DefaultParser())
                    .history(new DefaultHistory())
                    .completer(new StringsCompleter("create", "user", "login", "logout", "quit", "add", "remove",
                            "delete", "show", "cancel"))
                    .build();

            while (true) {
                String line = reader.readLine("> ");
                String[] commandArgs = line.split(" ");


                if (commandArgs[0] == "") {
                    System.out.println("Please enter a command");
                    continue;
                }

                String command = commandArgs[0];
                String subCommand = "";
                
                if (commandArgs.length > 1)
                    subCommand = commandArgs[1];

                switch (command) {
                    case "create":
                        handleCreateCommand(subCommand, commandArgs);
                        break;
                    case "login":
                        handleLoginUser(subCommand, commandArgs);
                        break;
                    case "logout":
                        handleLogoutUser(subCommand, commandArgs);
                        break;
                    case "quit":
                        System.out.println("Goodbye!");
                        return;
                    case "add":
                        handleAddCommand(subCommand, commandArgs);
                        break;
                    case "remove":
                        handleRemoveCommand(subCommand, commandArgs);
                        break;
                    case "delete":
                        handleDeleteCommand(subCommand, commandArgs);
                        break;
                    case "show":
                        handleShowCommand(subCommand, commandArgs);
                        break;
                    case "cancel":
                        handleCancelCommand(subCommand, commandArgs);
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handlers for each command/sub-command

    // Handler for the "create" command
    private void handleCreateCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "user":
                handleCreateUser(args);
                break;
            case "listing":
                handleCreateListing(args);
                break;
            case "booking":
                handleCreateBooking(args);
                break;
            // TODO: Add other "add" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'create': " + subCommand);
                break;
        }
    }

    // Handler for the "create user" command
    private void handleCreateUser(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("n").longOpt("name").hasArg().required().desc("user name").build());
            options.addOption(
                    Option.builder("a").longOpt("address").hasArg().required().desc("user address").build());
            options.addOption(Option.builder("b").longOpt("birthdate").hasArg().required()
                    .desc("user birthdate (YYYY-MM-DD)").build());
            options.addOption(
                    Option.builder("o").longOpt("occupation").hasArg().required().desc("user occupation").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String userName = cmd.getOptionValue("n");
            String userAddress = cmd.getOptionValue("a").replaceAll("_", " ");
            String userBirthdate = cmd.getOptionValue("b");
            String userOccupation = cmd.getOptionValue("o");

            if (!isValidDate(userBirthdate)) {
                System.out.println("Birthdate must be in format YYYY-MM-DD");
                return;
            }

            // TODO create user logic

            User user = new User(null, userName, userAddress, LocalDate.parse(userBirthdate), userOccupation);
            try {
                dao.insertUser(user);
            }
            catch (DataAccessException e) {
                System.out.println(e.getMessage());
                return;
            }

            System.out.printf("Creating user %s with address %s, birthdate %s, and occupation %s\n",
                    userName, userAddress, userBirthdate, userOccupation);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "create listing" command
    private void handleCreateListing(String[] args) {
        try {
            Options options = new Options();
            options.addOption(
                    Option.builder("t").longOpt("listing type").hasArg().required().desc("listing type").build());
            options.addOption(Option.builder("ppn").longOpt("price per night").hasArg().required()
                    .desc("listing price per night").build());
            options.addOption(Option.builder("a").longOpt("address").hasArg().required()
                    .desc("listing address").build());
            options.addOption(Option.builder("pc").longOpt("postal code").hasArg().required()
                    .desc("listing postal code").build());
            options.addOption(Option.builder("lo").longOpt("longitude").hasArg().required()
                    .desc("listing longitude").build());
            options.addOption(Option.builder("la").longOpt("latitude").hasArg().required()
                    .desc("listing latitude").build());
            options.addOption(Option.builder("ci").longOpt("city").hasArg().required()
                    .desc("listing city").build());
            options.addOption(Option.builder("co").longOpt("country").hasArg().required()
                    .desc("listing country").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingType = cmd.getOptionValue("t");
            String pricePerNight = cmd.getOptionValue("ppn");
            String address = cmd.getOptionValue("a");
            String postalCode = cmd.getOptionValue("pc");
            String longitude = cmd.getOptionValue("lo");
            String latitude = cmd.getOptionValue("la");
            String city = cmd.getOptionValue("ci");
            String country = cmd.getOptionValue("co");

            // Process the "create listing" command here

            Listing listing = new Listing(null, listingType, null, address, postalCode, null, null, city, country, null);

            try {

            }

            System.out.printf("Creating listing with type: %s, price per night: %s, address: %s, postal code: %s, " +
                    "longitude: %s, latitude: %s, city: %s, country: %s\n", listingType, pricePerNight, address,
                    postalCode, longitude, latitude, city, country);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "create booking" command
    private void handleCreateBooking(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            options.addOption(Option.builder("sd").longOpt("startDate").hasArg().required()
                    .desc("availability start date").build());
            options.addOption(
                    Option.builder("ed").longOpt("endDate").hasArg().required().desc("availability end date").build());
            options.addOption(
                    Option.builder("pm").longOpt("paymentMethod").hasArg().required().desc("payment method").build());
            options.addOption(
                    Option.builder("cn").longOpt("cardNumber").hasArg().required().desc("card number").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");
            String availabilityStartDate = cmd.getOptionValue("sd");
            String availabilityEndDate = cmd.getOptionValue("ed");
            String paymentMethod = cmd.getOptionValue("pm");
            String cardNumber = cmd.getOptionValue("cn");

            if (!(isValidDate(availabilityStartDate) && isValidDate(availabilityEndDate))) {
                System.out.println("Start Date and End Date must be in format YYYY-MM-DD");
                return;
            }

            // TODO add create booking

            System.out.printf("Created booking for listing: %s from: %s to %s\n Paid with card: %s using method: %s\n", listingId, availabilityStartDate,
                    availabilityEndDate, cardNumber, paymentMethod);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "login user" command
    private void handleLoginUser(String subCommand, String[] args) {
        if (subCommand.equals("user")) {
            try {
                Options options = new Options();
                options.addOption(Option.builder("s").longOpt("sin").hasArg().required().desc("user sin").build());

                CommandLineParser parser = new DefaultParser();
                CommandLine cmd = parser.parse(options, args);

                String userSin = cmd.getOptionValue("s");

                // TODO login user

                // If success
                logged_in_user_sin = userSin;

                System.out.printf("Login with SIN %s\n", userSin);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Unknown sub-command for 'login': " + subCommand);
        }
    }

    // Handler for the "logout user" command
    private void handleLogoutUser(String subCommand, String[] args) {
        if (subCommand.equals("user")) {
            System.out.printf("Logging out user: %s\n", logged_in_user_sin);
            logged_in_user_sin = "";
        } else {
            System.out.println("Unknown sub-command for 'login': " + subCommand);
        }
    }

    // Handler for the "add" command
    private void handleAddCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "availability":
                handleAddAvailability(args);
                break;
            // TODO: Add other "add" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'add': " + subCommand);
                break;
        }
    }

    // Handler for the "add availability" command
    private void handleAddAvailability(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            options.addOption(Option.builder("sd").longOpt("startDate").hasArg().required()
                    .desc("availability start date").build());
            options.addOption(
                    Option.builder("ed").longOpt("endDate").hasArg().required().desc("availability end date").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");
            String availabilityStartDate = cmd.getOptionValue("sd");
            String availabilityEndDate = cmd.getOptionValue("ed");

            if (!(isValidDate(availabilityStartDate) && isValidDate(availabilityEndDate))) {
                System.out.println("Start Date and End Date must be in format YYYY-MM-DD");
                return;
            }

            // TODO availability logic

            System.out.printf("Added availability for listing: %s from: %s to %s\n", listingId, availabilityStartDate,
                    availabilityEndDate);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "remove" command
    private void handleRemoveCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "availability":
                handleRemoveAvailability(args);
                break;
            // TODO: Add other "remove" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'remove': " + subCommand);
                break;
        }
    }

    // Handler for the "remove availability" command
    private void handleRemoveAvailability(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            options.addOption(Option.builder("sd").longOpt("startDate").hasArg().required()
                    .desc("availability start date").build());
            options.addOption(
                    Option.builder("ed").longOpt("endDate").hasArg().required().desc("availability end date").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");
            String availabilityStartDate = cmd.getOptionValue("sd");
            String availabilityEndDate = cmd.getOptionValue("ed");

            if (!(isValidDate(availabilityStartDate) && isValidDate(availabilityEndDate))) {
                System.out.println("Start Date and End Date must be in format YYYY-MM-DD");
                return;
            }

            // TODO removal logic
            boolean removed = true;

            if (removed) {
                System.out.printf("Removed availability for listing: %s from: %s to %s\n", listingId,
                        availabilityStartDate, availabilityEndDate);
            } else {
                System.out.printf("Availability not found for listing: %s from: %s to %s\n", listingId,
                        availabilityStartDate, availabilityEndDate);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "delete" command
    private void handleDeleteCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "listing":
                handleDeleteListing(args);
                break;
            // TODO: Add other "delete" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'delete': " + subCommand);
                break;
        }
    }

    // Handler for the "delete listing" command
    private void handleDeleteListing(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");

            // TODO Listing deletion logic
            // Check if user sin matches to host

            boolean removed = true;

            if (removed) {
                System.out.println("Deleted listing with id: " + listingId);
            } else {
                System.out.println("Listing not found with id: " + listingId);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "show" command
    private void handleShowCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "mylistings":
                handleShowMyListings(args);
                break;
            case "mybookings":
                handleShowMyBookings(args);
                break;
            // TODO: Add other "show" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'show': " + subCommand);
                break;
        }
    }

    // Handler for the "show mylistings" command
    private void handleShowMyListings(String[] args) {
        System.out.println("My Listings:");
        // TODO show mylistings logic
        // use saved user sin

    }

    // Handler for the "show mybookings" command
    private void handleShowMyBookings(String[] args) {
        System.out.println("My Bookings:");
        // TODO show myBooking logic
        // use saved user sin
    }

    // Handler for the "cancel" command
    private void handleCancelCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "booking":
                handleCancelBooking(args);
                break;
            // TODO: Add other "cancel" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'cancel': " + subCommand);
                break;
        }
    }

    // Handler for the "cancel booking" command
    private void handleCancelBooking(String[] args) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("b").longOpt("booking-id").hasArg().required().desc("booking id").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String bookingId = cmd.getOptionValue("b");

            // TODO Booking cancellation logic
            boolean removed = true;

            if (removed) {
                System.out.println("Cancelled booking with id: " + bookingId);
            } else {
                System.out.println("Booking not found with id: " + bookingId);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Helper
    private boolean isValidDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ConsoleLogger.setup();
        ServiceCli serviceCli = new ServiceCli();
        serviceCli.run();
    }
}
