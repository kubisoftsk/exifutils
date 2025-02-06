package sk.kubisoft.exifutils.core.analysis;

import javax.inject.Inject;
import javax.inject.Singleton;

import sk.kubisoft.exifutils.core.config.ConfigService;

@Singleton
public class MetaDataExtractorFactory {

	private final ConfigService configService;

	@Inject
	public MetaDataExtractorFactory(ConfigService configService) {
		this.configService = configService;
	}

	public MetaDataExtractor newMetaDataExtractor() {
		var exifToolConfig = configService.getConfig().getExifTool();
		if (exifToolConfig == null || exifToolConfig.getPath() == null) {
			throw new IllegalArgumentException("ExifTool path not configured");
		}
		return new MetaDataExtractor(exifToolConfig.getPath());
	}
}
