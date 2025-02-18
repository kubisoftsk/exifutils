package sk.kubisoft.exifutils.setgps;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetGpsCommandRunner implements CommandRunner {

    private static final Option DELETE = Option.builder()
            .option("d")
            .longOpt("delete")
            .desc("Delete GPS information from the file.")
            .build();

    private static final Option COORDINATES = Option.builder()
            .option("c")
            .longOpt("coordinates")
            .hasArg()
            .desc("GPS coordinates in decimal format as 'latitude,longitude' (e.g., '48.12345678,17.12345678'). Positive values indicate North/East, negative values indicate South/West.")
            .build();

    private final Console console;

    private final SetGpsCommand setGpsCommand;

    @Inject
    public SetGpsCommandRunner(Console console, SetGpsCommand setGpsCommand) {
        this.console = console;
        this.setGpsCommand = setGpsCommand;
    }

    @Override
    public int runCommand(CommandLine command) {
        try {
            SetGpsCommandInput input = parseInput(command);
            setGpsCommand.execute(input);
            return 0;
        } catch (ParseException e) {
            console.errorln("Error parsing command arguments", e);
            return 1;
        } catch (RuntimeException e) {
            console.errorln("Error executing " + getCommandName() + " command", e);
            return 1;
        }
    }

    private SetGpsCommandInput parseInput(CommandLine cmd) throws ParseException {
        List<Path> sourceFiles = new ArrayList<>();
        String[] args = cmd.getArgs();
        for (String sourceArg : args) {
            Path sourceFile = Paths.get(sourceArg);
            if (!Files.exists(sourceFile)) {
                throw new ParseException("Source file/directory does not exist: " + sourceArg);
            }
            if (!Files.isReadable(sourceFile)) {
                throw new ParseException("Cannot read source file: " + sourceArg);
            }
            sourceFiles.add(sourceFile);
        }
        if (cmd.hasOption(DELETE)) {
            return new SetGpsCommandInput(sourceFiles, null, null, true);
        } else {
            String coordinatesString = cmd.getOptionValue(COORDINATES);
            if (StringUtils.isBlank(coordinatesString)) {
                throw new ParseException("GPS coordinates must be provided.");
            }
            var coordinates = parseCoordinates(coordinatesString);
            return new SetGpsCommandInput(sourceFiles, coordinates.latitude(), coordinates.longitude(), false);
        }
    }

    private Coordinates parseCoordinates(String coordinates) throws ParseException {
        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            throw new ParseException("Invalid GPS coordinates format: " + coordinates);
        }
        try {
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            return new Coordinates(latitude, longitude);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid GPS coordinates format: " + coordinates);
        }
    }

    private record Coordinates(double latitude, double longitude) {}

    @Override
    public String getCommandName() {
        return "set-gps";
    }

    @Override
    public String getCommandDescription() {
        return "Sets GPS coordinates in the EXIF metadata of the given files.";
    }

    @Override
    public Options getOptions() {
        var options = new Options();
        options.addOption(DELETE);
        options.addOption(COORDINATES);
        return options;
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return  List.of(
                new CommandArgument.Builder("FILE|DIR")
                        .multiple()
                        .build()
        );
    }
}
