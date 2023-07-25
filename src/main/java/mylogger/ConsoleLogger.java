package mylogger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;


public class ConsoleLogger {

    public static void setup() {
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();

        // Set the pattern for the log messages
        //String pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level %class{36} %L %M - %msg%xEx%n";
        String pattern = "%d{MM-dd HH:mm:ss} %-5level %class{36} %L %M - %msg%xEx%n";
        builder.add(
            builder.newAppender("Stdout", "CONSOLE")
                .addAttribute("target", org.apache.logging.log4j.core.appender.ConsoleAppender.Target.SYSTEM_OUT)
                .add(
                    builder.newLayout("PatternLayout")
                            .addAttribute("pattern", pattern)
                )
        );
        builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("Stdout")));

        ((LoggerContext) LogManager.getContext(false)).start(builder.build());
    }

    public static void main(String[] args) {
        setup();
        Logger logger = LogManager.getLogger(ConsoleLogger.class);
        logger.info("This is an info message");
        logger.error("This is an error message");
    }
}