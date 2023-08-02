package tools;

import data.Dao;
import domain.Availability;
import domain.Booking;
import domain.Listing;
import domain.User;
import exception.DataAccessException;
import exception.ServiceException;
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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                userService.addUser(user);
            
            } catch (ServiceException e) {

            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } 

            System.out.printf("Creating user %s with address %s, birthdate %s, and occupation %s\n",
                    userName, userAddress, userBirthdate, userOccupation);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "create listing" command
    private void handleCreateListing(String[] args) {
        if (checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(
                    Option.builder("t").longOpt("listing type").hasArg().required().desc("listing type").build());

            // TODO REMOVE
            // options.addOption(Option.builder("ppn").longOpt("price per night").hasArg().required()
            //         .desc("listing price per night").build());
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

            // TODO REMOVE
            // String pricePerNight = cmd.getOptionValue("ppn");
            String address = cmd.getOptionValue("a");
            String postalCode = cmd.getOptionValue("pc");
            String longitude = cmd.getOptionValue("lo");
            String latitude = cmd.getOptionValue("la");
            String city = cmd.getOptionValue("ci");
            String country = cmd.getOptionValue("co");

            // Process the "create listing" command here

            if (!isValidListingType(listingType)) {
                System.out.println("Listing type must be one of: House, Apartment, Guesthouse, Hotel");
                return;
            }

            if (!isValidPostalCode(postalCode)) {
                System.out.println("Postal code must be in format A1A 1A1");
                return;
            }

            // TODO Add Listing
            Listing listing = new Listing(null, listingType, address, postalCode, null, null, city, country, Long.parseLong(logged_in_user_sin));

            try {
                //dao.insertListing(listing);

                // TODO ADD AMENITIES CHANGE TO MULTI PROMPT

                // PROMPT USER FOR AMENITIES "N to exit"
                // check if amenity is valid

            }  catch (ServiceException e) {

                System.out.println(e.getMessage());
                if (e.getCause() != null) {
                    System.out.println(e.getCause().getMessage());
                }
            }

            System.out.printf("Creating listing with type: %s, price per night: %s, address: %s, postal code: %s, " +
                    "longitude: %s, latitude: %s, city: %s, country: %s\n", listingType, address,
                    postalCode, longitude, latitude, city, country);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Handler for the "create booking" command
    private void handleCreateBooking(String[] args) {
        if (checkUserLoggedIn())
            return;

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
            // TODO check requirements if user will be recommended a listing

            try {
                // Check if user sin matches to listingId
                // if (dao.doesListingIdHaveHostSin(Long.parseLong(listingId), Long.parseLong(logged_in_user_sin)))
                //     System.out.println("User is host of listing, cannot book own listing");

                // TODO
                //Float amount = dao.getListingPricePerNight(Long.parseLong(listingId)); // TODO get price per night

                // Create booking
                //Booking booking = new Booking(null, LocalDate.parse(availabilityStartDate), LocalDate.parse(availabilityEndDate), dao.getCurrentDate(), paymentMethod, amount, cardNumber, Long.parseLong(logged_in_user_sin), Long.parseLong(listingId));

            } catch (ServiceException e) {
                System.out.println(e.getMessage());
                return;
            }

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
        if (checkUserLoggedIn())
            return;

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
        if (checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            options.addOption(Option.builder("sd").longOpt("startDate").hasArg().required()
                    .desc("availability start date").build());
            options.addOption(
                    Option.builder("ed").longOpt("endDate").hasArg().required().desc("availability end date").build());
            options.addOption(Option.builder("ppn").longOpt("price per night").hasArg().required()
                    .desc("listing price per night").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");
            String availabilityStartDate = cmd.getOptionValue("sd");
            String availabilityEndDate = cmd.getOptionValue("ed");
            String pricePerNight = cmd.getOptionValue("ppn");

            if (!(isValidDate(availabilityStartDate) && isValidDate(availabilityEndDate))) {
                System.out.println("Start Date and End Date must be in format YYYY-MM-DD");
                return;
            }

            // TODO availability logic

            // TODO Check if user sin matches to listingId

            Availability availability = new Availability(null, LocalDate.parse(availabilityStartDate), LocalDate.parse(availabilityEndDate), new BigDecimal(pricePerNight), Long.parseLong(listingId));

            try {
                //dao.insertAvailability(availability);
            } catch (ServiceException e) {
                System.out.println(e.getMessage());
                return;
            }

            System.out.printf("Added availability for listing: %s from: %s to %s\nprice per night: %s\n", listingId, availabilityStartDate,
                    availabilityEndDate, pricePerNight);
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
        if (checkUserLoggedIn())
            return;

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

            // TODO Check if user sin matches to listingId

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
        if (checkUserLoggedIn())
            return;

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
        if (checkUserLoggedIn())
            return;

        System.out.println("My Listings:");
        // TODO show mylistings logic
        // use saved user sin

    }

    // Handler for the "show mybookings" command
    private void handleShowMyBookings(String[] args) {
        if (checkUserLoggedIn())
            return;

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
        if (checkUserLoggedIn())
            return;

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

    private boolean isValidPostalCode(String postalCode) {
        /*
         * Validates Canadian postal code
         */
        Pattern pattern = Pattern.compile("^(?!.*[DFIOQU])[A-VXY][0-9][A-Z] ?[0-9][A-Z][0-9]$");
        Matcher matcher = pattern.matcher(postalCode);
        return matcher.matches();
    }

    private boolean isValidListingType(String listingType) {
        List<String> listingTypes = new ArrayList<>();
        listingTypes.add("House");
        listingTypes.add("Apartment");
        listingTypes.add("Guesthouse");
        listingTypes.add("Hotel");

        return listingTypes.contains(listingType);
    }

    private boolean isValidAmenity(String amenity) {
    List<String> amenityTypes = new ArrayList<>();

        amenityTypes.add("Wifi");
        amenityTypes.add("Washer");
        amenityTypes.add("Air conditioning");
        amenityTypes.add("Dedicated workspace");
        amenityTypes.add("Hair dryer");
        amenityTypes.add("Kitchen");
        amenityTypes.add("Dryer");
        amenityTypes.add("Heating");
        amenityTypes.add("TV");
        amenityTypes.add("Iron");
        amenityTypes.add("Pool");
        amenityTypes.add("Free parking");
        amenityTypes.add("Crib");
        amenityTypes.add("BBQ grill");
        amenityTypes.add("Indoor fireplace");
        amenityTypes.add("Hot tub");
        amenityTypes.add("EV charger");
        amenityTypes.add("Gym");
        amenityTypes.add("Breakfast");
        amenityTypes.add("Smoking allowed");
        amenityTypes.add("Beachfront");
        amenityTypes.add("Ski-in/ski-out");
        amenityTypes.add("Waterfront");
        amenityTypes.add("Smoke alarm");
        amenityTypes.add("Carbon monoxide alarm");

        return amenityTypes.contains(amenity);
    }

    public boolean checkUserLoggedIn() {
        if (!logged_in_user_sin.equals("")) {
            System.out.println("Please login first");
            return false;
        }
        return true;
    }

    public boolean getValidDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return end.compareTo(start) > 0;
    }

    public Integer getNumberOfDays(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        // TODO
        return 0;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ConsoleLogger.setup();
        ServiceCli serviceCli = new ServiceCli();
        serviceCli.run();
    }
}
