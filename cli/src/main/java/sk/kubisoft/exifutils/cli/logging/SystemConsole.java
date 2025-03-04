package sk.kubisoft.exifutils.cli.logging;

import org.slf4j.Logger;
import sk.kubisoft.exifutils.core.logging.Console;

import java.util.Scanner;

public class SystemConsole implements Console {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SystemConsole.class);

    private final Scanner scanner;

    private boolean verbose = false;

    private boolean alwaysTrue = false;

    public SystemConsole() {
        this.scanner = new Scanner(System.in);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scanner != null) {
                scanner.close();
            }
        }));
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean confirmAction(String message) {
        if (alwaysTrue) {
            return true;
        }
        while (true) {
            System.out.print(message + " (y/n/a) or (yes/no/all): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("y") || response.equals("yes") || response.equals("a") || response.equals("all")) {
                if (response.equals("a") || response.equals("all")) {
                    alwaysTrue = true;
                }
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            }
            System.out.println("Please answer 'y', 'n' or 'a'");
        }
    }

    @Override
    public void print(String message) {
        System.out.print(message);
        logger.info(message);
    }

    @Override
    public void print(String format, Object... args) {
        String message = String.format(format, args);
        System.out.print(message);
        logger.info(message);
    }

    @Override
    public void progress(String format, Object... args) {
        String message = String.format(format, args);
        System.out.print("\33[2K\r" + message); // Special character sequence to clear line and move cursor to beginning
        System.out.flush();
        logger.debug(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void println(String format, Object... args) {
        String message = String.format(format, args);
        System.out.println(message);
        logger.info(message);
    }

    @Override
    public void verbose(String message) {
        if (verbose) {
            System.out.print(message);
            logger.debug(message);
        }
    }

    @Override
    public void verbose(String format, Object... args) {
        String message = String.format(format, args);
        logger.debug(message);
        if (verbose) {
            System.out.print(message);
        }
    }

    @Override
    public void verboseln(String message) {
        logger.debug(message);
        if (verbose) {
            System.out.println(message);
        }
    }

    @Override
    public void verboseln(String format, Object... args) {
        String message = String.format(format, args);
        logger.debug(message);
        if (verbose) {
            System.out.println(message);
        }
    }

    @Override
    public void error(String message) {
        System.err.print(message);
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        System.err.print(message);
        logger.error(message, t);
    }

    @Override
    public void error(String format, Object... args) {
        String message = String.format(format, args);
        System.err.print(message);
        logger.error(message);
    }

    @Override
    public void error(String format, Throwable t, Object... args) {
        String message = String.format(format, args);
        System.err.print(message);
        logger.error(message, t);
    }

    @Override
    public void errorln(String message) {
        System.err.println(message);
        logger.error(message);
    }

    @Override
    public void errorln(String message, Throwable t) {
        System.err.println(message);
        logger.error(message, t);
    }

    @Override
    public void errorln(String format, Object... args) {
        String message = String.format(format, args);
        System.err.println(message);
        logger.error(message);
    }

    @Override
    public void errorln(String format, Throwable t, Object... args) {
        String message = String.format(format, args);
        System.err.println(message);
        logger.error(message, t);
    }
}
