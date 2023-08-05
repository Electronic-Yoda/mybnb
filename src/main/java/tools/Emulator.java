package tools;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import data.Dao;
import data.DbConfig;
import domain.*;
import service.BookingService;
import service.ListingService;
import service.UserService;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class Emulator {

    private final Dao dao;
    private final UserService userService;
    private final ListingService listingService;
    private final BookingService bookingService;

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
            // Add more specific locations as needed
    };

    String[] listingTypes = {
            "House",
            "Apartment",
            "Guesthouse",
            "Hotel"
    };

    int maxNumberOfListings = 10;
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
                    for (AddressComponent component : result.addressComponents) {
                        if (Arrays.asList(component.types).contains(AddressComponentType.POSTAL_CODE)) {
                            String postalCode = component.shortName;
                            Listing listing = new Listing(
                                    null, // listing_id can be generated based on your logic
                                    listingTypes[random.nextInt(listingTypes.length)],
                                    result.formattedAddress,
                                    postalCode,
                                    new BigDecimal(result.geometry.location.lng),
                                    new BigDecimal(result.geometry.location.lat),
                                    "Toronto",
                                    "Canada",
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
            System.out.println("Number of users: " + users.size());
            try (FileWriter file = new FileWriter(userFilePath)) {
                file.write(usersJson);
            }
            System.out.println("Number of listings: " + listings.size());
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

    public void loadDataToDatabase(String userFilePath, String listingFilePath) {
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
                    System.out.println("Emulator: Did not add listing: " + listing);
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
                LocalDate startDate = LocalDate.of(2023, 8, 1);
                int plusDays = random.nextInt(30) + 1;
                LocalDate endDate = startDate.plusDays(plusDays);
                for (int i = 0; i < random.nextInt(20); i++) {
                    Availability availability = new Availability(
                            null,
                            startDate,
                            endDate,
                            new BigDecimal(random.nextInt(700) + 50),
                            listing.listing_id()
                    );
                    try {
                        listingService.addAvailability(availability, listing.users_sin());
                    } catch (Exception e) {
                        System.out.println("Emulator: Did not add availability: " + availability);
                    }
                    startDate = endDate.plusDays(random.nextInt(15) + 1);
                    plusDays = random.nextInt(30) + 1;
                    endDate = startDate.plusDays(plusDays);
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
        loadDataToDatabase(userFilePath, listingFilePath);
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
                listingService.getAvailabilitiesOfListing(listing.listing_id()).forEach(System.out::println);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        DbConfig dbConfig = new DbConfig();
        dbConfig.resetTables();
        Emulator emulator = new Emulator();
        emulator.loadDataToDatabase();
        emulator.showLoadedData();
    }
}
