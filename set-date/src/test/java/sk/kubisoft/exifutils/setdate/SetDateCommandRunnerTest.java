package sk.kubisoft.exifutils.setdate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.logging.Console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SetDateCommandRunnerTest {

    @Mock
    private Console console;

    @Mock
    private SetDateCommand setDateCommand;

    private SetDateCommandRunner runner;

    @BeforeEach
    void setUp() {
        runner = new SetDateCommandRunner(console, setDateCommand);
    }

    @Test
    void shouldHaveFixZoneOption() {
        var options = runner.getOptions();

        assertThat(options.hasOption("f")).isTrue();
        assertThat(options.hasOption("fix-zone")).isTrue();
    }

    @Test
    void shouldParseFixZoneFlag() throws Exception {
        var cmd = parseArgs("-f", "somedir");

        assertThat(cmd.hasOption("f")).isTrue();
    }

    @Test
    void shouldParseFixZoneLongFlag() throws Exception {
        var cmd = parseArgs("--fix-zone", "somedir");

        assertThat(cmd.hasOption("fix-zone")).isTrue();
    }

    @Test
    void shouldParseFixZoneWithZoneId() throws Exception {
        var cmd = parseArgs("-f", "-z", "Europe/Paris", "somedir");

        assertThat(cmd.hasOption("f")).isTrue();
        assertThat(cmd.hasOption("z")).isTrue();
        assertThat(cmd.getOptionValue("z")).isEqualTo("Europe/Paris");
    }

    @Test
    void shouldRejectFixZoneWithDateTime() throws Exception {
        int result = runCommand("-f", "-d", "2021-01-01 12:00:00", "somedir");

        assertThat(result).isEqualTo(1);
        var captor = ArgumentCaptor.forClass(ParseException.class);
        verify(console).errorln(any(), captor.capture());
        assertThat(captor.getValue().getMessage()).contains("--fix-zone and --date-time are mutually exclusive");
    }

    @Test
    void shouldRejectFixZoneWithPattern() throws Exception {
        int result = runCommand("-f", "-p", "yyyy-MM-dd", "somedir");

        assertThat(result).isEqualTo(1);
        var captor = ArgumentCaptor.forClass(ParseException.class);
        verify(console).errorln(any(), captor.capture());
        assertThat(captor.getValue().getMessage()).contains("--fix-zone and --pattern are mutually exclusive");
    }

    @Test
    void shouldRejectFixZoneWithUnknownOnly() throws Exception {
        int result = runCommand("-f", "-u", "somedir");

        assertThat(result).isEqualTo(1);
        var captor = ArgumentCaptor.forClass(ParseException.class);
        verify(console).errorln(any(), captor.capture());
        assertThat(captor.getValue().getMessage()).contains("--fix-zone and --unknown are mutually exclusive");
    }

    @Test
    void shouldAllowFixZoneWithRename() throws Exception {
        var cmd = parseArgs("-f", "-r", "somedir");

        assertThat(cmd.hasOption("f")).isTrue();
        assertThat(cmd.hasOption("r")).isTrue();
    }

    private CommandLine parseArgs(String... args) throws ParseException {
        var parser = new DefaultParser();
        return parser.parse(runner.getOptions(), args);
    }

    private int runCommand(String... args) throws ParseException {
        var cmd = parseArgs(args);
        return runner.runCommand(cmd);
    }
}
