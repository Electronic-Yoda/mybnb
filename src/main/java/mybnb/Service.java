package mybnb;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import mylogger.ConsoleLogger;

public class Service {
    private final Dao dao = new Dao();
    private static final Logger logger = LogManager.getLogger(Service.class);

    public void setUpDb() {
        try {
            dao.createTables();
        } catch (Exception e) {
            logger.error("Error while setting up database: " + e.getMessage(), e);
        }
    }

    public void resetDb() {
        try {
            dao.dropTables();
            dao.createTables();
        } catch (Exception e) {
            logger.error("Error while resetting database: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // Only for testing purposes
        ConsoleLogger.setup();
        Service service = new Service();
        service.resetDb();
        logger.info("Database reset successfully");
    }

}
