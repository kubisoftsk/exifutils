package sk.kubisoft.exifutils.core.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.config.model.DateTimeConfig;
import sk.kubisoft.exifutils.core.config.model.ExifUtilsConfiguration;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.logging.JUnitConsole;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static sk.kubisoft.exifutils.core.media.MediaType.IMAGE;
import static sk.kubisoft.exifutils.core.media.MediaType.VIDEO;

@ExtendWith(MockitoExtension.class)
class MediaAnalyzerTest {

	private final Console console = new JUnitConsole();

    @Mock
    private ConfigService configServiceMock;

    @Mock
    private MediaTypeDetector mediaTypeDetectorMock;

	@Mock
	private MetaDataExtractorFactory metaDataExtractorFactoryMock;

	@Mock
    private ExifDateExtractor exifDateExtractorMock;

    @Mock
    private GpsZoneExtractor gpsZoneExtractorMock;

    private MediaAnalyzer mediaAnalyzer;

    @BeforeEach
    void setUp() {
        mediaAnalyzer = new MediaAnalyzer(console, configServiceMock, mediaTypeDetectorMock,
										  metaDataExtractorFactoryMock, exifDateExtractorMock, gpsZoneExtractorMock);
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
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNull();
    }

    @Test
    void extractCreationDateForImage2() {
        // This is full size image taken with IPhone 14 in Greece (hence the correct timezone)
		LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 31, 18, 11, 44);
		ZoneOffset zoneOffset = ZoneOffset.ofHours(3);

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-08-31T18:11:44");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void extractCreationDateForImage3() {
        // This is full size HEIC image taken with IPhone 14 in Slovakia
//        Map<String, String> metaData = loadMetaData("/exifdata/image_3.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-09-21T15:30:44");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractCreationDateForImage4() {
        // This is full size image taken with OnePlus Nord 2T in Slovakia
//        Map<String, String> metaData = loadMetaData("/exifdata/image_4.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2022-12-02T21:43:04");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractCreationDateForImage5() {
        // This is full size image taken with OnePlus 9 Pro in Slovakia
//        Map<String, String> metaData = loadMetaData("/exifdata/image_5.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2024-10-13T00:11:23");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractCreationDateForImage6() {
        // This is full size image taken with OnePlus 12 in Slovakia
//        Map<String, String> metaData = loadMetaData("/exifdata/image_6.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2025-01-04T14:01:05");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractCreationDateForImage7() {
        // This is full size image taken with OnePlus 6 in Slovakia
//        Map<String, String> metaData = loadMetaData("/exifdata/image_7.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2020-01-03T07:19:08");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractCreationDateForImage8() {
        // This is full size image taken with older OnePlus phone One E1003 in Slovakia
//        Map<String, String> metaData = loadMetaData("/exifdata/image_8.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2016-12-23T11:05:28");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void noDateFoundForVideo1() {
        // This is likely video sent via WhatsApp, which does not contain any EXIF metadata
//        Map<String, String> metaData = loadMetaData("/exifdata/video_1.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNull();
    }

    @Test
    void extractCreationDateForVideo2() {
        // This is video taken with IPhone 14 in Greece shortly before image_2
//        Map<String, String> metaData = loadMetaData("/exifdata/video_2.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-08-31T18:10:31");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+03:00");

    }

    @Test
    void extractCreationDateForVideo3() {
        // This is video taken with IPhone 14 in Slovakia shortly after image_3
//        Map<String, String> metaData = loadMetaData("/exifdata/video_3.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-09-21T15:33:11");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractCreationDateForVideo4() {
        // This is video taken with probably OnePlus Nord 2T, but there is no offset in metadata
        // This video was actualy taken at 18:43:21 local time in Slovakia, but unfortunately the offset is missing in metadata
//        Map<String, String> metaData = loadMetaData("/exifdata/video_4.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-04-19T18:43:21");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractCreationDateForVideo5() {
        // This is video taken with OnePlus 9 Pro, but there is no offset in metadata
        // This video was actualy taken at 17:52:18 local time in Slovakia, but unfortunately the offset is missing in metadata
		// so the summer time has offset +02:00
//        Map<String, String> metaData = loadMetaData("/exifdata/video_5.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2024-08-10T17:52:18");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractCreationDateForVideo6() {
        // This is video taken with OnePlus 12, but there is no offset in metadata
        // This video was actualy taken at 16:58:01 local time in Slovakia, but unfortunately the offset is missing in metadata
//        Map<String, String> metaData = loadMetaData("/exifdata/video_6.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2025-01-04T16:58:01");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractCreationDateForVideo7() {
        // This is video taken with OnePlus phone, but there is no offset in metadata
        // This video was actualy taken at 16:25:06 +0100 local time in Slovakia
        // then the offset is just guessed from current system timezone
//        Map<String, String> metaData = loadMetaData("/exifdata/video_7.json");
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2022-02-21T16:25:06");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    private MediaDateTime analyze(MediaType mediaType, LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        when(mediaTypeDetectorMock.detectMediaType(any()))
				.thenReturn(mediaType);
		if (localDateTime != null && zoneOffset != null) {
			when(exifDateExtractorMock.extractCreationDate(any()))
					.thenReturn(Optional.of(new ExifDateTime(localDateTime, zoneOffset)));
		}

		Path testFilePath = Paths.get("dummy");

		List<MediaFile> mediaFiles = mediaAnalyzer.analyze(List.of(testFilePath));

        if (mediaFiles.isEmpty()) {
            return null;
        }

        return mediaFiles.getFirst().creationDate();
    }

}