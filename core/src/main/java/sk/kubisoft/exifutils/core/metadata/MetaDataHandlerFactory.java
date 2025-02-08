package sk.kubisoft.exifutils.core.metadata;

import sk.kubisoft.exifutils.core.config.ConfigService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MetaDataHandlerFactory {

    private final ConfigService configService;

    @Inject
    public MetaDataHandlerFactory(ConfigService configService) {
        this.configService = configService;
    }

    public MetaDataHandler create() {
        var exifToolConfig = configService.getConfig().getExifTool();
        if (exifToolConfig == null || exifToolConfig.getPath() == null) {
            throw new IllegalArgumentException("ExifTool path not configured, please check config file.");
        }
        return new MetaDataHandler(exifToolConfig.getPath());
    }
}
