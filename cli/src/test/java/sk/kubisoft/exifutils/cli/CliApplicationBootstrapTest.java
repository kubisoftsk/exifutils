package sk.kubisoft.exifutils.cli;

import org.junit.jupiter.api.Test;
import sk.kubisoft.exifutils.core.CommandRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the Dagger dependency injection setup is correct.
 * Ensures all components can be created and all command runners are properly wired.
 */
class CliApplicationBootstrapTest {

    @Test
    void shouldCreateDaggerComponentSuccessfully() {
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();

        assertThat(component).isNotNull();
    }

    @Test
    void shouldInjectConsole() {
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();

        var console = component.console();

        assertThat(console).isNotNull();
    }

    @Test
    void shouldInjectAllCommandRunners() {
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();

        Map<String, CommandRunner> commandRunners = component.commandRunners();

        assertThat(commandRunners)
            .isNotNull()
            .isNotEmpty()
            .containsKeys("info", "sort", "set-date", "shift-date", "set-gps", "rename");
    }

    @Test
    void shouldHaveSixCommandRunners() {
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();

        Map<String, CommandRunner> commandRunners = component.commandRunners();

        assertThat(commandRunners).hasSize(6);
    }

    @Test
    void allCommandRunnersShouldHaveNonNullImplementations() {
        ExifUtilsModule.CliComponent component = DaggerExifUtilsModule_CliComponent.create();

        Map<String, CommandRunner> commandRunners = component.commandRunners();

        commandRunners.forEach((name, runner) -> {
            assertThat(runner)
                .as("CommandRunner for '%s' should not be null", name)
                .isNotNull();
            assertThat(runner.getCommandName())
                .as("CommandRunner name should match key")
                .isEqualTo(name);
        });
    }
}
