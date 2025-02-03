package sk.kubisoft.exifutils.core.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.kubisoft.exifutils.core.config.ConfigService;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.core.media.MediaDateTime;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

class MediaDateExtractor {

    private static final Logger logger = LoggerFactory.getLogger(MediaDateExtractor.class);

    private final Console console;
    private final ConfigService configService;

    public MediaDateExtractor(Console console, ConfigService configService) {
        this.configService = configService;
        this.console = console;
    }

    public Optional<MediaDateTime> extractCreationDate(MediaType mediaType, Map<String, String> metadata) {



        if (mediaType == MediaType.VIDEO) {
            // assume the video date is in UTC time, this is important for videos, because historically
            // quick time videos has the date in UTC time, so we must convert it to local time with guessed offset
            OffsetDateTime utcDateTime = localDateTime.atOffset(ZoneOffset.UTC);
            var localTimeAtOffsetSameInstant = utcDateTime.withOffsetSameInstant(offsetToUse).toLocalDateTime();
            return Optional.of(new MediaDateTime(localTimeAtOffsetSameInstant, offsetToUse));
        } else if (mediaType == MediaType.IMAGE) {
            // similar to video, but we assume the image date is in local time, so we don't need to convert it, just use it
            return Optional.of(new MediaDateTime(localDateTime, offsetToUse));
        } else {
            throw new IllegalArgumentException("Unknown media type: " + mediaType);
        }
    }




}

