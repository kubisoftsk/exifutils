package sk.kubisoft.exifutils.core.file;

import java.nio.file.Path;
import java.util.Objects;

public record MoveAction(
        Path source,

        Path target
) {

    public MoveAction {
        if (source == null) {
            throw new IllegalArgumentException("Source path cannot be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target path cannot be null.");
        }
    }

    @Override
    public String toString() {
        if (Objects.equals(target.getParent(), source.getParent())) {
            return String.format("%s to %s", source, target.getFileName());
        } else {
            return String.format("%s to %s", source, target);
        }
    }
}
