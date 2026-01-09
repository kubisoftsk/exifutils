package sk.kubisoft.exifutils.core.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.Yaml;
import sk.kubisoft.exifutils.core.analysis.device.DeviceProfileService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.logging.JUnitConsole;
import sk.kubisoft.exifutils.core.media.MediaType;
import sk.kubisoft.exifutils.core.testutil.MetadataParser;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static sk.kubisoft.exifutils.core.media.MediaType.IMAGE;
import static sk.kubisoft.exifutils.core.media.MediaType.VIDEO;

@ExtendWith(MockitoExtension.class)
class ExifDateExtractorTest {

    private final Console console = new JUnitConsole();

    private ExifDateExtractor exifDateExtractor;

    @BeforeEach
    void setUp() {
        // we can use real ExifDateParser here, because it is just a simple wrapper around java.time classes
        var exifDateParser = new ExifDateParser();
		var deviceProfileService = new DeviceProfileService(new Yaml());
        exifDateExtractor = new ExifDateExtractor(console, exifDateParser, deviceProfileService);
    }

    @Test
    void noDateFoundForImage1() {
        // This is likely image sent via WhatsApp, which does not contain any EXIF metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_1.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNull();
    }

    @Test
    void extractDateTimeForImage2() {
        // This is full size image taken with IPhone 14 in Greece (hence the correct timezone)
        Map<String, String> metaData = loadMetaData("/exifdata/image_2.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2023-08-31T18:11:44");
		assertThat(exifDateTime.localTime()).isTrue();
        assertThat(exifDateTime.zoneOffset()).hasToString("+03:00");
    }

    @Test
    void extractDateTimeForImage3() {
        // This is full size HEIC image taken with IPhone 14 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_3.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2023-09-21T15:30:44");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).hasToString("+02:00");
    }

    @Test
    void extractDateTimeForImage4() {
        // This is full size image taken with OnePlus Nord 2T in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_4.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2022-12-02T21:43:04");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).hasToString("+01:00");
    }

    @Test
    void extractDateTimeForImage5() {
        // This is full size image taken with OnePlus 9 Pro in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_5.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2024-10-13T00:11:23");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).hasToString("+02:00");
    }

    @Test
    void extractDateTimeForImage6() {
        // This is full size image taken with OnePlus 12 in Slovakia
        Map<String, String> metaData = loadMetaData("/exifdata/image_6.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2025-01-04T14:01:05");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).hasToString("+01:00");
    }

    @Test
    void extractDateTimeForImage7() {
        // This is full size image taken with OnePlus 6 in Slovakia
        // there is no zone offset in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_7.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2020-01-03T07:19:08");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForImage8() {
        // This is full size image taken with older OnePlus phone One E1003 in Slovakia
        // there is no zone offset in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_8.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2016-12-23T11:05:28");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForImage9() {
        // This is full size image taken with Nikon D3100 camera in Slovakia at local time 12:43:35
        // there is no zone offset in metadata
        Map<String, String> metaData = loadMetaData("/exifdata/image_9.txt");

        var exifDateTime = extract(IMAGE, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2013-01-03T09:44:05");
        assertThat(exifDateTime.localTime()).isTrue();
        assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void noDateFoundForVideo1() {
        // This is likely video sent via WhatsApp, which does not contain any EXIF metadata
        Map<String, String> metaData = loadMetaData("/exifdata/video_1.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNull();
    }

    @Test
    void extractDateTimeForVideo2() {
        // This is video taken with IPhone 14 in Greece shortly before image_2
        Map<String, String> metaData = loadMetaData("/exifdata/video_2.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2023-08-31T18:10:31");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).hasToString("+03:00");

    }

    @Test
    void extractDateTimeForVideo3() {
        // This is video taken with IPhone 14 in Slovakia shortly after image_3
        Map<String, String> metaData = loadMetaData("/exifdata/video_3.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2023-09-21T15:33:11");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).hasToString("+02:00");
    }

    @Test
    void extractDateTimeForVideo4() {
        // This is video taken with probably OnePlus Nord 2T at 18:43:21 local time in Slovakia (+0200)
        // Offset is missing in metadata and video dates are stored in UTC by convention
        Map<String, String> metaData = loadMetaData("/exifdata/video_4.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2023-04-19T16:43:21");
		assertThat(exifDateTime.localTime()).isFalse();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForVideo5() {
        // This is video taken with OnePlus 9 Pro at 17:52:18 local time in Slovakia (+0200)
		// Offset is missing in metadata and video dates are stored in UTC by convention
        Map<String, String> metaData = loadMetaData("/exifdata/video_5.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2024-08-10T15:52:18");
		assertThat(exifDateTime.localTime()).isFalse();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForVideo6() {
        // This is video taken with OnePlus 12 at 16:58:01 local time in Slovakia (+0100)
		// Offset is missing in metadata and video dates are stored in UTC by convention
        Map<String, String> metaData = loadMetaData("/exifdata/video_6.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2025-01-04T15:58:01");
		assertThat(exifDateTime.localTime()).isFalse();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForVideo7() {
        // This is video taken with OnePlus phone at 16:25:06 local time in Slovakia (+0100)
		// Offset is missing in metadata and video dates are stored in UTC by convention
        Map<String, String> metaData = loadMetaData("/exifdata/video_7.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2022-02-21T15:25:06");
		assertThat(exifDateTime.localTime()).isFalse();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeForVideo8() {
        // This is video taken with Nikon D3100 DSLR camera at 12:43:35 local time in Slovakia (+0100)
        // Offset is missing in metadata
        // This is a special case, because the video date is stored in local time, not in UTC as is common for videos
        Map<String, String> metaData = loadMetaData("/exifdata/video_9.txt");

        var exifDateTime = extract(VIDEO, metaData);

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2013-01-11T12:43:35");
		assertThat(exifDateTime.localTime()).isTrue();
		assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeUsingForceField() {
        // Using forceField to extract from a specific field (ModifyDate instead of DateTimeOriginal)
        Map<String, String> metaData = loadMetaData("/exifdata/image_2.txt");

        var exifDateTime = extractWithForceField(IMAGE, metaData, "ModifyDate");

        assertThat(exifDateTime).isNotNull();
        assertThat(exifDateTime.localDateTime()).hasToString("2023-08-31T18:11:44");
        assertThat(exifDateTime.localTime()).isTrue();
        // forceField parses without offset field, so offset should be null
        assertThat(exifDateTime.zoneOffset()).isNull();
    }

    @Test
    void extractDateTimeUsingForceFieldNonExistent() {
        // Using forceField with a field that does not exist
        Map<String, String> metaData = loadMetaData("/exifdata/image_2.txt");

        var exifDateTime = extractWithForceField(IMAGE, metaData, "NonExistentField");

        assertThat(exifDateTime).isNull();
    }

    @Test
    void extractDateTimeUsingForceFieldFromFileModifyDate() {
        // Using forceField to extract from FileModifyDate
        // This tests the use case mentioned in the issue - using filesystem dates
        Map<String, String> metaData = loadMetaData("/exifdata/image_2.txt");

        var exifDateTime = extractWithForceField(IMAGE, metaData, "FileModifyDate");

        assertThat(exifDateTime).isNotNull();
        // FileModifyDate: 2025:01:04 15:55:52+01:00 - parsed as local time
        assertThat(exifDateTime.localDateTime()).hasToString("2025-01-04T15:55:52");
        assertThat(exifDateTime.localTime()).isTrue();
        // Offset is embedded in the date string and should be parsed
        assertThat(exifDateTime.zoneOffset()).hasToString("+01:00");
    }

    @Test
    void extractDateTimeUsingForceFieldForImageWithNoDate() {
        // Using forceField on an image that normally has no date
        Map<String, String> metaData = loadMetaData("/exifdata/image_1.txt");

        // Normal extraction should return null
        var normalExifDateTime = extract(IMAGE, metaData);
        assertThat(normalExifDateTime).isNull();

        // forceField should also return null if the field doesn't exist
        var forcedExifDateTime = extractWithForceField(IMAGE, metaData, "DateTimeOriginal");
        assertThat(forcedExifDateTime).isNull();
    }

    private ExifDateTime extract(MediaType mediaType, Map<String, String> metadata) {
        return exifDateExtractor.extractCreationDate(mediaType, metadata)
                .orElse(null);
    }

    private ExifDateTime extractWithForceField(MediaType mediaType, Map<String, String> metadata, String forceField) {
        return exifDateExtractor.extractCreationDate(mediaType, metadata, forceField)
                .orElse(null);
    }

    private Map<String, String> loadMetaData(String resourceName) {
        try (var is = getClass().getResourceAsStream(resourceName)) {
            return MetadataParser.parseMetadata(is);
        } catch (Exception e) {
            throw new RuntimeException("Error loading metadata from resource: " + resourceName, e);
        }
    }

}
