package sk.kubisoft.exifutils.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration test for the main application entry point.
 * Verifies the complete application bootstrap process including Dagger component creation
 * and CLI initialization.
 */
class ExifUtilsApplicationTest {

    @Test
    void applicationShouldBootstrapDaggerComponentWithoutErrors() {
        assertThatCode(() -> {
            var component = DaggerExifUtilsModule_CliComponent.create();
            var commandRunners = component.commandRunners();
            var console = component.console();

            // Verify we got valid instances
            assertThat(commandRunners).isNotNull().isNotEmpty();
            assertThat(console).isNotNull();

            // Verify we can create the CLI
            var cli = new ExifUtilsCli(commandRunners, console);
            assertThat(cli).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    void daggerComponentShouldProvideAllSevenCommands() {
        var component = DaggerExifUtilsModule_CliComponent.create();
        var commandRunners = component.commandRunners();

        assertThat(commandRunners)
            .hasSize(7)
            .containsKeys("info", "sort", "set-date", "shift-date", "set-gps", "dedupe", "rename");
    }

    @Test
    void allProvidedCommandRunnersShouldBeInstantiable() {
        var component = DaggerExifUtilsModule_CliComponent.create();
        var commandRunners = component.commandRunners();

        // Verify all command runners are properly instantiated
        commandRunners.forEach((name, runner) -> {
            assertThat(runner).as("Runner for command '%s'", name).isNotNull();
            assertThat(runner.getCommandName()).as("Command name for '%s'", name).isEqualTo(name);
            assertThat(runner.getCommandDescription()).as("Description for '%s'", name).isNotBlank();
            assertThat(runner.getOptions()).as("Options for '%s'", name).isNotNull();
        });
    }
}
