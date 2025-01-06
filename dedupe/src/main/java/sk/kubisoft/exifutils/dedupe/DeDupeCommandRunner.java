package sk.kubisoft.exifutils.dedupe;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DeDupeCommandRunner implements CommandRunner {

    @Inject
    public DeDupeCommandRunner() {
    }

    @Override
    public int runCommand(CommandLine command) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "dedupe";
    }

    @Override
    public String getCommandDescription() {
        return "Find and remove duplicate media files";
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
