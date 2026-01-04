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
        return new MetaDataHandler(configService.getExifToolPath());
    }
}
