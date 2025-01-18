package sk.kubisoft.exifutils.sort;

import org.apache.commons.lang3.StringUtils;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FilenameUtils.getExtension;

@Singleton
public class MediaFileRenamer {

    private final ConfigService configService;

    @Inject
    public MediaFileRenamer(ConfigService configService) {
        this.configService = configService;
    }

    public String rename(MediaFile mediaFile, MediaDateTime date) {
        String result = getPattern();
        LocalDateTime dateTime = date.getLocalDateTime();

        // Find all ${date,format} patterns
        Matcher dateMatcher = Pattern.compile("\\$\\{date,([^}]+)}").matcher(result);
        StringBuffer sb = new StringBuffer();
        while (dateMatcher.find()) {
            String format = dateMatcher.group(1);
            String formatted = dateTime.format(DateTimeFormatter.ofPattern(format));
            dateMatcher.appendReplacement(sb, formatted);
        }
        dateMatcher.appendTail(sb);
        result = sb.toString();

        // Replace other variables
        Map<String, String> replacements = new HashMap<>();
        replacements.put("${prefix}", getPrefix(mediaFile.mediaType()));

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        var extension = getExtension(mediaFile.originalPath().getFileName().toString());
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
        var rename = configService.getConfig().getRename();
        if (rename == null || rename.getPattern() == null) {
            throw new IllegalArgumentException("Rename pattern not configured");
        }
        return rename.getPattern();
    }
}
