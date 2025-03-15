package sk.kubisoft.exifutils.setgps;

public record SetGpsCommandInput(

        String[] inputPaths,

        Double latitude,

        Double longitude,

        boolean remove

) {
}
