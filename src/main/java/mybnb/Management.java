package mybnb;

import data.Dao;
import mylogger.ConsoleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static void main(String[] args) throws ClassNotFoundException {
        // Reset database if command line argument is "reset"
        if (args.length > 0 && args[0].equals("reset")) {
            Management management = new Management(new Dao());
            management.resetDatabase();
        }
    }
}
