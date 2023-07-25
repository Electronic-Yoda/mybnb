package mybnb;

import mylogger.ConsoleLogger;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        ConsoleLogger.setup();

        // Create components
        // Reset database if command line argument is "reset"
        Service service = new Service();
        if (args.length > 0 && args[0].equals("reset")) {
            service.resetDb();
        }
        // Start CLI (command line interface)
        ServiceCli serviceCli = new ServiceCli(service);
        serviceCli.run();
    }
}
