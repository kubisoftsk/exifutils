package sk.kubisoft.exifutils.setgps;

import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.SetLocationAction;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.metadata.GpsDataSetter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SetGpsCommand {

    private final Console console;

    private final FileExplorer fileExplorer;

    private final GpsDataSetter gpsDataSetter;

    @Inject
    public SetGpsCommand(Console console, FileExplorer fileExplorer, GpsDataSetter gpsDataSetter) {
        this.console = console;
        this.fileExplorer = fileExplorer;
        this.gpsDataSetter = gpsDataSetter;
    }

    public void execute(SetGpsCommandInput input) {
        console.verboseln("Running ExifUtils Rename command with input: %s", input);

        console.println("Searching for media files...");
        List<MediaFile> mediaFiles = fileExplorer.listMediaFiles(input.sourcePaths());
        console.println("Found %d files.", mediaFiles.size());

        List<SetLocationAction> actions = new ArrayList<>();
        for (var mediaFile : mediaFiles) {
            if (input.remove()) {
                actions.add(new SetLocationAction(mediaFile, null, null));
            } else {
                actions.add(new SetLocationAction(mediaFile, input.latitude(), input.longitude()));
            }
        }

        if (actions.isEmpty()) {
            console.println("No files to process.");
            return;
        }

        console.println("Total %d files will have location set.", actions.size());
        actions.forEach((action) -> console.println("Set GPS location %s", action));

        // Confirm action or abort
        if (console.confirmAction("Do you want to continue?")) {
            console.println("Setting GPS data to files...");
            gpsDataSetter.setLocationData(actions);
        } else {
            console.println("Aborted.");
        }
    }

}
