package tool;

import data.Dao;
import data.DbConfig;
import domain.Listing;
import exception.ServiceException;
import mylogger.ConsoleLogger;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import service.BookingService;
import service.ListingService;
import service.ReportService;
import service.UserService;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ManagementCli {
    private static final Logger logger = LogManager.getLogger(ServiceCli.class);
    private final Dao dao = new Dao(
            "jdbc:mysql://localhost:3307/mydb",
            "root",
            "");
    private final UserService userService = new UserService(dao);
    private final ListingService listingService = new ListingService(dao);
    private final BookingService bookingService = new BookingService(dao);
    private final Emulator emulator = new Emulator(dao);
    private final DbConfig dbConfig = new DbConfig();
    private final ReportService reportService = new ReportService(dao);
    private Thread reportThread = null;
    private volatile boolean shouldRunReport = false;
    private volatile LocalDate startDateRange = LocalDate.now();
    private volatile LocalDate endDateRange = LocalDate.now().plusDays(100);


    public void start() {
        try {
            Terminal terminal = TerminalBuilder.terminal();

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new org.jline.reader.impl.DefaultParser())
                    .history(new DefaultHistory())
                    .completer(new StringsCompleter("load", "reset", "run", "quit", "set"))
                    .build();

            while (true) {
                String line = reader.readLine("<ManagementCLI> ");
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
                    case "load":
                        handleLoadCommand(subCommand, commandArgs);
                        break;
                    case "reset":
                        handleResetCommand(subCommand, commandArgs);
                        break;
                    case "run":
                        handleRunCommand(subCommand, commandArgs);
                        break;
                    case "stop":
                        handleStopCommand(subCommand, commandArgs);
                        break;
                    case "quit":
                        System.out.println("Exiting...");
                        return;
                    case "set":
                        handleSetCommand(subCommand, commandArgs);
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
    private void handleLoadCommand(String subCommand, String[] commandArgs) {
        switch (subCommand) {
            case "testData":
                handleLoadTestDataCommand();
                break;
            default:
                System.out.println("Unknown subcommand: " + subCommand);
                break;
        }
    }

    private void handleLoadTestDataCommand() {
        System.out.println("Loading test data...");
        emulator.loadDataToDatabase();
        emulator.showLoadedData();
    }

    private void handleResetCommand(String subCommand, String[] commandArgs) {
        switch (subCommand) {
            case "database":
                handleResetDatabaseCommand();
                break;
            default:
                System.out.println("Unknown subcommand: " + subCommand);
                break;
        }
    }

    private void handleResetDatabaseCommand() {
        System.out.println("Resetting database...");
        try {
            dbConfig.resetTables();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleRunCommand(String subCommand, String[] commandArgs) {
        switch (subCommand) {
            case "reports":
                handleRunReportsCommand();
                break;
            default:
                System.out.println("Unknown subcommand: " + subCommand);
                break;
        }
    }

    private void handleRunReportsCommand() {
        System.out.println("Running reports...");
        shouldRunReport = true;

        reportThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ReportWindow reportWindow = new ReportWindow();

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reportWindow.setVisible(true);
                    }
                });

                try {
                    while (shouldRunReport) {
                        final Map<String, Long> numberOfBookingsInDateRangePerCity
                                = reportService.getNumberOfBookingsInDateRangePerCity(
                                startDateRange, endDateRange);

                        final Map<String, Map<String, Long>> numberOfBookingsInDateRangePerPostalCodePerCity
                                = reportService.getNumberOfBookingsInDateRangePerPostalCodePerCity(startDateRange, endDateRange);

                        final List<Listing> allListings = listingService.getListings();

                        final Map<String, Long> numberOfListingsPerCountry
                                = reportService.getNumberOfListingsPerCountry(allListings);

                        final Map<String, Map<String, Long>> numberOfListingsPerCityPerCountry
                                = reportService.getNumberOfListingsPerCityPerCountry(allListings);

                        final Map<String, Map<String, Map<String, Long>>> numberOfListingsPerPostalCodePerCityPerCountry
                                = reportService.getNumberOfListingsPerCityPerCountryPerPostalCode(allListings);


                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                reportWindow.addText(String.format("REPORT (%s, %s)",
                                        LocalDate.now().toString(), LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

                                reportWindow.addText(String.format(
                                        "Number of bookings per city from %s to %s:", startDateRange.toString(), endDateRange.toString()));
                                numberOfBookingsInDateRangePerCity.forEach((city, numberOfBookings) -> {
                                    reportWindow.addText(String.format("%s: %d", city, numberOfBookings));
                                });

                                reportWindow.addText("");

                                reportWindow.addText(String.format(
                                        "Number of bookings per postal code per city from %s to %s:", startDateRange.toString(), endDateRange.toString()));
                                numberOfBookingsInDateRangePerPostalCodePerCity.forEach((city, postalCodeMap) -> {
                                    reportWindow.addText(String.format("%s:", city));
                                    postalCodeMap.forEach((postalCode, numberOfBookings) -> {
                                        reportWindow.addText(String.format("  %s: %d", postalCode, numberOfBookings));
                                    });
                                });

                                reportWindow.addText("");

                                numberOfListingsPerCountry.forEach((country, numberOfListings) -> {
                                    reportWindow.addText(String.format("Number of listings in %s: %d", country, numberOfListings));
                                });

                                reportWindow.addText("");

                                numberOfListingsPerCityPerCountry.forEach((country, cityMap) -> {
                                    reportWindow.addText(String.format("Number of listings per city in %s:", country));
                                    cityMap.forEach((city, numberOfListings) -> {
                                        reportWindow.addText(String.format("  %s: %d", city, numberOfListings));
                                    });
                                });

                                reportWindow.addText("");

                                numberOfListingsPerPostalCodePerCityPerCountry.forEach((country, cityMap) -> {
                                    reportWindow.addText(String.format("Number of listings per postal code per city in %s:", country));
                                    cityMap.forEach((city, postalCodeMap) -> {
                                        reportWindow.addText(String.format("  %s:", city));
                                        postalCodeMap.forEach((postalCode, numberOfListings) -> {
                                            reportWindow.addText(String.format("    %s: %d", postalCode, numberOfListings));
                                        });
                                    });
                                });
                                reportWindow.addText("==================================================");
                            }
                        });
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ServiceException e) {
                    System.out.println(e.getMessage());
                    if (e.getCause() != null)
                        System.out.println(e.getCause().getMessage());
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            reportWindow.dispose();
                        }
                    });
                }
            }
        });

        reportThread.start();
    }

    private void handleStopCommand(String subCommand, String[] commandArgs) {
        switch (subCommand) {
            case "reports":
                handleStopReportsCommand();
                break;
            default:
                System.out.println("Unknown subcommand: " + subCommand);
                break;
        }
    }

    private void handleStopReportsCommand() {
        try {
            System.out.println("Stopping reports...");
            shouldRunReport = false;  // Stop the loop in the reporting thread
            reportThread.interrupt();  // Interrupt the sleep call if necessary
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleSetCommand(String subCommand, String[] commandArgs) {
        switch (subCommand) {
            case "dateRange":
                handleSetDateRangeCommand(commandArgs);
                break;
            default:
                System.out.println("Unknown subcommand: " + subCommand);
                break;
        }
    }

    private void handleSetDateRangeCommand(String[] commandArgs) {
        try {
            Options options = new Options();
            options.addOption(Option.builder("sdr").longOpt("start-date-range").hasArg()
                    .desc("availability start date range").build());
            options.addOption(Option.builder("edr").longOpt("end-date-range").hasArg()
                    .desc("availability end date range").build());

            options.addOption(Option.builder("h").longOpt("help").desc("show help").build());
            HelpFormatter formatter = new HelpFormatter();

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, commandArgs);

            if (cmd.hasOption("h")) {
                formatter.printHelp("set dateRange", options);
                return;
            }
            String startDateRange = cmd.getOptionValue("sdr");
            String endDateRange = cmd.getOptionValue("edr");

            if (startDateRange == null || endDateRange == null) {
                System.out.println("Please enter a start date range and end date range");
                return;
            }
            System.out.println("Setting date range for reports." +
                    "Affected reports include: \n" +
                    "Number of bookings in date range per city\n" +
                    "Ranking of renters by time period, ");
            System.out.println("Start date range: " + startDateRange);
            System.out.println("End date range: " + endDateRange);
            this.startDateRange = LocalDate.parse(startDateRange);
            this.endDateRange = LocalDate.parse(endDateRange);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        ConsoleLogger.setup();
        ManagementCli cli = new ManagementCli();
        cli.start();
    }
}
