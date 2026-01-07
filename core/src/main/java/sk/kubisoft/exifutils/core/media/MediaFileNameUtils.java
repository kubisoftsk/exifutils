package sk.kubisoft.exifutils.core.media;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.utils.DatePatternResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.getExtension;

@Singleton
public class MediaFileNameUtils {

    private final ConfigService configService;

    @Inject
    public MediaFileNameUtils(ConfigService configService) {
        this.configService = configService;
    }

    public String createNewName(AnalyzedMediaFile mediaFile) {
        String result = getPattern();
        var mediaDate = mediaFile.getCreationDate();
        LocalDateTime dateTime = mediaDate.getLocalDateTime();

        // Resolve ${date,format} patterns
        result = DatePatternResolver.resolve(result, dateTime);

        // Replace other variables
        Map<String, String> replacements = new HashMap<>();
        replacements.put("${prefix}", getPrefix(mediaFile.getMediaType()));

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        var extension = getExtension(mediaFile.getOriginalPath().getFileName().toString());
        return result + "." + StringUtils.toRootLowerCase(extension);
    }

    private String getPrefix(MediaType mediaType) {
        return switch (mediaType) {
            case IMAGE -> "IMG";
            case VIDEO -> "VID";
            default -> throw new IllegalArgumentException("Unknown media type: " + mediaType);
        };
    }

    private String getPattern() {
        return configService.getRenamePattern();
    }
}
