package sk.kubisoft.exifutils.cli;

public class ExifUtilsApplication {

    static {
        // Prevents SLF4J from printing unnecessary debug messages
        System.setProperty("slf4j.internal.verbosity", "WARN");
    }

    public static void main(String[] args) {
        // Bootstrap the application by creating Dagger entry component and running the CLI
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();
        ExifUtilsCli cli = new ExifUtilsCli(component.commandRunners(), component.console());
        cli.run(args);
    }

}
