package sk.kubisoft.exifutils.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies CLI command runners are properly configured.
 * Tests command registration, descriptions, and options without executing actual commands.
 */
class CliIntegrationTest {

    private ExifUtilsModule.CliComponent component;
    private Map<String, CommandRunner> commandRunners;
    private Console console;

    @BeforeEach
    void setUp() {
        component = DaggerExifUtilsModule_CliComponent.create();
        commandRunners = component.commandRunners();
        console = component.console();
    }

    @Test
    void shouldHaveAllRequiredCommands() {
        assertThat(commandRunners)
            .containsKeys("info", "sort", "set-date", "shift-date", "set-gps", "rename");
    }

    @ParameterizedTest
    @ValueSource(strings = {"info", "sort", "set-date", "shift-date", "set-gps", "rename"})
    void commandShouldHaveNonNullDescription(String commandName) {
        CommandRunner runner = commandRunners.get(commandName);

        assertThat(runner.getCommandDescription())
            .isNotNull()
            .isNotBlank();
    }

    @ParameterizedTest
    @ValueSource(strings = {"info", "sort", "set-date", "shift-date", "set-gps", "rename"})
    void commandShouldHaveNonNullOptions(String commandName) {
        CommandRunner runner = commandRunners.get(commandName);

        assertThat(runner.getOptions()).isNotNull();
    }

    @Test
    void cliShouldBeInstantiable() {
        // This verifies the CLI can be created with injected dependencies
        ExifUtilsCli cli = new ExifUtilsCli(commandRunners, console);

        assertThat(cli).isNotNull();
    }

    @Test
    void allCommandsShouldHaveMatchingNames() {
        commandRunners.forEach((key, runner) -> {
            assertThat(runner.getCommandName())
                .as("Command name should match map key")
                .isEqualTo(key);
        });
    }
}
