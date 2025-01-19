package sk.kubisoft.exifutils.cli.logging;

import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Scanner;

@Singleton
public class SystemConsole implements Console {

    private final Scanner scanner;

    private boolean verbose = false;

    @Inject
    public SystemConsole() {
        this.scanner = new Scanner(System.in);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (scanner != null) {
                scanner.close();
            }
        }));
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean confirmAction(String message) {
        while (true) {
            System.out.print(message + " (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            }
            System.out.println("Please answer 'y' or 'n'");
        }
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
    }

    @Override
    public void verbose(String message) {
        if (verbose) {
            print(message);
        }
    }

    @Override
    public void verboseln(String message) {
        if (verbose) {
            println(message);
        }
    }

    @Override
    public void error(String message) {
        System.err.print(message);
    }

    @Override
    public void errorln(String message) {
        System.err.println(message);
    }
}
