package sk.kubisoft.exifutils.core.file;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileFilterOptionsTest {

    @Test
    void repeatedIncludeAccumulates() throws ParseException {
        var filter = parse("-i", "*.jpg", "-i", "*.png", "somedir");

        assertThat(filter.matches(Path.of("a.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("b.png"))).isTrue();
        assertThat(filter.matches(Path.of("c.mov"))).isFalse();
    }

    @Test
    void repeatedExcludeAccumulates() throws ParseException {
        var filter = parse("-x", "*.jpg", "-x", "*.png", "somedir");

        assertThat(filter.matches(Path.of("a.jpg"))).isFalse();
        assertThat(filter.matches(Path.of("b.png"))).isFalse();
        assertThat(filter.matches(Path.of("c.mov"))).isTrue();
    }

    @Test
    void commaInPatternDoesNotSplitValue() throws ParseException {
        var filter = parse("-i", "*.{jpg,png}", "-x", "a,b.jpg", "somedir");

        assertThat(filter.matches(Path.of("a.jpg"))).isTrue();
        assertThat(filter.matches(Path.of("b.png"))).isTrue();
        assertThat(filter.matches(Path.of("a,b.jpg"))).isFalse();
    }

    @Test
    void optionDoesNotSwallowPositionalArguments() throws ParseException {
        var options = new Options();
        FileFilterOptions.addTo(options);

        var cmd = new DefaultParser().parse(options, new String[]{"-i", "*.jpg", "dir1", "-x", "*.png", "dir2", "dir3"});

        assertThat(cmd.getArgs()).containsExactly("dir1", "dir2", "dir3");
    }

    @Test
    void noOptionsYieldsNoOpFilter() throws ParseException {
        var filter = parse("somedir");

        assertThat(filter.matches(Path.of("anything.xyz"))).isTrue();
    }

    @Test
    void invalidPatternThrowsParseException() {
        assertThatThrownBy(() -> parse("-i", "regex:(", "somedir"))
                .isInstanceOf(InvalidFilterPatternException.class)
                .hasMessageContaining("regex:(");
    }

    private FileFilter parse(String... args) throws ParseException {
        var options = new Options();
        FileFilterOptions.addTo(options);

        var cmd = new DefaultParser().parse(options, args);
        return FileFilterOptions.parse(cmd);
    }
}
