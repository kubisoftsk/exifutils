package sk.kubisoft.exifutils.core.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import java.util.List;
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
    void extractCreationDateForImageWithLocalTimeAndZoneOffset() {
        // This is full size image taken with IPhone 14 in Greece (hence the correct timezone)
		LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 31, 18, 11, 44);
		ZoneOffset zoneOffset = ZoneOffset.ofHours(3);

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-08-31T18:11:44");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void extractCreationDateForImageWithoutOffsetAndGpsData() {
		LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 3, 7, 19, 8);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2020-01-03T07:19:08");
        // The offset is just assumed from config file or system default timezone
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void extractCreationDateForImageWithoutOffsetAndGpsData2() {
        // This is full size image taken with older OnePlus phone One E1003 in Slovakia
		LocalDateTime localDateTime = LocalDateTime.of(2016, 12, 23, 11, 5, 28);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2016-12-23T11:05:28");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

    @Test
    void noDateFoundForVideo1() {
        // This is likely video sent via WhatsApp, which does not contain any EXIF metadata
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNull();
    }

    @Test
    void extractCreationDateForIphoneVideoThatStoresOffset() {
        // This is video taken with IPhone 14 in Greece shortly before image_2
		LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 31, 18, 10, 31);
		ZoneOffset zoneOffset = ZoneOffset.ofHours(3);

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-08-31T18:10:31");
        // Iphone videos actually does contain the offset in metadata tag CreationDate
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+03:00");
    }

    @Test
    void extractCreationDateForVideoCreateDateStoredInUTCWithoutOffsetSummer() {
        // This is video taken with probably OnePlus Nord 2T, but there is no offset in metadata
        // This video was actualy taken at 18:43:21 local time in Slovakia, but unfortunately the offset is missing in metadata
        // Notice this mocked time is in UTC time and in that time of year there is offset +02:00
		LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 19, 16, 43, 21);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2023-04-19T18:43:21");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+02:00");
    }

    @Test
    void extractCreationDateForVideoCreateDateStoredInUTCWithoutOffsetWinter() {
        // This is video taken with OnePlus 12, but there is no offset in metadata
        // This video was actualy taken at 16:58:01 local time in Slovakia, but unfortunately the offset is missing in metadata
        // Notice this mocked time is in UTC time and in that time of year there is offset +01:00
		LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 4, 15, 58, 1);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime().toString()).isEqualTo("2025-01-04T16:58:01");
        assertThat(creationDate.getZoneOffset().toString()).isEqualTo("+01:00");
    }

// TODO tests for videos with GPS data and images with GPS data but missing offset

    private MediaDateTime analyze(MediaType mediaType, LocalDateTime localDateTime, ZoneOffset zoneOffset) {
        when(mediaTypeDetectorMock.detectMediaType(any()))
				.thenReturn(mediaType);
		if (localDateTime != null) {
			when(exifDateExtractorMock.extractCreationDate(any()))
					.thenReturn(Optional.of(new ExifDateTime(localDateTime, zoneOffset)));
		}
        when(metaDataExtractorFactoryMock.newMetaDataExtractor())
                .thenReturn(Mockito.mock(MetaDataExtractor.class));

		Path testFilePath = Paths.get("dummy");

		List<MediaFile> mediaFiles = mediaAnalyzer.analyze(List.of(testFilePath));

        if (mediaFiles.isEmpty()) {
            return null;
        }

        return mediaFiles.getFirst().creationDate();
    }

}