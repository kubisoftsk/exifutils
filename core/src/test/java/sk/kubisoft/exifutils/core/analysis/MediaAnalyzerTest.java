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
import sk.kubisoft.exifutils.core.metadata.MetaDataHandler;
import sk.kubisoft.exifutils.core.metadata.MetaDataHandlerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
	private MetaDataHandlerFactory metaDataHandlerFactoryMock;

	@Mock
    private ExifDateExtractor exifDateExtractorMock;

    @Mock
    private GpsZoneExtractor gpsZoneExtractorMock;

    private MediaAnalyzer mediaAnalyzer;

    @BeforeEach
    void setUp() {
        mediaAnalyzer = new MediaAnalyzer(console, configServiceMock, mediaTypeDetectorMock,
                metaDataHandlerFactoryMock, exifDateExtractorMock, gpsZoneExtractorMock);
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

        var creationDate = analyze(IMAGE, localDateTime, true, zoneOffset);

        assertThat(creationDate).isNull();
    }

    @Test
    void extractCreationDateForImageWithLocalTimeAndZoneOffset() {
        // This is full size image taken with IPhone 14 in Greece (hence the correct timezone)
		LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 31, 18, 11, 44);
		ZoneOffset zoneOffset = ZoneOffset.ofHours(3);

        var creationDate = analyze(IMAGE, localDateTime, true, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime()).hasToString("2023-08-31T18:11:44");
        assertThat(creationDate.getZoneOffset()).hasToString("+03:00");
    }

    @Test
    void extractCreationDateForImageWithoutOffsetAndGpsData() {
		LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 3, 7, 19, 8);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(IMAGE, localDateTime, true, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime()).hasToString("2020-01-03T07:19:08");
        // The offset is just assumed from config file or system default timezone
        assertThat(creationDate.getZoneOffset()).hasToString("+01:00");
    }

    @Test
    void extractCreationDateForImageWithoutOffsetButWithGpsDataPresent() {
        // This image was taken with Samsung SM-M205FN phone in Slovakia, but the offset is missing in metadata
		LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 2, 17, 14, 56);
		ZoneOffset zoneOffset = null;
		ZoneOffset gpsResolvedZoneOffset = ZoneOffset.ofHours(1);

        var creationDate = analyze(IMAGE, localDateTime, true, zoneOffset, gpsResolvedZoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime()).hasToString("2023-01-02T17:14:56");
        assertThat(creationDate.getZoneOffset()).hasToString("+01:00");
    }

    @Test
    void noDateFoundForVideo1() {
        // This is likely video sent via WhatsApp, which does not contain any EXIF metadata
		LocalDateTime localDateTime = null;
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, true, zoneOffset);

        assertThat(creationDate).isNull();
    }

    @Test
    void extractCreationDateForIphoneVideoThatStoresOffset() {
        // This is video taken with IPhone 14 in Greece shortly before image_2
		LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 31, 18, 10, 31);
		ZoneOffset zoneOffset = ZoneOffset.ofHours(3);

        var creationDate = analyze(VIDEO, localDateTime, true, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime()).hasToString("2023-08-31T18:10:31");
        // Iphone videos actually does contain the offset in metadata tag CreationDate
        assertThat(creationDate.getZoneOffset()).hasToString("+03:00");
    }

	@Test
	void extractCreationDateForVideoCreateDateStoredInUTCWithoutOffsetButWithGPSData() {
		// This is video taken with OnePlus 12 in Greece at 11:32:51 local time (+0300), but there is no offset in metadata
		// Fortunately there is GPS data in metadata, so we can extract the offset from that
		LocalDateTime localDateTime = LocalDateTime.of(2023, 8, 30, 8, 32, 51);
		ZoneOffset zoneOffset = null;
		ZoneOffset gpsResolvedZoneOffset = ZoneOffset.ofHours(3);

		var creationDate = analyze(VIDEO, localDateTime, false, zoneOffset, gpsResolvedZoneOffset);

		assertThat(creationDate).isNotNull();
		assertThat(creationDate.getLocalDateTime()).hasToString("2023-08-30T11:32:51");
		assertThat(creationDate.getZoneOffset()).hasToString("+03:00");
	}

    @Test
    void extractCreationDateForVideoCreateDateStoredInUTCWithoutOffsetSummer() {
        // This is video taken with probably OnePlus Nord 2T, but there is no offset in metadata
        // This video was actualy taken at 18:43:21 local time in Slovakia, but unfortunately the offset is missing in metadata
        // Notice this mocked time is in UTC time and in that time of year there is offset +02:00
		LocalDateTime localDateTime = LocalDateTime.of(2023, 4, 19, 16, 43, 21);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, false, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime()).hasToString("2023-04-19T18:43:21");
        assertThat(creationDate.getZoneOffset()).hasToString("+02:00");
    }

    @Test
    void extractCreationDateForVideoCreateDateStoredInUTCWithoutOffsetWinter() {
        // This is video taken with OnePlus 12, but there is no offset in metadata
        // This video was actualy taken at 16:58:01 local time in Slovakia, but unfortunately the offset is missing in metadata
        // Notice this mocked time is in UTC time and in that time of year there is offset +01:00
		LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 4, 15, 58, 1);
		ZoneOffset zoneOffset = null;

        var creationDate = analyze(VIDEO, localDateTime, false, zoneOffset);

        assertThat(creationDate).isNotNull();
        assertThat(creationDate.getLocalDateTime()).hasToString("2025-01-04T16:58:01");
        assertThat(creationDate.getZoneOffset()).hasToString("+01:00");
    }

	private MediaDateTime analyze(MediaType mediaType, LocalDateTime localDateTime, boolean localTime, ZoneOffset zoneOffset) {
		return analyze(mediaType, localDateTime, localTime, zoneOffset, null);
	}

    private MediaDateTime analyze(MediaType mediaType, LocalDateTime localDateTime, boolean localTime,
								  ZoneOffset zoneOffset, ZoneOffset gpsResolvedZoneOffset) {
        when(mediaTypeDetectorMock.detectMediaType(any()))
				.thenReturn(mediaType);
		if (localDateTime != null) {
			when(exifDateExtractorMock.extractCreationDate(any(), any()))
					.thenReturn(Optional.of(new ExifDateTime(localDateTime, localTime, zoneOffset)));
		}
        when(metaDataHandlerFactoryMock.create())
                .thenReturn(Mockito.mock(MetaDataHandler.class));
		if (gpsResolvedZoneOffset != null) {
			when(gpsZoneExtractorMock.extractGpsZone(any(Path.class), anyMap()))
					.thenReturn(Optional.of(gpsResolvedZoneOffset));
		}
		Path testFilePath = Paths.get("dummy");

		List<MediaFile> mediaFiles = mediaAnalyzer.analyze(List.of(testFilePath));

        if (mediaFiles.isEmpty()) {
            return null;
        }

        return mediaFiles.getFirst().creationDate();
    }

}