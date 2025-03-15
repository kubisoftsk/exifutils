package sk.kubisoft.exifutils.core.file;

import sk.kubisoft.exifutils.core.media.MediaFile;

// NOTE: Both latitude and longitude must be either null or non-null. If null, then it means that the location should be removed.
public record SetLocationAction(

        MediaFile mediaFile,

        Double latitude,

        Double longitude

) {

    public SetLocationAction {
        if (mediaFile == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }
        if (latitude == null && longitude != null) {
            throw new IllegalArgumentException("Latitude cannot be null when longitude is not null.");
        }
        if (latitude != null && longitude == null) {
            throw new IllegalArgumentException("Longitude cannot be null when latitude is not null.");
        }
    }

    @Override
    public String toString() {
        return String.format("Set GPS location %f %f to %s", latitude, longitude, mediaFile.getOriginalPath());
    }
}
