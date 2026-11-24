package sk.kubisoft.exifutils.cli;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileFilter;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.FileNameAnalyzer;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;
import sk.kubisoft.exifutils.core.metadata.GpsDataSetter;
import sk.kubisoft.exifutils.info.InfoCommand;
import sk.kubisoft.exifutils.info.InfoCommandInputParser;
import sk.kubisoft.exifutils.info.InfoCommandRunner;
import sk.kubisoft.exifutils.rename.RenameCommand;
import sk.kubisoft.exifutils.rename.RenameCommandRunner;
import sk.kubisoft.exifutils.setdate.SetDateCommand;
import sk.kubisoft.exifutils.setdate.SetDateCommandRunner;
import sk.kubisoft.exifutils.setgps.SetGpsCommand;
import sk.kubisoft.exifutils.setgps.SetGpsCommandRunner;
import sk.kubisoft.exifutils.shiftdate.ShiftDateCommand;
import sk.kubisoft.exifutils.shiftdate.ShiftDateCommandRunner;
import sk.kubisoft.exifutils.sort.MediaFileSorter;
import sk.kubisoft.exifutils.sort.SortCommand;
import sk.kubisoft.exifutils.sort.SortCommandRunner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Verifies that --include/--exclude reach {@link FileExplorer} in every command that walks files, and that no command
 * asks for confirmation when there is nothing to process.
 */
@ExtendWith(MockitoExtension.class)
class FileFilterCommandsTest {

    @Mock
    private Console console;

    @Mock
    private ConfigService configService;

    @Mock
    private FileExplorer fileExplorer;

    @Mock
    private MediaAnalyzer mediaAnalyzer;

    @Mock
    private MediaFileSorter mediaFileSorter;

    @Mock
    private FileNameAnalyzer fileNameAnalyzer;

    @Mock
    private MediaFileNameUtils fileNameUtils;

    @Mock
    private DuplicatePreProcessor duplicatePreProcessor;

    @Mock
    private FileMover fileMover;

    @Mock
    private ExifDateSetter exifDateSetter;

    @Mock
    private GpsDataSetter gpsDataSetter;

    private Path outputDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        outputDir = tempDir.resolve("out");
        lenient().when(fileExplorer.listMediaFiles(any(), any(), any())).thenReturn(List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"info", "sort", "set-date", "shift-date", "set-gps", "rename"})
    void commandShouldPassFilterToFileExplorer(String commandName) throws ParseException {
        int exitCode = run(commandName, "-i", "*.{jpg,JPG}", "--include", "*.png", "-x", "*-WA*", "--exclude", "Screenshot_*");

        var captor = ArgumentCaptor.forClass(FileFilter.class);
        verify(fileExplorer).listMediaFiles(any(), any(), captor.capture());
        var filter = captor.getValue();
        assertThat(filter.matches(Path.of("IMG_0001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("IMG_0001.JPG"))).isTrue();
        assertThat(filter.matches(Path.of("picture.png"))).isTrue();
        assertThat(filter.matches(Path.of("video.mp4"))).isFalse();
        assertThat(filter.matches(Path.of("IMG-20240715-WA0001.jpg"))).isFalse();
        assertThat(filter.matches(Path.of("Screenshot_1.png"))).isFalse();
        assertThat(exitCode).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"info", "sort", "set-date", "shift-date", "set-gps", "rename"})
    void commandShouldNotAskForConfirmationWhenNoFilesAreFound(String commandName) throws ParseException {
        int exitCode = run(commandName, "-x", "*");

        assertThat(exitCode).isZero();
        verify(console, never()).confirmAction(any());
        verifyNoInteractions(fileMover, exifDateSetter, gpsDataSetter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"info", "sort", "set-date", "shift-date", "set-gps", "rename"})
    void commandShouldRejectInvalidPattern(String commandName) throws ParseException {
        int exitCode = run(commandName, "-x", "regex:(");

        assertThat(exitCode).isEqualTo(2);
        assertThat(outputDir).doesNotExist();
        verifyNoInteractions(fileExplorer);
        var captor = ArgumentCaptor.forClass(ParseException.class);
        verify(console).errorln(any(), captor.capture());
        assertThat(captor.getValue()).hasMessageContaining("Invalid --exclude pattern 'regex:('");
    }

    private int run(String commandName, String... filterArgs) throws ParseException {
        var args = new ArrayList<>(List.of(filterArgs));
        args.addAll(switch (commandName) {
            case "sort" -> List.of("-o", outputDir.toString());
            case "set-date" -> List.of("-d", "2024-07-15 10:00:00", "-z", "Europe/Bratislava", "-r");
            case "shift-date" -> List.of("-d", "PT1H", "-r");
            case "set-gps" -> List.of("-c", "48.1,17.1");
            default -> List.of();
        });
        args.add("somedir");

        var runner = createRunner(commandName);
        var cmd = new DefaultParser().parse(runner.getOptions(), args.toArray(String[]::new));
        assertThat(cmd.getArgs()).containsExactly("somedir");
        return runner.runCommand(cmd);
    }

    private CommandRunner createRunner(String commandName) {
        return switch (commandName) {
            case "info" -> new InfoCommandRunner(console, new InfoCommandInputParser(),
                    new InfoCommand(fileExplorer, console, mediaAnalyzer));
            case "sort" -> new SortCommandRunner(console, new SortCommand(fileExplorer, mediaFileSorter, mediaAnalyzer,
                    fileMover, console, exifDateSetter), configService);
            case "set-date" -> new SetDateCommandRunner(console, new SetDateCommand(console, configService, fileNameAnalyzer,
                    mediaAnalyzer, fileExplorer, exifDateSetter, fileNameUtils, duplicatePreProcessor, fileMover));
            case "shift-date" -> new ShiftDateCommandRunner(console, new ShiftDateCommand(console, fileExplorer, exifDateSetter,
                    mediaAnalyzer, fileNameUtils, duplicatePreProcessor, fileMover));
            case "set-gps" -> new SetGpsCommandRunner(console, new SetGpsCommand(console, fileExplorer, gpsDataSetter));
            case "rename" -> new RenameCommandRunner(console, new RenameCommand(fileExplorer, fileNameUtils, mediaAnalyzer,
                    duplicatePreProcessor, fileMover, console, exifDateSetter));
            default -> throw new IllegalArgumentException("Unknown command: " + commandName);
        };
    }
}
