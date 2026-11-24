package sk.kubisoft.exifutils.core.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.config.ConfigService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataHandlerFactoryTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private MetaDataHandlerFactory factory;

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void shouldUseExifToolFromPathWhenPathIsNotConfigured(String configuredPath) {
        when(configService.getExifToolPath()).thenReturn(configuredPath);

        assertThat(factory.resolveExifToolPath()).isEqualTo("exiftool");
    }

    @Test
    void shouldUseConfiguredPath() {
        when(configService.getExifToolPath()).thenReturn("/opt/exiftool/exiftool");

        assertThat(factory.resolveExifToolPath()).isEqualTo("/opt/exiftool/exiftool");
    }
}
