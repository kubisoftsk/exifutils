package sk.kubisoft.exifutils.core.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.file.SetDateAction;
import sk.kubisoft.exifutils.core.logging.JUnitConsole;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExifDateSetterTest {

    @Mock
    private MetaDataHandlerFactory metaDataHandlerFactoryMock;

    @Mock
    private MetaDataHandler metaDataHandlerMock;

    private ExifDateSetter exifDateSetter;

    @BeforeEach
    void setUp() {
        when(metaDataHandlerFactoryMock.create()).thenReturn(metaDataHandlerMock);
        exifDateSetter = new ExifDateSetter(new JUnitConsole(), metaDataHandlerFactoryMock);
    }

    @Test
    void writesGroupQualifiedVideoTags() {
        Path file = Path.of("video.mp4");
        var dateTime = new MediaDateTime(LocalDateTime.of(2026, 10, 20, 10, 1, 0), ZoneOffset.ofHours(2));

        exifDateSetter.setDateTime(List.of(new SetDateAction(file, MediaType.VIDEO, dateTime)));

        Map<String, String> tags = captureTags(file);
        assertEquals(Map.of(
                "QuickTime:CreateDate", "2026:10:20 08:01:00",
                "Keys:CreationDate", "2026:10:20 10:01:00+02:00",
                "XMP-xmp:CreateDate", "",
                "XMP-pdf:CreationDate", ""
        ), tags);
    }

    @Test
    void writesGroupQualifiedImageTags() {
        Path file = Path.of("image.jpg");
        var dateTime = new MediaDateTime(LocalDateTime.of(2026, 1, 15, 14, 30, 5), ZoneOffset.ofHours(1));

        exifDateSetter.setDateTime(List.of(new SetDateAction(file, MediaType.IMAGE, dateTime)));

        Map<String, String> tags = captureTags(file);
        assertEquals(Map.of(
                "IFD0:ModifyDate", "2026:01:15 14:30:05",
                "ExifIFD:DateTimeOriginal", "2026:01:15 14:30:05",
                "ExifIFD:OffsetTime", "+01:00",
                "ExifIFD:OffsetTimeOriginal", "+01:00",
                "XMP-tiff:DateTime", ""
        ), tags);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> captureTags(Path file) {
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(metaDataHandlerMock).setMetaDataTags(eq(file), captor.capture());
        return captor.getValue();
    }
}
