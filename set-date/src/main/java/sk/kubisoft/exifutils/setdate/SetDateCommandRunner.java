package sk.kubisoft.exifutils.setdate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SetDateCommandRunner implements CommandRunner {

    @Inject
    public SetDateCommandRunner() {
    }

    @Override
    public int runCommand(CommandLine command) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "set-date";
    }

    @Override
    public String getCommandDescription() {
        return "Sets created date of media files";
    }

    @Override
    public Options getOptions() {
        return new Options();
    }

    @Override
    public List<CommandArgument> getCommandArguments() {
        return List.of();
    }
}
