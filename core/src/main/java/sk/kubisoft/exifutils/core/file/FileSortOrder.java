package sk.kubisoft.exifutils.core.file;

public enum FileSortOrder {
    NAME,
    LAST_MODIFIED,
    CREATED;

    public static FileSortOrder fromString(String value) {
        if (value == null || value.isBlank()) {
            return NAME;
        }
        try {
            return FileSortOrder.valueOf(value.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid file sort order: '" + value + "'. Valid values: name, last-modified, created");
        }
    }
}
