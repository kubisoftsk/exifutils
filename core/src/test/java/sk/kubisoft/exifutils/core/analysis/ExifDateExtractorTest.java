package sk.kubisoft.exifutils.core.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.logging.Console;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExifDateExtractorTest {

    @Mock
    private Console consoleMock;

    private ExifDateExtractor exifDateExtractor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // we can use real ExifDateParser here, because it is just a simple wrapper around java.time classes
        var exifDateParser = new ExifDateParser();
        exifDateExtractor = new ExifDateExtractor(consoleMock, exifDateParser);
    }

    @Test
    void noDateFoundForImage1() {
        // This is likely image sent via WhatsApp, which does not contain any EXIF metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_1.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNull();
    }

    @Test
    void extractDateTimeForImage2() {
        // This is full size image taken with IPhone 14 in Greece (hence the correct timezone)
        Map<String, String> metaData = loadMetaData("/exifdata/image_2.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2023-08-31T18:11:44");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void extractDateTimeForImage3() {
        // This is full size HEIC image taken with IPhone 14 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_3.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2023-09-21T15:30:44");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractDateTimeForImage4() {
        // This is full size image taken with OnePlus Nord 2T in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_4.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2022-12-02T21:43:04");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractDateTimeForImage5() {
        // This is full size image taken with OnePlus 9 Pro in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_5.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2024-10-13T00:11:23");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractDateTimeForImage6() {
        // This is full size image taken with OnePlus 12 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_6.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2025-01-04T14:01:05");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractDateTimeForImage7() {
        // This is full size image taken with OnePlus 6 in Slovakia
        // there is no zone offset in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_7.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2020-01-03T07:19:08");
        assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForImage8() {
        // This is full size image taken with older OnePlus phone One E1003 in Slovakia
        // there is no zone offset in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_8.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2016-12-23T11:05:28");
        assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void noDateFoundForVideo1() {
        // This is likely video sent via WhatsApp, which does not contain any EXIF metadata
        Map<String, String> metaData = loadMetaData("/exifdata/video_1.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNull();
    }

    @Test
    void extractDateTimeForVideo2() {
        // This is video taken with IPhone 14 in Greece shortly before image_2
        Map<String, String> metaData = loadMetaData("/exifdata/video_2.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2023-08-31T18:10:31");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+03:00");

    }

    @Test
    void extractDateTimeForVideo3() {
        // This is video taken with IPhone 14 in Slovakia shortly after image_3
        Map<String, String> metaData = loadMetaData("/exifdata/video_3.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2023-09-21T15:33:11");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractDateTimeForVideo4() {
        // This is video taken with probably OnePlus Nord 2T at 18:43:21 local time in Slovakia
        // Offset is missing in metadata and
        Map<String, String> metaData = loadMetaData("/exifdata/video_4.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2023-04-19T18:43:21");
        assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForVideo5() {
        // This is video taken with OnePlus 9 Pro, but there is no offset in metadata
        // This video was actualy taken at 17:52:18 local time in Slovakia, but unfortunately the offset is missing in metadata
        // so the summer time has offset +02:00
        Map<String, String> metaData = loadMetaData("/exifdata/video_5.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2024-08-10T17:52:18");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractDateTimeForVideo6() {
        // This is video taken with OnePlus 12, but there is no offset in metadata
        // This video was actualy taken at 16:58:01 local time in Slovakia, but unfortunately the offset is missing in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/video_6.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2025-01-04T16:58:01");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractDateTimeForVideo7() {
        // This is video taken with OnePlus phone, but there is no offset in metadata
        // This video was actualy taken at 16:25:06 +0100 local time in Slovakia
        // then the offset is just guessed from current system timezone
        Map<String, String> metaData = loadMetaData("/exifdata/video_7.json");

        var exifDateTime = extract(metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime().toString()).isEqualTo("2022-02-21T16:25:06");
        assertThat(exifDateTime.zoneOffset().toString()).isEqualTo("+01:00");
    }

    private ExifDateTime extract(Map<String, String> metadata) {
        return exifDateExtractor.extractCreationDate(metadata)
                .orElse(null);
    }

    private Map<String, String> loadMetaData(String resourceName) {
        try (var is = getClass().getResourceAsStream(resourceName)) {
            return objectMapper.readValue(is, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error loading metadata from resource: " + resourceName, e);

        }
    }

}
