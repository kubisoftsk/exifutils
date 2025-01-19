package sk.kubisoft.exifutils.cli.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

public final class LoggingUtils {

    private LoggingUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static Optional<Path> getCurrentLogFile() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Find FileAppender
        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext();) {
                Appender<ILoggingEvent> appender = index.next();

                if (appender instanceof FileAppender<ILoggingEvent> fileAppender) {
                    String file = fileAppender.getFile();
                    return Optional.of(Paths.get(file).normalize());
                }
            }
        }

        // No FileAppender found
        return Optional.empty();
    }
}
