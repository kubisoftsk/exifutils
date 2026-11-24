package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileFilterTest {

    @Test
    void matchesGlobInclude() {
        var filter = FileFilter.of(List.of("IMG_*.jpg"), List.of());

        assertThat(filter.matches(Path.of("root", "IMG_00001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("root", "DSC_4321.mov"))).isFalse();
    }

    @Test
    void matchesGlobExclude() {
        var filter = FileFilter.of(List.of(), List.of("*-WA*.jpg"));

        assertThat(filter.matches(Path.of("root", "IMG-20240715-WA0001.jpg"))).isFalse();
        assertThat(filter.matches(Path.of("root", "IMG_00001.jpg"))).isTrue();
    }

    @Test
    void matchesRegexPrefix() {
        var filter = FileFilter.of(List.of("regex:P211000[12]\\.jpg"), List.of());

        assertThat(filter.matches(Path.of("root", "first", "P2110001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("root", "first", "P2110002.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("root", "first", "P2110003.mov"))).isFalse();
    }

    @Test
    void combinedIncludeThenExclude() {
        var filter = FileFilter.of(List.of("*.jpg"), List.of("*-WA*.jpg"));

        assertThat(filter.matches(Path.of("root", "IMG-20240715-WA0001.jpg"))).isFalse();
        assertThat(filter.matches(Path.of("root", "IMG_00001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("root", "DSC_4321.mov"))).isFalse();
    }

    @Test
    void noFiltersConfiguredPassesEverything() {
        assertThat(FileFilter.NONE.matches(Path.of("root", "anything.xyz"))).isTrue();
        assertThat(FileFilter.of(List.of(), List.of()).matches(Path.of("root", "anything.xyz"))).isTrue();
    }

    @Test
    void invalidGlobPatternThrows() {
        assertThatThrownBy(() -> FileFilter.of(List.of("["), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidRegexPatternThrows() {
        assertThatThrownBy(() -> FileFilter.of(List.of("regex:("), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void matchesFileNameOnlyNotFullPath() {
        var filter = FileFilter.of(List.of("IMG_00001.jpg"), List.of());

        assertThat(filter.matches(Path.of("root", "first", "IMG_00001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("other", "IMG_00001.jpg"))).isTrue();
    }

    @Test
    void regexMustMatchWholeFileName() {
        var filter = FileFilter.of(List.of("regex:WA"), List.of());

        assertThat(filter.matches(Path.of("root", "IMG-20240715-WA0001.jpg"))).isFalse();
        assertThat(filter.matches(Path.of("root", "WA"))).isTrue();
    }

    @Test
    void regexCanIgnoreCaseWithInlineFlag() {
        var filter = FileFilter.of(List.of("regex:(?i).*\\.jpe?g"), List.of());

        assertThat(filter.matches(Path.of("root", "IMG_00001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("root", "IMG_00001.JPG"))).isTrue();
        assertThat(filter.matches(Path.of("root", "IMG_00001.JPEG"))).isTrue();
    }

    @Test
    void patternWithDirectorySeparatorNeverMatches() {
        var filter = FileFilter.of(List.of("first/*.jpg", "**/*.jpg"), List.of());

        assertThat(filter.matches(Path.of("root", "first", "P2110001.jpg"))).isFalse();
    }
}
