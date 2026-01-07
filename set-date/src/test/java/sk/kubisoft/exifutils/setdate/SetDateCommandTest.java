package sk.kubisoft.exifutils.setdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.analysis.MediaAnalyzer;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.file.FileNameAnalyzer;
import sk.kubisoft.exifutils.core.file.FileMover;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.file.conflict.DuplicatePreProcessor;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.AnalyzedMediaFile;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaFileNameUtils;
import sk.kubisoft.exifutils.core.media.MediaType;
import sk.kubisoft.exifutils.core.metadata.ExifDateSetter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetDateCommandTest {

    @Mock
    private Console console;

    @Mock
    private ConfigService configService;

    @Mock
    private FileExplorer fileExplorer;

    @Mock
    private FileNameAnalyzer fileNameAnalyzer;

    @Mock
    private MediaAnalyzer mediaAnalyzer;

    @Mock
    private ExifDateSetter exifDateSetter;

    @Mock
    private MediaFileNameUtils fileNameUtils;

    @Mock
    private DuplicatePreProcessor duplicatePreProcessor;

    @Mock
    private FileMover fileMover;

    @Captor
    private ArgumentCaptor<List<SetDateAction>> actionsCaptor;

    private SetDateCommand command;

    @BeforeEach
    void setUp() {
        command = new SetDateCommand(console, configService, fileNameAnalyzer, mediaAnalyzer,
                fileExplorer, exifDateSetter, fileNameUtils, duplicatePreProcessor, fileMover);

        lenient().when(configService.getTimeZone()).thenReturn(ZoneId.of("Europe/Bratislava"));
        lenient().when(console.confirmAction(any())).thenReturn(true);
    }

    @Test
    void fixZoneShouldUseExistingLocalDateTimeWithNewOffset() {
        // Given: a file with existing EXIF date at 10:00 with wrong offset (+00:00)
        Path testPath = Paths.get("/test/photo.jpg");
        LocalDateTime existingLocalDateTime = LocalDateTime.of(2023, 8, 15, 10, 0, 0);
        ZoneOffset wrongOffset = ZoneOffset.UTC;
        MediaDateTime existingDate = new MediaDateTime(existingLocalDateTime, wrongOffset);

        MediaFile mediaFile = new MediaFile(testPath, MediaType.IMAGE);
        AnalyzedMediaFile analyzedFile = new AnalyzedMediaFile(testPath, MediaType.IMAGE,
                Collections.emptyMap(), existingDate);

        when(fileExplorer.listMediaFiles(any())).thenReturn(List.of(mediaFile));
        when(mediaAnalyzer.analyze(anyList())).thenReturn(List.of(analyzedFile));

        // When: fix-zone is called with Europe/Athens timezone (+03:00 in summer)
        ZoneId correctZone = ZoneId.of("Europe/Athens");
        SetDateCommandInput input = new SetDateCommandInput(
                new String[]{"/test"},
                null,  // no pattern
                null,  // no manual dateTime
                correctZone,
                false, // no rename
                false, // not unknownOnly
                true   // fixZone = true
        );

        command.execute(input);

        // Then: the date setter should be called with the same local time but new offset
        verify(exifDateSetter).setDateTime(actionsCaptor.capture());
        List<SetDateAction> actions = actionsCaptor.getValue();

        assertThat(actions).hasSize(1);
        SetDateAction action = actions.getFirst();

        // Local time should remain unchanged
        assertThat(action.dateTime().getLocalDateTime()).isEqualTo(existingLocalDateTime);
        // Offset should be +03:00 (Athens summer time)
        assertThat(action.dateTime().getZoneOffset()).isEqualTo(ZoneOffset.ofHours(3));
    }

    @Test
    void fixZoneShouldUseDefaultTimezoneWhenNotSpecified() {
        // Given: a file with existing EXIF date
        Path testPath = Paths.get("/test/photo.jpg");
        LocalDateTime existingLocalDateTime = LocalDateTime.of(2023, 1, 15, 10, 0, 0);
        ZoneOffset wrongOffset = ZoneOffset.UTC;
        MediaDateTime existingDate = new MediaDateTime(existingLocalDateTime, wrongOffset);

        MediaFile mediaFile = new MediaFile(testPath, MediaType.IMAGE);
        AnalyzedMediaFile analyzedFile = new AnalyzedMediaFile(testPath, MediaType.IMAGE,
                Collections.emptyMap(), existingDate);

        when(fileExplorer.listMediaFiles(any())).thenReturn(List.of(mediaFile));
        when(mediaAnalyzer.analyze(anyList())).thenReturn(List.of(analyzedFile));

        // When: fix-zone is called without specifying zoneId (should use default from config)
        SetDateCommandInput input = new SetDateCommandInput(
                new String[]{"/test"},
                null,  // no pattern
                null,  // no manual dateTime
                null,  // no zoneId - should use default
                false, // no rename
                false, // not unknownOnly
                true   // fixZone = true
        );

        command.execute(input);

        // Then: should use config timezone (Europe/Bratislava, +01:00 in winter)
        verify(exifDateSetter).setDateTime(actionsCaptor.capture());
        List<SetDateAction> actions = actionsCaptor.getValue();

        assertThat(actions).hasSize(1);
        assertThat(actions.getFirst().dateTime().getZoneOffset()).isEqualTo(ZoneOffset.ofHours(1));
    }

    @Test
    void fixZoneShouldSkipFilesWithoutExifDate() {
        // Given: a file without EXIF date
        Path testPath = Paths.get("/test/photo.jpg");

        MediaFile mediaFile = new MediaFile(testPath, MediaType.IMAGE);
        AnalyzedMediaFile analyzedFile = new AnalyzedMediaFile(testPath, MediaType.IMAGE,
                Collections.emptyMap(), null); // no creation date

        when(fileExplorer.listMediaFiles(any())).thenReturn(List.of(mediaFile));
        when(mediaAnalyzer.analyze(anyList())).thenReturn(List.of(analyzedFile));

        // When: fix-zone is called
        SetDateCommandInput input = new SetDateCommandInput(
                new String[]{"/test"},
                null, null, ZoneId.of("Europe/Paris"),
                false, false, true
        );

        command.execute(input);

        // Then: no actions should be performed
        verify(exifDateSetter).setDateTime(actionsCaptor.capture());
        assertThat(actionsCaptor.getValue()).isEmpty();
    }
}
