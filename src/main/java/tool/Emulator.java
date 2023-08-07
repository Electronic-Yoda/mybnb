package tool;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import data.Dao;
import data.DbConfig;
import domain.*;
import mylogger.ConsoleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.BookingService;
import service.ListingService;
import service.UserService;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class Emulator {

    private final Dao dao;
    private final UserService userService;
    private final ListingService listingService;
    private final BookingService bookingService;

    private static final Logger logger = LogManager.getLogger(Emulator.class);

    String[] occupations = {
            "Engineer", "Doctor", "Teacher", "Programmer", "Nurse", "Lawyer",
            "Designer", "Architect", "Pharmacist", "Mechanic", "Chef", "Pilot",
            "Accountant", "Scientist", "Police Officer", "Firefighter", "Dentist",
            "Veterinarian", "Psychologist", "Professor", "Actor", "Musician",
            "Athlete", "Journalist", "Writer", "Artist", "Photographer",
            "Real Estate Agent", "Electrician", "Plumber", "Farmer", "Cashier",
            "Waiter", "Bartender", "Flight Attendant", "Hairdresser", "Librarian"
    };

    String[] locations = {
            "Downtown Toronto, ON",
            "North York, Toronto, ON",
            "Scarborough, Toronto, ON",
            "Etobicoke, Toronto, ON",
            "York, Toronto, ON",
            "East York, Toronto, ON",
            "leslie, Toronto, ON",
            "Don Mills, Toronto, ON",
            "Willowdale, Toronto, ON",
            "Downsview, Toronto, ON",
            "Weston, Toronto, ON",
            "York Mills, Toronto, ON",
            "Agincourt, Toronto, ON",
            "Alderwood, Toronto, ON",
            "Alexandra Park, Toronto, ON",
            "Allenby, Toronto, ON",
            "Amesbury, Toronto, ON",
            "Armour Heights, Toronto, ON",
            "Banbury, Toronto, ON",
            "Bathurst Manor, Toronto, ON",
            "Bay Street Corridor, Toronto, ON",
            "Bayview Village, Toronto, ON",
            "College Street, Toronto, ON",
            "Corktown, Toronto, ON",
            "Corso Italia, Toronto, ON",
            "Davenport, Toronto, ON",
            "Dorset Park, Toronto, ON",
            "Dovercourt Park, Toronto, ON",
            "Dufferin Grove, Toronto, ON",
            "Earlscourt, Toronto, ON",
            "Tim Hortons, Toronto, ON",
            "KFC, Toronto, ON",
            "Metro, Toronto, ON",
            "Subway, Toronto, ON",
            "Pizza Pizza, Toronto, ON",
            "Pizza Hut, Toronto, ON",
            "Popeyes, Toronto, ON",
            "McDonalds, Toronto, ON",
            "Burger King, Toronto, ON",
            "Wendys, Toronto, ON",
            "A&W, Toronto, ON",
            "Starbucks, Toronto, ON",
            "Second Cup, Toronto, ON",
            "CN Tower, Toronto, ON",
            "Rogers Centre, Toronto, ON",
            "Air Canada Centre, Toronto, ON",
            "Scotiabank Arena, Toronto, ON",
            "Ripley's Aquarium of Canada, Toronto, ON",
            "Royal Ontario Museum, Toronto, ON",
            "Art Gallery of Ontario, Toronto, ON",
            "Casa Loma, Toronto, ON",
            "Toronto Zoo, Toronto, ON",
            "High Park, Toronto, ON",
            "Ontario Science Centre, Toronto, ON",
            "Toronto Islands, Toronto, ON",
            "St. Lawrence Market, Toronto, ON",
            "Distillery District, Toronto, ON",
            "Yonge-Dundas Square, Toronto, ON",
            "Harbourfront Centre, Toronto, ON",
            "Nathan Phillips Square, Toronto, ON",
            "Kensington Market, Toronto, ON",
            "Chinatown, Toronto, ON",
            "Greektown, Toronto, ON",
            "Little Italy, Toronto, ON",
            "Little Portugal, Toronto, ON",
            "Koreatown, Toronto, ON",
            "The Annex, Toronto, ON",
            "The Beaches, Toronto, ON",
            "The Danforth, Toronto, ON",
            "The Junction, Toronto, ON",
            "Yorkville, Toronto, ON",
            "Bloor West Village, Toronto, ON",
            "Church and Wellesley, Toronto, ON",
            "Queen Street West, Toronto, ON",
            "Queen's Park, Toronto, ON",
            "University of Toronto, Toronto, ON",
            "Ryerson University, Toronto, ON",
            "George Brown College, Toronto, ON",
            "Humber College, Toronto, ON",
            "Seneca College, Toronto, ON",
            "Centennial College, Toronto, ON",
            "OCAD University, Toronto, ON",
            "York University, Toronto, ON",
            "Toronto Public Library, Toronto, ON",
            "Toronto City Hall, Toronto, ON",
            "Toronto Police Service, Toronto, ON",
            "Mcdonalds, Hamilton, ON",
            "Tim Hortons, Hamilton, ON",
            "Subway, Hamilton, ON",
            "Pizza Pizza, Hamilton, ON",
            "Pizza Hut, Hamilton, ON",
            "Popeyes, Hamilton, ON",
            "One World Trade Center, New York, NY",
            "Empire State Building, New York, NY",
            "Statue of Liberty, New York, NY",
            "Rockefeller Center, New York, NY",
            "Times Square, New York, NY",
            "Central Park, New York, NY",
            "Brooklyn Bridge, New York, NY",
            "Grand Central Terminal, New York, NY",
            "Metropolitan Museum of Art, New York, NY",
            "Museum of Modern Art, New York, NY",
            "American Museum of Natural History, New York, NY",
            "Ellis Island, New York, NY",
            "Solomon R. Guggenheim Museum, New York, NY",
            "Radio City Music Hall, New York, NY",
            "High Line, New York, NY",
            "St. Patrick's Cathedral, New York, NY",
            "MIT, Cambridge, MA",
            "Harvard University, Cambridge, MA",
            "Boston University, Boston, MA",
            "Boston College, Boston, MA",
            "Northeastern University, Boston, MA",
            "Tufts University, Boston, MA",
            "Parliament Hill, Ottawa, ON",
            "Rideau Canal, Ottawa, ON",
            "National Gallery of Canada, Ottawa, ON",
            "Canadian War Museum, Ottawa, ON",
            "Canadian Museum of History, Ottawa, ON",
            "Canadian Museum of Nature, Ottawa, ON",
            "ByWard Market, Ottawa, ON",
            "Notre-Dame Cathedral Basilica, Ottawa, ON",
            "Peace Tower, Ottawa, ON",
            "Mcdonalds, San Francisco, CA",
            "Tim Hortons, San Francisco, CA",
            "Subway, San Francisco, CA",
            "Pizza Pizza, San Francisco, CA",
            "Pizza Hut, San Francisco, CA",
            "Popeyes, San Francisco, CA",
            "Mcdonalds, San Jose, CA",
            "Tim Hortons, San Jose, CA",
            "Subway, San Jose, CA",
            "Pizza Pizza, San Jose, CA",
            // Add more specific locations as needed
    };

    String[] commercialHostAddresses = {
        "Mcdonalds, San Francisco, CA",
        "Tim Hortons, San Francisco, CA",
        "Subway, San Francisco, CA",
        "Pizza Pizza, San Francisco, CA",
        "Pizza Hut, San Francisco, CA",
        "Popeyes, San Francisco, CA",
        "Mcdonalds, San Jose, CA",
        "Tim Hortons, San Jose, CA",
        "Subway, San Jose, CA",
        "Pizza Pizza, San Jose, CA",
    };

    String[] listingTypes = {
            "House",
            "Apartment",
            "Guesthouse",
            "Hotel"
    };

    int maxNumberOfListings = 40;
    int minNumberOfListings = 1;

    public Emulator() {
        this.dao = new Dao();
        this.userService = new UserService(dao);
        this.listingService = new ListingService(dao);
        this.bookingService = new BookingService(dao);
    }

    public Emulator(Dao dao) {
        this.dao = dao;
        this.userService = new UserService(dao);
        this.listingService = new ListingService(dao);
        this.bookingService = new BookingService(dao);
    }

    public Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(Point2D.class, new Point2DAdapter())
                .create();
    }

    public void generateUserAndListingFiles(int numberOfHosts, long sinStartingValue,
                                            String userFilePath, String listingFilePath, String apiKey) {
        try {
            Faker faker = new Faker();
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();

            // Create a JSON array to hold all the data
            List<User> users = new ArrayList<>();
            List<Listing> listings = new ArrayList<>();

            Random random = new Random();

            // Pre-fetch addresses
            Set<GeocodingResult> preFetchedAddresses = new HashSet<>();

            for (String location : locations) {
                GeocodingResult[] results = GeocodingApi.geocode(context, location).await();
                preFetchedAddresses.addAll(Arrays.asList(results));
            }
            //Collections.shuffle(preFetchedAddresses); // Shuffle the addresses if you want them to be used randomly

            Iterator<GeocodingResult> addressIterator = preFetchedAddresses.iterator();

            for (long i = 0; i < numberOfHosts && addressIterator.hasNext(); i++) {
                Long sin = sinStartingValue + i;
                String name = faker.name().fullName(); // Generate a name
                String address = faker.address().streetAddress();
                LocalDate birthdate = faker.date().birthday(18, 100).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String occupation = occupations[random.nextInt(occupations.length)]; // Generate an occupation

                User user = new User(sin, name, address, birthdate, occupation);

                // Convert user to JSON and add to the array
                users.add(user);

                for (int j = 0; j < random.nextInt(maxNumberOfListings + 1 - minNumberOfListings) + minNumberOfListings
                        && addressIterator.hasNext(); j++) {
                    // Generate listing details using Geocoding API for real locations in Toronto
                    GeocodingResult result = addressIterator.next();
                    String city = null;
                    String country = null;
                    for (AddressComponent component : result.addressComponents) {
                        if (Arrays.asList(component.types).contains(AddressComponentType.LOCALITY)) {
                            city = component.longName;
                        } else if (Arrays.asList(component.types).contains(AddressComponentType.COUNTRY)) {
                            country = component.longName;
                        } else if (Arrays.asList(component.types).contains(AddressComponentType.POSTAL_CODE)) {
                            String postalCode = component.shortName;
                            // Get the short address (ex: 419 Boston Ave instead of 419 Boston Ave, Medford, MA 02155, USA)
                            // this means take the string before the first comma
                            String shortAddress = result.formattedAddress.substring(0, result.formattedAddress.indexOf(','));
                            Listing listing = new Listing(
                                    null, // listing_id can be generated based on your logic
                                    listingTypes[random.nextInt(listingTypes.length)],
                                    shortAddress,
                                    postalCode,
                                    new Point2D.Double(result.geometry.location.lng, result.geometry.location.lat),
                                    city,  // Set real city
                                    country,  // Set real country
                                    sin
                            );
                            // Convert listing to JSON and add to the array
                            listings.add(listing);
                        }
                    }
                }
            }
            Gson gson = getGson();

            String usersJson = gson.toJson(users);
            String listingsJson = gson.toJson(listings);

            // Write JSON array to file
            logger.info("Number of users: " + users.size());
            try (FileWriter file = new FileWriter(userFilePath)) {
                file.write(usersJson);
            }
            logger.info("Number of listings: " + listings.size());
            try (FileWriter file = new FileWriter(listingFilePath)) {
                file.write(listingsJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateUserAndListingFiles() {
        String currentDirectory = System.getProperty("user.dir");
        String emulatorDataDir = Paths.get(currentDirectory, "emulator_data").toString();
        if (!new java.io.File(emulatorDataDir).exists()) {
            new java.io.File(emulatorDataDir).mkdir();
        }
        String userFilePath = Paths.get(emulatorDataDir, "users.json").toString();
        String listingFilePath = Paths.get(emulatorDataDir, "listings.json").toString();

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("credentials.properties")) {
            properties.load(input);
            String apiKey = properties.getProperty("GOOGLE_MAPS_API_KEY");
            generateUserAndListingFiles(50, 3,
                    userFilePath, listingFilePath, apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataToDatabase(String userFilePath, String listingFilePath, LocalDate dateToBegin) {
        try {
            Gson gson = getGson();
            String usersJson = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(userFilePath)));
            String listingsJson = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(listingFilePath)));
            List<User> users = gson.fromJson(usersJson, new TypeToken<List<User>>() {}.getType());
            List<Listing> listings = gson.fromJson(listingsJson, new TypeToken<List<Listing>>() {}.getType());

            for (User user : users) {
                userService.addUser(user);
            }
            for (Listing listing : listings) {
                try {
                    listingService.addListing(listing);
                } catch (Exception e) {
                    logger.info("Emulator: Did not add listing: " + listing);
                    logger.info(e.getMessage());
                    if (e.getCause() != null) {
                        logger.info(e.getCause().getMessage());
                    }
                }
            }

            // get all listings because we need the listing_id
            listings = listingService.getListings();

            // add amenities
            List<String> amenities = new ArrayList<>();
            listingService.getAllAllowedAmenities().forEach(amenity -> amenities.add(amenity.amenity_name()));
            for (Listing listing : listings) {
                for (int i = 0; i < amenities.size(); i++) {
                    if (Math.random() < 0.5) {
                        listingService.addAmenityToListing(listing.listing_id(), listing.users_sin(), amenities.get(i));
                    }
                }
            }
            Random random = new Random();
            // add availabilities
            for (Listing listing : listings) {
//                LocalDate startDate = dateToBegin;
//                int plusDays = random.nextInt(30) + 1;
//                LocalDate endDate = startDate.plusDays(plusDays);
////                for (int i = 0; i < random.nextInt(20); i++) {
//                for (int i = 0; i < 20; i++) {
//                    Availability availability = new Availability(
//                            null,
//                            startDate,
//                            endDate,
//                            new BigDecimal(random.nextInt(700) + 50),
//                            listing.listing_id()
//                    );
//                    try {
//                        listingService.addAvailability(availability, listing.users_sin(), dateToBegin);
//                    } catch (Exception e) {
//                        logger.info("Emulator: Did not add availability: " + availability);
//                    }
//                    startDate = endDate.plusDays(random.nextInt(2) + 1);
////                    plusDays = random.nextInt(50) + 10;
//                    plusDays = 50;
//                    endDate = startDate.plusDays(plusDays);
//                }

                // Simply make listing available for the entire year
                LocalDate startDate = dateToBegin;
                LocalDate endDate = dateToBegin.plusDays(365);
                Availability availability = new Availability(
                        null,
                        startDate,
                        endDate,
                        new BigDecimal(random.nextInt(400) + 30).setScale(2, RoundingMode.HALF_UP),
                        listing.listing_id()
                );
                try {
                    listingService.addAvailability(availability, listing.users_sin(), dateToBegin);
                } catch (Exception e) {
                    logger.info("Emulator: Did not add availability: " + availability);
                }
            }

            // Loop through each user and have each user randomly book listings
            Collections.shuffle(users);
            for (User user : users) {
                // Get a random number of bookings for this user
                int numBookings = random.nextInt(10);
                // Get a random list of listings for this user to book
                List<Listing> listingsToBook = new ArrayList<>(listings);
                Collections.shuffle(listingsToBook);
                listingsToBook = listingsToBook.subList(0, numBookings);
                // Loop through each listing and create a booking
                for (Listing listing : listingsToBook) {
                    // Get a random start date
//                    LocalDate startDate = dateToBegin;
                    LocalDate startDate = dateToBegin.plusDays(random.nextInt(300));
                    int plusDays = random.nextInt(10) + 1;
                    LocalDate endDate = startDate.plusDays(plusDays);
                    // Create the booking
                    Booking booking = new Booking(
                            null,
                            startDate,
                            endDate,
                            dateToBegin,
                            null,
                            List.of("visa", "mastercard", "american_express").get(random.nextInt(3)),
                            random.nextLong(111111111) + 100000000,
                            user.sin(),
                            listing.listing_id()
                    );
                    try {
                        bookingService.addBooking(booking);
                    } catch (Exception e) {
                        logger.info("Emulator: Did not book: " + booking);
                        logger.info(e.getMessage());
                        if (e.getCause() != null) {
                            logger.info(e.getCause().getMessage());
                        }
                    }
                }
            }
            List<Booking> bookings = bookingService.getBookings();
            // cancel some bookings
            // Loop through each user and have each user randomly cancel bookings
            Collections.shuffle(users);
            for (User user : users) {
                // Get the bookings for this user
                List<Booking> userBookings = bookingService.getBookingsOfUser(user.sin());
                if (userBookings.isEmpty()) {
                    continue;
                }
                // Get a random number of bookings to cancel
                int numBookingsToCancel = random.nextInt(userBookings.size());
                // Get a random list of bookings to cancel
                List<Booking> bookingsToCancel = new ArrayList<>(userBookings);
                Collections.shuffle(bookingsToCancel);
                bookingsToCancel = bookingsToCancel.subList(0, numBookingsToCancel);
                // Loop through each booking and cancel it
                for (Booking booking : bookingsToCancel) {
                    try {
                        // Either tenant or host can cancel. 50% chance of each
                        if (Math.random() < 0.5) {
                            bookingService.tenantCancelBooking(booking.booking_id(), user.sin(), dateToBegin);
                        } else {
                            // We need to find the host id for this listing
                            Long hostId = listingService.getListing(booking.listings_listing_id()).users_sin();
                            bookingService.hostCancelBooking(booking.booking_id(), hostId, dateToBegin);
                        }
                    } catch (Exception e) {
                        logger.info("Emulator: Did not cancel booking: " + booking);
                        logger.info(e.getMessage());
                        if (e.getCause() != null) {
                            logger.info(e.getCause().getMessage());
                        }
//                        e.printStackTrace();
                    }
                }
            }

            // add reviews
            // create a review map where key is 1 to 5 and value is a list of unique noun phrases corresponding to that rating
            Map<Integer, List<String>> reviewMap = new HashMap<>();
            reviewMap.put(1, List.of(
                    "Terrible place",
                    "Horrible place",
                    "Awful environment",
                    "Beyond terrible place"
            ));
            reviewMap.put(2, List.of(
                    "Bad place",
                    "Unpleasant place",
                    "Unsatisfactory place",
                    "Unpleasant environment",
                    "Unsatisfactory environment"
            ));
            reviewMap.put(3, List.of(
                    "Okay place",
                    "Average place",
                    "Satisfactory place",
                    "Okay environment",
                    "Average environment"
            ));
            reviewMap.put(4, List.of(
                    "Good place",
                    "Great place",
                    "Excellent place",
                    "Good environment",
                    "Satisfactory environment"
            ));
            reviewMap.put(5, List.of(
                    "Amazing place",
                    "Wonderful place",
                    "Fantastic place",
                    "Amazing environment",
                    "best environment"
            ));

            // Note: before adding reviews, we need to fake the time so that the bookings are in the past
            // This is because the booking service will not allow reviews to be added for bookings in the future
            for (Listing listing : listings) {
                LocalDate currentDate = LocalDate.of(2022, 1, 1);
                for (int i = 0; i < 6; i++) {
                    // add availability for this listing
                    Availability availability = new Availability(
                            null,
                            currentDate,
                            currentDate.plusDays(random.nextInt(20) + 1),
                            new BigDecimal(String.valueOf(random.nextInt(400) + 30)).setScale(2, RoundingMode.HALF_UP),
                            listing.listing_id()
                    );
                    currentDate = availability.end_date().plusDays(random.nextInt(10) + 1);
                    try {
                        listingService.addAvailability(availability, listing.users_sin(), availability.start_date());
                        // also add a booking for this availability
                        // Get a random user as the tenant
                        User user = users.get(random.nextInt(users.size()));
                        Booking booking = new Booking(
                                null,
                                availability.start_date(),
                                availability.end_date(),
                                availability.start_date(),
                                null,
                                List.of("visa", "mastercard", "american_express").get(random.nextInt(3)),
                                random.nextLong(111111111) + 100000000,
                                user.sin(),
                                listing.listing_id()
                        );
                        Long bookingID = bookingService.addBooking(booking);
                        // get the booking because it has the booking id

                        // also add a review for this booking
                        // get a random rating from the review map
                        int randomIndex = random.nextInt(5);
                        int rating = new ArrayList<>(reviewMap.keySet()).get(randomIndex);
                        String reviewText = reviewMap.get(rating).get(random.nextInt(reviewMap.get(rating).size()));
                        // get tenant id and host id
                        Long tenantID = user.sin();
                        Long hostID = listingService.getListing(listing.listing_id()).users_sin();
                        // change the current date to be after the booking date
                        LocalDate bookingEndDate = availability.end_date().plusDays(1);
                        // add the review
                        bookingService.tenantRateListing(
                                tenantID,
                                rating,
                                bookingID,
                                bookingEndDate
                        );
                        bookingService.tenantRateHost(
                                tenantID,
                                rating,
                                bookingID,
                                bookingEndDate
                        );
                        bookingService.addCommentFromTenant(
                                tenantID,
                                reviewText,
                                bookingID,
                                bookingEndDate
                        );
                        bookingService.addCommentFromHost(
                                hostID,
                                "Nice tenant",
                                bookingID,
                                bookingEndDate
                        );
                        bookingService.hostRateTenant(
                                hostID,
                                rating,
                                bookingID,
                                bookingEndDate
                        );

                    } catch (Exception e) {
                        logger.info("Emulator: Did not add one fake review");
                        logger.info(e.getMessage());
                        if (e.getCause() != null) {
                            logger.info(e.getCause().getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadDataToDatabase() {
        String currentDirectory = System.getProperty("user.dir");
        String emulatorDataDir = Paths.get(currentDirectory, "emulator_data").toString();
        String userFilePath = Paths.get(emulatorDataDir, "users.json").toString();
        String listingFilePath = Paths.get(emulatorDataDir, "listings.json").toString();
        loadDataToDatabase(userFilePath, listingFilePath, LocalDate.of(2023, 8, 1));
    }

    public void showLoadedData() {
        try {
            System.out.println("Users:");
            userService.getUsers().forEach(System.out::println);
            System.out.println("Listings:");
            for (Listing listing : listingService.getListings()) {
                System.out.println(listing);
                System.out.println("Amenities:");
                listingService.getAmenitiesOfListing(listing.listing_id()).forEach(System.out::println);
                System.out.println("Availabilities:");
                listingService.getAvailabilitiesOfListing(listing.listing_id(), LocalDate.of(2023, 7, 20)).forEach(System.out::println);
                System.out.println("Reviews:");
                bookingService.getReviewsOfListing(listing.listing_id()).forEach(System.out::println);
            }
            System.out.println("Cancelled Bookings:");
            bookingService.getCancelledBookings().forEach(System.out::println);

            System.out.println("Number of users: " + userService.getUsers().size());
            System.out.println("Number of listings: " + listingService.getListings().size());
            System.out.println("Number of bookings: " + bookingService.getBookings().size());
            System.out.println("Number of cancelled bookings: " + bookingService.getCancelledBookings().size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
//        ConsoleLogger.setup();
//        Emulator emulator = new Emulator();
//        emulator.generateUserAndListingFiles();

//        DbConfig dbConfig = new DbConfig();
//        dbConfig.resetTables();
//        emulator.loadDataToDatabase();
//        emulator.showLoadedData();
//        dbConfig.resetTables();
    }
}
