package sk.kubisoft.exifutils.setgps;

public record SetGpsCommandInput(

        String[] sourcePaths,

        Double latitude,

        Double longitude,

        boolean remove

) {
}
