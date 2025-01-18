package sk.kubisoft.exifutils.core.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.config.model.ExifUtilsConfiguration;
import sk.kubisoft.exifutils.core.config.model.RenameConfig;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaFileNameUtilsTest {

    @Mock
    private ConfigService configService;

    private MediaFileNameUtils renamer;
    private static final String PATTERN = "IMG_${date,yyyyMMdd}_${date,HHmmss}";

    @BeforeEach
    void setUp() {
        var config = new ExifUtilsConfiguration();
        var renameConfig = new RenameConfig();
        renameConfig.setPattern(PATTERN);
        config.setRename(renameConfig);
        when(configService.getConfig()).thenReturn(config);
        renamer = new MediaFileNameUtils(configService);
    }

    @Test
    void shouldCreateNewNameIPhoneImage() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 18, 15, 30, 45);
        MediaDateTime mediaDateTime = new MediaDateTime(dateTime);
        MediaFile mediaFile = new MediaFile(
                Path.of("IMG_7456.JPG"),
                MediaType.IMAGE,
                Collections.emptyMap()
        );

        // when
        String result = renamer.createNewName(mediaFile, mediaDateTime);

        // then
        assertEquals("IMG_20240118_153045.jpg", result);
    }

    @Test
    void shouldCreateNewNameIPhoneVideo() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 18, 15, 30, 45);
        MediaDateTime mediaDateTime = new MediaDateTime(dateTime);
        MediaFile mediaFile = new MediaFile(
                Path.of("IMG_7456.MOV"),
                MediaType.VIDEO,
                Collections.emptyMap()
        );

        // when
        String result = renamer.createNewName(mediaFile, mediaDateTime);

        // then
        assertEquals("IMG_20240118_153045.mov", result);
    }

    @Test
    void shouldCreateNewNameAndroidImage() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 18, 15, 30, 45);
        MediaDateTime mediaDateTime = new MediaDateTime(dateTime);
        MediaFile mediaFile = new MediaFile(
                Path.of("IMG20240118153045.JPG"),
                MediaType.IMAGE,
                Collections.emptyMap()
        );

        // when
        String result = renamer.createNewName(mediaFile, mediaDateTime);

        // then
        assertEquals("IMG_20240118_153045.jpg", result);
    }

    @Test
    void shouldHandleHeicFormat() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 18, 15, 30, 45);
        MediaDateTime mediaDateTime = new MediaDateTime(dateTime);
        MediaFile mediaFile = new MediaFile(
                Path.of("IMG_7456.HEIC"),
                MediaType.IMAGE,
                Collections.emptyMap()
        );

        // when
        String result = renamer.createNewName(mediaFile, mediaDateTime);

        // then
        assertEquals("IMG_20240118_153045.heic", result);
    }
}