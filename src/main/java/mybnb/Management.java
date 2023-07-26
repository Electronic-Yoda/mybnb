package mybnb;

import data.Dao;
import domain.Amenity;
import mylogger.ConsoleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Management {
    private final Dao dao;
    private static final Logger logger = LogManager.getLogger(Management.class);

    public Management(Dao dao) {
        this.dao = dao;
    }

    // ============= Setups and resets =============
    public void setUpDatabase() {
        dao.createTables();
        logger.info("Database setup successfully");
    }

    public void resetDatabase() {
        dao.dropTables();
        dao.createTables();
        logger.info("Database reset tables successfully");
    }

    public List<Amenity> getAllAmenities() {
        return dao.getAmenities();
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // Reset database if command line argument is "reset"

        Management management = new Management(new Dao());
//        if (args.length > 0 && args[0].equals("reset")) {
//            management.resetDatabase();
//        }

        management.resetDatabase();
        System.out.println("available amenities:");
        for (Amenity amenity : management.getAllAmenities()) {
            System.out.println(amenity);
        }
    }
}
