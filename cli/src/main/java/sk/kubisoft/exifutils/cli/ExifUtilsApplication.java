package sk.kubisoft.exifutils.cli;

public class ExifUtilsApplication {

    public static void main(String[] args) {
        // Bootstrap the application by creating Dagger entry component and running the CLI
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();
        ExifUtilsCli cli = new ExifUtilsCli(component.commandRunners());
        cli.run(args);
    }

}
