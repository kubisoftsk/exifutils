package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    void ignoresCaseByDefault() {
        var glob = FileFilter.of(List.of("*.jpg"), List.of("ŠKÔLKA*"));
        var regex = FileFilter.of(List.of("regex:.*\\.jpg"), List.of("regex:ŠKÔLKA.*"));

        for (var filter : List.of(glob, regex)) {
            assertThat(filter.matches(Path.of("root", "IMG_00001.jpg"))).isTrue();
            assertThat(filter.matches(Path.of("root", "IMG_00001.JPG"))).isTrue();
            assertThat(filter.matches(Path.of("root", "škôlka.jpg"))).isFalse();
        }
    }

    @Test
    void regexCanMatchCaseWithInlineFlag() {
        var filter = FileFilter.of(List.of("regex:(?-i).*\\.jpg"), List.of());

        assertThat(filter.matches(Path.of("root", "IMG_00001.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("root", "IMG_00001.JPG"))).isFalse();
    }

    @Test
    void globPrefixIsExplicitFormOfDefault() {
        var filter = FileFilter.of(List.of("glob:*.{jpg,png}"), List.of("glob:*-WA*"));

        assertThat(filter.matches(Path.of("root", "IMG_00001.JPG"))).isTrue();
        assertThat(filter.matches(Path.of("root", "IMG-20240715-WA0001.jpg"))).isFalse();
        assertThat(filter.matches(Path.of("root", "DSC_4321.mov"))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "  ", "glob:", "glob: ", "regex:", "regex: ",
            "first/*.jpg", "**/*.jpg", "/", "glob:first/*.jpg", "{a,b/c}", "[a/b]", "a\\/b", "regex:first/.*\\.jpg",
            "[", "{a", "glob:[", "regex:("
    })
    void rejectsPatternThatCannotMatch(String pattern) {
        assertThatThrownBy(() -> FileFilter.of(List.of("*", pattern), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid --include pattern '" + pattern + "': ");
        assertThatThrownBy(() -> FileFilter.of(List.of(), List.of("*", pattern)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid --exclude pattern '" + pattern + "': ");
    }

    @Test
    void errorMessageDescribesTheProblem() {
        assertThatThrownBy(() -> FileFilter.of(List.of(""), List.of()))
                .hasMessage("Invalid --include pattern '': Pattern is empty");
        assertThatThrownBy(() -> FileFilter.of(List.of(), List.of("thumbs/*.jpg")))
                .hasMessageContaining("must not contain a path separator");
        assertThatThrownBy(() -> FileFilter.of(List.of(), List.of("regex:(")))
                .hasMessage("Invalid --exclude pattern 'regex:(': Unclosed group (near index 1)");
    }
}
