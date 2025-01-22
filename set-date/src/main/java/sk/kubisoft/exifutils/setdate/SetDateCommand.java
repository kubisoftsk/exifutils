package sk.kubisoft.exifutils.setdate;

import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.metadata.MetaDataSetter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SetDateCommand {

    private final Console console;
    private final ConfigService configService;

    @Inject
    public SetDateCommand(Console console, ConfigService configService) {
        this.console = console;
        this.configService = configService;
    }

    public void execute(SetDateCommandInput input) {
        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured");
        }
        try (var metaDataSetter = new MetaDataSetter(exifToolConfig.getPath())) {
            for (var file : input.sourceFiles()) {
                console.println("Setting date and time for file: %s", file);
                metaDataSetter.setDateTime(file, input.dateTime(), input.zoneOffset());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing files", e);
        }
    }
}
