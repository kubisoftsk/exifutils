package sk.kubisoft.exifutils.core.metadata;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.config.ConfigService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MetaDataHandlerFactory {

    private static final String DEFAULT_EXIFTOOL = "exiftool";

    private final ConfigService configService;

    @Inject
    public MetaDataHandlerFactory(ConfigService configService) {
        this.configService = configService;
    }

    public MetaDataHandler create() {
        return new MetaDataHandler(resolveExifToolPath());
    }

    String resolveExifToolPath() {
        // Executable without a directory is looked up in PATH
        return StringUtils.defaultIfBlank(configService.getExifToolPath(), DEFAULT_EXIFTOOL);
    }
}
