package tool;

import data.Dao;
import domain.*;
import exception.ServiceException;
import filter.ListingFilter;
import service.BookingService;
import service.ListingService;
import service.UserService;
import mylogger.ConsoleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.jline.reader.*;
import org.jline.terminal.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;

import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceCli {
    // Command Line Interface to interact with services. Handles user input and
    // output relevant information to user
    private static final Logger logger = LogManager.getLogger(ServiceCli.class);
    private final Dao dao = new Dao(
            "jdbc:mysql://localhost:3307/mydb",
            "root",
            "");
    private final UserService userService = new UserService(dao);
    private final ListingService listingService = new ListingService(dao);
    private final BookingService bookingService = new BookingService(dao);
    private String logged_in_user_sin = "";

    public void start() {
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
                String prompt = logged_in_user_sin.equals("") ? "> " : "<user " + logged_in_user_sin + "> ";
                String line = reader.readLine(prompt);
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
            System.out.println(e.getMessage());
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
            options.addOption(Option.builder("s").longOpt("sin").hasArg().required().desc("user sin").build());
            options.addOption(Option.builder("n").longOpt("name").hasArg().required().desc("user name").build());
            options.addOption(
                    Option.builder("a").longOpt("address").hasArg().required().desc("user address").build());
            options.addOption(Option.builder("b").longOpt("birthdate").hasArg().required()
                    .desc("user birthdate (YYYY-MM-DD)").build());
            options.addOption(
                    Option.builder("o").longOpt("occupation").hasArg().required().desc("user occupation").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String userSin = cmd.getOptionValue("s");
            String userName = cmd.getOptionValue("n").replaceAll("_", " ");
            String userAddress = cmd.getOptionValue("a").replaceAll("_", " ");
            String userBirthdate = cmd.getOptionValue("b");
            String userOccupation = cmd.getOptionValue("o").replaceAll("_", " ");

            if (!isValidDate(userBirthdate)) {
                System.out.println("Birthdate must be in format YYYY-MM-DD");
                return;
            }

            User user = new User(Long.parseLong(userSin), userName, userAddress, LocalDate.parse(userBirthdate),
                    userOccupation);
            userService.addUser(user);

            System.out.printf("Creating user %s with sin %s, address %s, birthdate %s, and occupation %s\n",
                    userName, userSin, userAddress, userBirthdate, userOccupation);

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
            return;
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    // Handler for the "create listing" command
    private void handleCreateListing(String[] args) {
        if (!checkUserLoggedIn())
            return;
        try {
            Options options = new Options();
            options.addOption(
                    Option.builder("t").longOpt("listing type").hasArg().required().desc("listing type").build());

            options.addOption(Option.builder("a").longOpt("address").hasArg().required()
                    .desc("listing address").build());
            options.addOption(Option.builder("pc").longOpt("postal_code").hasArg().required()
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
            String address = cmd.getOptionValue("a").replaceAll("_", " ");
            String postalCode = cmd.getOptionValue("pc").replaceAll("_", " ");
            String longitude = cmd.getOptionValue("lo");
            String latitude = cmd.getOptionValue("la");
            String city = cmd.getOptionValue("ci");
            String country = cmd.getOptionValue("co");

            // Process the "create listing" command here

            if (!isValidListingType(listingType)) {
                System.out.println("Listing type must be one of: House, Apartment, Guesthouse, Hotel");
                return;
            }

//            if (!isValidPostalCode(postalCode)) {
//                System.out.println("Postal code must be in format A1A 1A1");
//                return;
//            }

            Listing listing = new Listing(null, listingType, address, postalCode,
                    new Point2D.Double(Double.parseDouble(longitude), Double.parseDouble(latitude)),
                    city, country,
                    Long.parseLong(logged_in_user_sin));

            Long listing_id =  listingService.addListing(listing);

            System.out.printf("Creating listing with type: %s, address: %s, postal code: %s, " +
                                "longitude: %s, latitude: %s, city: %s, country: %s\n", listingType, address,
                                postalCode, longitude, latitude, city, country);
            System.out.println("Listing id: " + listing_id);

            // Add amenities
            amenityAdderRoutine(listing_id);

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }

    }

    // Handler for the "create booking" command
    private void handleCreateBooking(String[] args) {
        if (!checkUserLoggedIn())
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

            // Check if user sin matches to listingId
            if (listingService.isHostOfListing(Long.parseLong(logged_in_user_sin), Long.parseLong(listingId))) {
                System.out.println("User is host of listing, cannot book own listing");
                return;
            }

//            Float amount = listingService.getListingPricePerNight(Long.parseLong(listingId)) * getNumberOfDays(availabilityStartDate, availabilityEndDate);
//            LocalDate today = LocalDate.parse(bookingService.getCurrDate().toString());

            // Create booking
            Booking booking = new Booking(null, LocalDate.parse(availabilityStartDate),
                    LocalDate.parse(availabilityEndDate), LocalDate.now(), null, paymentMethod, Long.parseLong(cardNumber),
                    Long.parseLong(logged_in_user_sin), Long.parseLong(listingId));

            Long booking_id = bookingService.addBooking(booking);

        System.out.printf("Created booking for listing: %s from: %s to %s\nPaid with card: %s using method: %s\n",
                listingId, availabilityStartDate,
                availabilityEndDate, cardNumber, paymentMethod);

            System.out.printf("Booking id: %s\n", booking_id);

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    // Handler for the "login user" command
    private void handleLoginUser(String subCommand, String[] args) {
        try {
            if (subCommand.equals("user")) {
                if (!logged_in_user_sin.equals("")) {
                    System.out.printf("User %s already logged in\n", logged_in_user_sin);
                    return;
                }
                Options options = new Options();
                options.addOption(Option.builder("s").longOpt("sin").hasArg().required().desc("user sin").build());

                CommandLineParser parser = new DefaultParser();
                CommandLine cmd = parser.parse(options, args);
                String userSin = cmd.getOptionValue("s");

                // help option


                if (!userService.userExists(Long.parseLong(userSin))) {
                    System.out.println("User does not exist");
                    return;
                }

                logged_in_user_sin = userSin;
                System.out.printf("Login with SIN %s\n", userSin);
            } else {
                System.out.println("Unknown sub-command for 'login': " + subCommand);
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    // Handler for the "logout user" command
    private void handleLogoutUser(String subCommand, String[] args) {
        if (!checkUserLoggedIn())
            return;

        if (subCommand.equals("user")) {
            System.out.printf("Logging out user: %s\n", logged_in_user_sin);
            logged_in_user_sin = "";
        } else {
            System.out.println("Unknown sub-command for 'logout': " + subCommand);
        }
    }

    // Handler for the "add" command
    private void handleAddCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "availability":
                handleAddAvailability(args);
                break;
            case "amenity":
                handleAddAmenity(args);
                break;
            case "review":
                handleAddReview(args);
                break;
            // TODO: Add other "add" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'add': " + subCommand);
                break;
        }
    }

    // Handler for the "add availability" command
    private void handleAddAvailability(String[] args) {
        if (!checkUserLoggedIn())
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

            // Check if listing exists
            if (!listingService.doesListingExist(Long.parseLong(listingId))) {
                System.out.println("Listing does not exist");
                return;
            }

            Listing listing = listingService.getListing(Long.parseLong(listingId));

            // Recommend price per night if not provided
            if (pricePerNight.equals("unsure")) {
                // Check if city exists
                if (!listingService.doesCityExists(listing.city())) {
                    System.out.println("No recommended pricing exists for this city. Please provide price per night");
                }
                else {
                    String recommendedPricePerNight = listingService.getRecommendedPricePerNight(Long.parseLong(listingId));
                    if (recommendedPricePerNight.equals("0.0")) {
                        System.out.println("No recommended pricing exists for this city. Please provide price per night");
                    } else {
                        System.out.println("Recommended price per night (Average price per night based on city): " + recommendedPricePerNight);
                    }
                }

                Scanner myScanner = new Scanner(System.in);
                System.out.println("Confirm price:");
                pricePerNight = myScanner.nextLine();
            }

            if (!(isValidDate(availabilityStartDate) && isValidDate(availabilityEndDate))) {
                System.out.println("Start Date and End Date must be in format YYYY-MM-DD");
                return;
            }

            // Check Price per night not negative
            if (Float.parseFloat(pricePerNight) < 0) {
                System.out.println("Price per night must be not negative");
                return;
            }

            // Date range makes sense
            if (!getValidDateRange(availabilityStartDate, availabilityEndDate)) {
                System.out.println("Start Date must be before End Date");
                return;
            }


            // add availability
            Availability availability = new Availability(null, LocalDate.parse(availabilityStartDate),
                    LocalDate.parse(availabilityEndDate), new BigDecimal(pricePerNight).setScale(2), Long.parseLong(listingId));

            listingService.addAvailability(availability, Long.parseLong(logged_in_user_sin), LocalDate.now());
            System.out.printf("Added availability for listing: %s from: %s to %s\nprice per night: %s\n", listingId,
                    availabilityStartDate,
                    availabilityEndDate, pricePerNight);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
            System.out.println(e.getCause().getMessage());
            }
            return;
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    public void handleAddAmenity(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");

            // Check if listing exists
            if (!listingService.doesListingExist(Long.parseLong(listingId))) {
                System.out.println("Listing does not exist");
                return;
            }

            // Check if user is host of listing
            if (!listingService.isHostOfListing(Long.parseLong(logged_in_user_sin), Long.parseLong(listingId))) {
                System.out.println("User is not host of listing");
                return;
            }

            amenityAdderRoutine(Long.parseLong(listingId));

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
            System.out.println(e.getCause().getMessage());
            }
            return;
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    public void handleAddReview(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("b").longOpt("booking-id").hasArg().required().desc("booking id").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String bookingId = cmd.getOptionValue("b");

            // Check if user is tenant of booking
            if (bookingService.isTenantOfBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin))) {
                tenantReviewAdderRoutine(Long.parseLong(logged_in_user_sin), Long.parseLong(bookingId));
            }
            // Check if user is host of Booking
            else if (bookingService.isHostOfBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin))) {
                hostReviewAdderRoutine(Long.parseLong(logged_in_user_sin), Long.parseLong(bookingId));
            }
            else {
                System.out.println("User is neither host nor tenant of booking");
                return;
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
            return;
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }


    // Handler for the "remove" command
    private void handleRemoveCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "availability":
                handleRemoveAvailability(args);
                break;
            case "review":
                handleRemoveReview(args);
                break;
            // TODO: Add other "remove" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'remove': " + subCommand);
                break;
        }
    }

    // Handler for the "remove availability" command
    private void handleRemoveAvailability(String[] args) {
        if (!checkUserLoggedIn())
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

            if (!getValidDateRange(availabilityStartDate, availabilityEndDate)) {
                System.out.println("Start Date must be before End Date");
                return;
            }

            listingService.deleteAvailability(Long.parseLong(listingId), Long.parseLong(logged_in_user_sin), LocalDate.parse(availabilityStartDate), LocalDate.parse(availabilityEndDate));

            System.out.printf("Removed availability for listing: %s from: %s to %s\n", listingId,
                availabilityStartDate, availabilityEndDate);
            
        } catch (ServiceException e) {
            System.out.println(e.getMessage());

            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void handleRemoveReview(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("b").longOpt("booking-id").hasArg().required().desc("booking id").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String bookingId = cmd.getOptionValue("b");

            // Check if user is tenant of booking
            if (bookingService.isTenantOfBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin))) {
                tenantReviewRemoverRoutine(Long.parseLong(logged_in_user_sin), Long.parseLong(bookingId));
            }
            // Check if user is host of Booking
            else if (bookingService.isHostOfBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin))) {
                hostReviewRemoverRoutine(Long.parseLong(logged_in_user_sin), Long.parseLong(bookingId));
            }
            else {
                System.out.println("User is neither host nor tenant of booking");
                return;
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());

            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());

            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    // Handler for the "delete" command
    private void handleDeleteCommand(String subCommand, String[] args) {
        switch (subCommand) {
            case "listing":
                handleDeleteListing(args);
                break;
            case "amenity":
                handleDeleteAmenity(args);
                break;
            // TODO: Add other "delete" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'delete': " + subCommand);
                break;
        }
    }

    // Handler for the "delete listing" command
    private void handleDeleteListing(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");

            // Check if user sin matches to host
            if (!listingService.isHostOfListing(Long.parseLong(logged_in_user_sin), Long.parseLong(listingId))) {
                System.out.println("User is not host of listing");
                return;
            }

            // Check if there are any future bookings for listing
            if (listingService.doesListingHaveFutureBookings(Long.parseLong(listingId), LocalDate.now())) {
                System.out.println("Listing has bookings, cannot delete");
                return;
            }

            // delete listing
            listingService.deleteListing(Long.parseLong(listingId), Long.parseLong(logged_in_user_sin), LocalDate.now());
            System.out.println("Deleted listing with id: " + listingId);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    public void handleDeleteAmenity(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            options.addOption(Option.builder("a").longOpt("amenity").hasArg().required().desc("amenity").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");
            String amenity = cmd.getOptionValue("a").replaceAll("_", " ");

            // Check if listing exists
            if (!listingService.doesListingExist(Long.parseLong(listingId))) {
                System.out.println("Listing does not exist");
                return;
            }

            // Check if user is host of listing
            if (!listingService.isHostOfListing(Long.parseLong(logged_in_user_sin), Long.parseLong(listingId))) {
                System.out.println("User is not host of listing");
                return;
            }

            // delete amenity
            listingService.removeAmenityFromListing(Long.parseLong(listingId), Long.parseLong(logged_in_user_sin), amenity);
            System.out.printf("Deleted amenity: %s for listing: %s\n", amenity, listingId);
        } catch (ServiceException e) {
            System.out.println(e.getMessage());

            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());

            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
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
            case "myreviews":
                handleShowMyReviews(args);
                break;
            case "amenities":
                handleShowAmenities(args);
                break;
            case "availabilities":
                handleShowAvailability(args);
                break;
            case "listings":
                handleShowListings(args);
                break;
            // TODO: Add other "show" sub-commands handlers here, if needed
            default:
                System.out.println("Unknown sub-command for 'show': " + subCommand);
                break;
        }
    }

    // Handler for the "show mylistings" command
    private void handleShowMyListings(String[] args) {
        if (!checkUserLoggedIn())
            return;

        System.out.println("My Listings:");
        // show user listings
        
        try {
            List<Listing> listings = listingService.getListingsOfUser(Long.parseLong(logged_in_user_sin));

            for (Listing listing : listings) {
//                String listingString = String.format("Listing id: %s, Listing type: %s, Address: %s, Postal code: %s, Longitude: %s, Latitude: %s, City: %s, Country: %s, Host SIN: %s",
//                        listing.listing_id(), listing.listing_type(), listing.address(), listing.postal_code(), listing.longitude(), listing.latitude(), listing.city(), listing.country(), listing.users_sin());
//                System.out.println(listingString);

                System.out.println(listing);
            }

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    // Handler for the "show mybookings" command
    private void handleShowMyBookings(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            System.out.println("My Bookings (as a Tenant):");
            List<Booking> bookings = bookingService.getBookingsOfUser(Long.parseLong(logged_in_user_sin));

            for (Booking booking : bookings) {
                String bookingString = String.format("Booking id: %s, Start date: %s, End date: %s, Booking date: %s, Payment method: %s, Amount: %s, Card number: %s, Tenant SIN: %s, Listing id: %s",
                        booking.booking_id(), booking.start_date(), booking.end_date(), booking.transaction_date(), booking.payment_method(), booking.amount(), booking.card_number(), booking.tenant_sin(), booking.listings_listing_id());
                System.out.println(bookingString);
            }

            System.out.println("My Bookings (as a Host):");
            bookings = bookingService.getBookingsOfHost(Long.parseLong(logged_in_user_sin));

            for (Booking booking : bookings) {
                String bookingString = String.format("Booking id: %s, Start date: %s, End date: %s, Booking date: %s, Amount: %s, Tenant SIN: %s, Listing id: %s",
                        booking.booking_id(), booking.start_date(), booking.end_date(), booking.transaction_date(), booking.amount(), booking.tenant_sin(), booking.listings_listing_id());
                System.out.println(bookingString);
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void handleShowMyReviews(String[] args) {
        if (!checkUserLoggedIn())
            return;

        try {
            System.out.println("My Reviews (as a Tenant):");
            List<Review> reviews = bookingService.getReviewsAsTenant(Long.parseLong(logged_in_user_sin));

            for (Review review : reviews) {
                String reviewString = String.format("Review id: %s, Rating: %s, Comment: %s, Booking id: %s",
                        review.review_id(), review.rating_of_tenant(), review.comment_from_host(), review.bookings_booking_id());
                System.out.println(reviewString);
            }

            System.out.println("My Reviews (as a Host):");
            reviews = bookingService.getReviewsAsHost(Long.parseLong(logged_in_user_sin));

            for (Review review : reviews) {
                String reviewString = String.format("Review id: %s, Host Rating: %s, Listing Rating: %s, Comment: %s, Booking id: %s",
                        review.review_id(), review.rating_of_host(), review.rating_of_listing(), review.comment_from_tenant(), review.bookings_booking_id());
                System.out.println(reviewString);
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());

            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void handleShowAmenities(String[] args) {
        if (!checkUserLoggedIn())
            return;

        System.out.println("Amenities:");

        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");

            List<String> amenities = listingService.getAmenitiesOfListing(Long.parseLong(listingId));

            System.out.println("Amenities for listing id: " + listingId);
            for (String amenity : amenities) {
                System.out.println(amenity);
            }

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void handleShowAvailability(String[] args) {
        if (!checkUserLoggedIn())
            return;

        System.out.println("Availabilities:");

        try {
            Options options = new Options();
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().required().desc("listing id").build());
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String listingId = cmd.getOptionValue("l");

            List<Availability> availabilities = listingService.getAvailabilitiesOfListing(Long.parseLong(listingId), LocalDate.now());

            System.out.println("Availabilities for listing id: " + listingId);
            for (Availability availability : availabilities) {
                // System.out.println(availability);
                String availabilityString = String.format("Availability id: %s, Start date: %s, End date: %s, Price per night: %s, Listing id: %s",
                        availability.availability_id(), availability.start_date(), availability.end_date(), availability.price_per_night(), availability.listings_listing_id());
                System.out.println(availabilityString);
            }

        } catch (ServiceException e) {
            System.out.println(e.getMessage());
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void handleShowListings(String[] args) {
        if (!checkUserLoggedIn())
            return;

        BigDecimal defaultSearchRadius = new BigDecimal(20);
        try {
            Options options = new Options();

            // Listings filter options
            options.addOption(Option.builder("l").longOpt("listing-id").hasArg().desc("listing id").build());
            options.addOption(Option.builder("t").longOpt("listing-types").hasArg()
                            .desc("a list of listing types, each separated by a comma").build());
            options.addOption(Option.builder("a").longOpt("address").hasArg()
                    .desc("listing address").build());
            options.addOption(Option.builder("pc").longOpt("postal-code").hasArg()
                    .desc("listing postal code").build());
            options.addOption(Option.builder("lo").longOpt("longitude").hasArg()
                    .desc("listing longitude").build());
            options.addOption(Option.builder("la").longOpt("latitude").hasArg()
                    .desc("listing latitude").build());
            options.addOption(Option.builder("ci").longOpt("city").hasArg()
                    .desc("listing city").build());
            options.addOption(Option.builder("co").longOpt("country").hasArg()
                    .desc("listing country").build());
            options.addOption(Option.builder("s").longOpt("user-sin").hasArg()
                    .desc("user sin").build());
            options.addOption(Option.builder("rad").longOpt("search-radius").hasArg()
                    .desc("Search radius. Defaults to 20 if address is not specified and is not set").build());


            // Availabilities filter options
            options.addOption(Option.builder("sd").longOpt("start-date").hasArg()
                    .desc("availability start date").build());
            options.addOption(Option.builder("ed").longOpt("end-date").hasArg()
                    .desc("availability end date").build());
            options.addOption(Option.builder("sdr").longOpt("start-date-range").hasArg()
                    .desc("availability start date range").build());
            options.addOption(Option.builder("edr").longOpt("end-date-range").hasArg()
                    .desc("availability end date range").build());
            options.addOption(Option.builder("ppn").longOpt("price-per-night").hasArg()
                    .desc("price per night").build());
            options.addOption(Option.builder("ppnmin").longOpt("price-per-night-range-min").hasArg()
                    .desc("price per night range min").build());
            options.addOption(Option.builder("ppnmax").longOpt("price-per-night-range-max").hasArg()
                    .desc("price per night range max").build());

            // Amenities filter options
            options.addOption(Option.builder("amen").longOpt("amenities").hasArg()
                    .desc("amenities (A list of amenities, each separated by comma").build());

            // add help
            options.addOption(Option.builder("h").longOpt("help").desc("show help").build());
            HelpFormatter formatter = new HelpFormatter();

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                formatter.printHelp("show listings", options);
                return;
            }

            Long listingId = cmd.hasOption("l") ? Long.parseLong(cmd.getOptionValue("l")) : null;
            String listingType = cmd.getOptionValue("t");
            String address = cmd.hasOption("a") ? cmd.getOptionValue("a").replaceAll("_", " ") : null;
            String postalCode = cmd.hasOption("pc") ? cmd.getOptionValue("pc").replaceAll("_", " ") : null;
            Double longitude = cmd.getOptionValue("lo") != null ? Double.parseDouble(cmd.getOptionValue("lo")) : null;
            Double latitude = cmd.getOptionValue("la") != null ? Double.parseDouble(cmd.getOptionValue("la")) : null;
            Point2D location = longitude != null && latitude != null ? new Point2D.Double(longitude, latitude) : null;
            String city = cmd.getOptionValue("ci");
            String country = cmd.getOptionValue("co");
            Long userSin = cmd.hasOption("s") ? Long.parseLong(cmd.getOptionValue("s")) : null;
            BigDecimal searchRadius = cmd.hasOption("rad") ? new BigDecimal(cmd.getOptionValue("rad")) :
                    ((address == null && postalCode == null && location != null ) ? defaultSearchRadius : null);

            LocalDate startDate = cmd.getOptionValue("sd") != null ? LocalDate.parse(cmd.getOptionValue("sd")) : null;
            LocalDate endDate = cmd.getOptionValue("ed") != null ? LocalDate.parse(cmd.getOptionValue("ed")) : null;
            LocalDate startDateRange = cmd.getOptionValue("sdr") != null ? LocalDate.parse(cmd.getOptionValue("sdr")) : null;
            LocalDate endDateRange = cmd.getOptionValue("edr") != null ? LocalDate.parse(cmd.getOptionValue("edr")) : null;
            BigDecimal pricePerNight = cmd.getOptionValue("ppn") != null ? new BigDecimal(cmd.getOptionValue("ppn")) : null;
            BigDecimal pricePerNightRangeMin = cmd.getOptionValue("ppnmin") != null ? new BigDecimal(cmd.getOptionValue("ppnmin")) : null;
            BigDecimal pricePerNightRangeMax = cmd.getOptionValue("ppnmax") != null ? new BigDecimal(cmd.getOptionValue("ppnmax")) : null;

            String amenities = cmd.getOptionValue("amen");
            // the listing types are separated by comma
            List<String> listingTypesList = listingType != null ? Arrays.asList(listingType.split(",")) : null;
            // The amenities are separated by comma
            List<String> amenitiesList = amenities != null ? Arrays.asList(amenities.split(",")) : null;

            ListingFilter listingFilter = new ListingFilter.Builder()
                    .withListing(
                            new Listing(
                                    listingId,
                                    (listingTypesList == null) ? null : listingTypesList.get(0),
                                    address,
                                    postalCode,
                                    location,
                                    city,
                                    country,
                                    userSin
                            )
                    )
                    .withAvailability(
                            new Availability(
                                    null,
                                    startDate,
                                    endDate,
                                    pricePerNight,
                                    listingId
                            )
                    )
                    .withAmenities(amenitiesList)
                    .withListingTypes(listingTypesList)
                    .withStartDateRange(startDateRange)
                    .withEndDateRange(endDateRange)
                    .withMinPricePerNight(pricePerNightRangeMin)
                    .withMaxPricePerNight(pricePerNightRangeMax)
                    .withSearchRadius(searchRadius)
                    .build();

            List<Listing> listings = listingService.searchListingsByFilter(listingFilter);
            System.out.println("Listings found: " + listings.size());
            for (Listing listing : listings) {
                 System.out.println(listing);
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
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
        if (!checkUserLoggedIn())
            return;

        try {
            Options options = new Options();
            options.addOption(Option.builder("b").longOpt("booking-id").hasArg().required().desc("booking id").build());

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String bookingId = cmd.getOptionValue("b");

            LocalDate today = LocalDate.parse(bookingService.getCurrDate().toString());
            // check if user is tenant or host
            if (bookingService.isTenantOfBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin))) {
                bookingService.tenantCancelBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin), today);
            }
            else if (bookingService.isHostOfBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin))) {
                bookingService.hostCancelBooking(Long.parseLong(bookingId), Long.parseLong(logged_in_user_sin), today);
            }
            else {
                System.out.println("User is not tenant or host of booking");
                return;
            }

            System.out.println("Cancelled booking with id: " + bookingId);
            
        } catch (ServiceException | org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
            return;
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


    private boolean isValidListingType(String listingType) {
        List<String> listingTypes = new ArrayList<>();
        listingTypes.add("house");
        listingTypes.add("apartment");
        listingTypes.add("guesthouse");
        listingTypes.add("hotel");

        return listingTypes.contains(listingType);
    }

    public boolean checkUserLoggedIn() {
        if (logged_in_user_sin.equals("")) {
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

    public Long getNumberOfDays(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return ChronoUnit.DAYS.between(start, end);
    }

    private void tenantReviewAdderRoutine(Long tenantID, Long bookingID){
        Scanner myScanner = new Scanner(System.in);
        String listing_rating = "";
        String host_rating = "";
        String comment = "";

        try {
            while (listing_rating.equals("")) {
                System.out.println("Enter rating for Listing (1-5)");
                listing_rating = myScanner.nextLine();
                if (!NumberUtils.isCreatable(listing_rating) || !(Integer.parseInt(listing_rating) >= 1 && Integer.parseInt(listing_rating) <= 5)) {
                    System.out.println("Rating must be between 1 and 5");
                    listing_rating = "";
                }
            }

            bookingService.tenantRateListing(tenantID, Integer.valueOf(listing_rating), bookingID, LocalDate.parse(bookingService.getCurrDate().toString()));

            while (host_rating.equals("")) {
                System.out.println("Enter rating for Host (1-5)");
                host_rating = myScanner.nextLine();
                if (!NumberUtils.isCreatable(host_rating) || !(Integer.parseInt(host_rating) >= 1 && Integer.parseInt(host_rating) <= 5)) {
                    System.out.println("Rating must be between 1 and 5");
                    host_rating = "";
                }
            }

            bookingService.tenantRateHost(tenantID, Integer.valueOf(host_rating), bookingID, LocalDate.parse(bookingService.getCurrDate().toString()));

            System.out.println("Enter comment");
            comment = myScanner.nextLine();

            bookingService.addCommentFromTenant(tenantID, comment, bookingID, LocalDate.parse(bookingService.getCurrDate().toString()));
            System.out.println("Review added");
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }

    }

    private void hostReviewAdderRoutine(Long hostID, Long bookingID){
        Scanner myScanner = new Scanner(System.in);
        String tenant_rating = "";
        String comment = "";

        try {
            while (tenant_rating.equals("")) {
                System.out.println("Enter rating for Tenant (1-5)");
                tenant_rating = myScanner.nextLine();
                if (!NumberUtils.isCreatable(tenant_rating) || !(Integer.parseInt(tenant_rating) >= 1 && Integer.parseInt(tenant_rating) <= 5)) {
                    System.out.println("Rating must be between 1 and 5");
                    tenant_rating = "";
                }
            }

            bookingService.hostRateTenant(hostID, Integer.valueOf(tenant_rating), bookingID, LocalDate.parse(bookingService.getCurrDate().toString()));

            System.out.println("Enter comment");
            comment = myScanner.nextLine();

            bookingService.addCommentFromHost(hostID, comment, bookingID, LocalDate.parse(bookingService.getCurrDate().toString()));
            System.out.println("Review added");
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void tenantReviewRemoverRoutine(Long tenantID, Long bookingID){
        try {
            bookingService.deleteCommentFromTenant(tenantID, bookingID);
            bookingService.deleteTenantRateHost(tenantID, bookingID);
            bookingService.deleteTenantRateListing(tenantID, bookingID);
            System.out.println("Review removed");
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void hostReviewRemoverRoutine(Long hostID, Long bookingID){
        try {
            bookingService.deleteCommentFromHost(hostID, bookingID);
            bookingService.deleteHostRateTenant(hostID, bookingID);
            System.out.println("Review removed");
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
    }

    private void amenityAdderRoutine(Long listingID){
        Scanner myScanner = new Scanner(System.in);
        Map<String, BigDecimal> amenities = new HashMap<>();
        try {
            for (Amenity allowedAmenity : listingService.getAllAllowedAmenities()) {
                amenities.put(allowedAmenity.amenity_name(), allowedAmenity.impact_on_revenue());
            }
        } catch (ServiceException e) {
            System.out.println(e.getMessage());
            if (e.getCause() != null) {
                System.out.println(e.getCause().getMessage());
            }
        }
        System.out.println("Amenity must be one of: " + amenities.keySet().toString());
        // print out top 5 amenities by revenue impact
        // sort amenities by revenue impact
        List<Map.Entry<String, BigDecimal>> list = new LinkedList<>(amenities.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
            public int compare(Map.Entry<String, BigDecimal> o1,
                               Map.Entry<String, BigDecimal> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        int count = 0;
        System.out.print("Top amenities to have: ");
        for (Map.Entry<String, BigDecimal> entry : list) {
            if (count == 5) {
                break;
            }
            System.out.print(entry.getKey());
            if (count != 4) {
                System.out.print(", ");
            }
            count++;
        }
        System.out.println();
        System.out.println("Enter amenity name or enter 'done' to finish adding amenities");
        while (true) {
            String amenity = myScanner.nextLine();
            if (amenity.equals("done")) {
                break;
            }
            if (!amenities.keySet().contains(amenity.toLowerCase())) {
                System.out.println("Amenity must be one of: " + amenities.keySet().toString());
                continue;
            }

            amenity = amenity.toLowerCase();
            try {
                listingService.addAmenityToListing(listingID, Long.parseLong(logged_in_user_sin), amenity);
                System.out.println("Amenity added: " + amenity + ". Expected revenue increase: " + amenities.get(amenity) + "%");
            } catch (ServiceException e) {
                System.out.println(e.getMessage());
                if (e.getCause() != null) {
                    System.out.println(e.getCause().getMessage());
                }
            }
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ConsoleLogger.setup();
        ServiceCli serviceCli = new ServiceCli();
        serviceCli.start();
    }
}
