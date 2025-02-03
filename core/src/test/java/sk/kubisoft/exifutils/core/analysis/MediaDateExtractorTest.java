package sk.kubisoft.exifutils.core.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.config.model.DateTimeConfig;
import sk.kubisoft.exifutils.core.config.model.ExifUtilsConfiguration;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static sk.kubisoft.exifutils.core.media.MediaType.IMAGE;
import static sk.kubisoft.exifutils.core.media.MediaType.VIDEO;

@ExtendWith(MockitoExtension.class)
class MediaDateExtractorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ConfigService configServiceMock;

    @Mock
    private Console consoleMock;

    private MediaDateExtractor mediaDateExtractor;

    @BeforeEach
    void setUp() {
        mediaDateExtractor = new MediaDateExtractor(configServiceMock, consoleMock);
        lenient().when(configServiceMock.getConfig()).thenReturn(createConfig());
    }

    private ExifUtilsConfiguration createConfig() {
        var dateTimeConfig = new DateTimeConfig();
        dateTimeConfig.setTimeZone("Europe/Bratislava");
        var exifUtilsConfig = new ExifUtilsConfiguration();
        exifUtilsConfig.setDateTime(dateTimeConfig);
        return exifUtilsConfig;
    }

    @Test
    void noDateFoundForImage1() {
        // This is likely image sent via WhatsApp, which does not contain any EXIF metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_1.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isEmpty());
    }

    @Test
    void extractCreationDateForImage2() {
        // This is full size image taken with IPhone 14 in Greece (hence the correct timezone)
        Map<String, String> metaData = loadMetaData("/exifdata/image_2.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        var mediaDateTime = creationDate.get();
        assertEquals("2023-08-31T18:11:44", mediaDateTime.getLocalDateTime().toString());
        assertEquals("+03:00", mediaDateTime.getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForImage3() {
        // This is full size HEIC image taken with IPhone 14 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_3.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2023-09-21T15:30:44", creationDate.get().getLocalDateTime().toString());
        assertEquals("+02:00", creationDate.get().getZoneOffset().toString());

    }

    @Test
    void extractCreationDateForImage4() {
        // This is full size image taken with OnePlus Nord 2T in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_4.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2022-12-02T21:43:04", creationDate.get().getLocalDateTime().toString());
        assertEquals("+01:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForImage5() {
        // This is full size image taken with OnePlus 9 Pro in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_5.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2024-10-13T00:11:23", creationDate.get().getLocalDateTime().toString());
        assertEquals("+02:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForImage6() {
        // This is full size image taken with OnePlus 12 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_6.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2025-01-04T14:01:05", creationDate.get().getLocalDateTime().toString());
        assertEquals("+01:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForImage7() {
        // This is full size image taken with OnePlus 6 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_7.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2020-01-03T07:19:08", creationDate.get().getLocalDateTime().toString());
        assertEquals("+01:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForImage8() {
        // This is full size image taken with older OnePlus phone One E1003 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_8.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(IMAGE, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2016-12-23T11:05:28", creationDate.get().getLocalDateTime().toString());
        assertEquals("+01:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void noDateFoundForVideo1() {
        // This is likely video sent via WhatsApp, which does not contain any EXIF metadata
        Map<String, String> metaData = loadMetaData("/exifdata/video_1.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isEmpty());
    }

    @Test
    void extractCreationDateForVideo2() {
        // This is video taken with IPhone 14 in Greece shortly before image_2
        Map<String, String> metaData = loadMetaData("/exifdata/video_2.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2023-08-31T18:10:31", creationDate.get().getLocalDateTime().toString());
        assertEquals("+03:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForVideo3() {
        // This is video taken with IPhone 14 in Slovakia shortly after image_3
        Map<String, String> metaData = loadMetaData("/exifdata/video_3.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2023-09-21T15:33:11", creationDate.get().getLocalDateTime().toString());
        assertEquals("+02:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForVideo4() {
        // This is video taken with probably OnePlus Nord 2T, but there is no offset in metadata
        // This video was actualy taken at 18:43:21 local time in Slovakia, but unfortunately the offset is missing in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/video_4.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2023-04-19T18:43:21", creationDate.get().getLocalDateTime().toString());
        assertEquals("+02:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForVideo5() {
        // This is video taken with OnePlus 9 Pro, but there is no offset in metadata
        // This video was actualy taken at 17:52:18 local time in Slovakia, but unfortunately the offset is missing in metadata
		// so the summer time has offset +02:00
        Map<String, String> metaData = loadMetaData("/exifdata/video_5.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2024-08-10T17:52:18", creationDate.get().getLocalDateTime().toString());
        assertEquals("+02:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForVideo6() {
        // This is video taken with OnePlus 12, but there is no offset in metadata
        // This video was actualy taken at 16:58:01 local time in Slovakia, but unfortunately the offset is missing in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/video_6.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2025-01-04T16:58:01", creationDate.get().getLocalDateTime().toString());
        assertEquals("+01:00", creationDate.get().getZoneOffset().toString());
    }

    @Test
    void extractCreationDateForVideo7() {
        // This is video taken with OnePlus phone, but there is no offset in metadata
        // This video was actualy taken at 16:25:06 +0100 local time in Slovakia
        // then the offset is just guessed from current system timezone
        Map<String, String> metaData = loadMetaData("/exifdata/video_7.json");

        Optional<MediaDateTime> creationDate = mediaDateExtractor.extractCreationDate(mediaFile(VIDEO, metaData));

        assertTrue(creationDate.isPresent());
        assertEquals("2022-02-21T16:25:06", creationDate.get().getLocalDateTime().toString());
        assertEquals("+01:00", creationDate.get().getZoneOffset().toString());
    }

    private Map<String, String> loadMetaData(String resourceName) {
        try (var is = getClass().getResourceAsStream(resourceName)) {
            return objectMapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error loading metadata from resource: " + resourceName, e);

        }
    }

    private MediaFile mediaFile(MediaType mediaType, Map<String, String> metaData) {
        return new MediaFile(null, mediaType, metaData, null);
    }
}