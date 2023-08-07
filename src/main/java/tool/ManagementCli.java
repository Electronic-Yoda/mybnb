package tool;

import data.Dao;
import data.DbConfig;
import exception.ServiceException;
import mylogger.ConsoleLogger;
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
import service.Report;
import service.UserService;
import java.awt.*;


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
    private final Report report = new Report(dao);
    private Thread reportThread = null;
    private volatile boolean shouldRunReport = false;


    public void start() {
        try {
            Terminal terminal = TerminalBuilder.terminal();

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(new org.jline.reader.impl.DefaultParser())
                    .history(new DefaultHistory())
                    .completer(new StringsCompleter("load", "reset", "run", "quit"))
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
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                reportWindow.addText("Report Data"); // Replace "Report Data" with actual data
                            }
                        });
                        Thread.sleep(2000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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

    public static void main(String[] args) {
        ConsoleLogger.setup();
        ManagementCli cli = new ManagementCli();
        cli.start();
    }
}
