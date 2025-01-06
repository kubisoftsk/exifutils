package sk.kubisoft.exifutils.rename;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import sk.kubisoft.exifutils.core.CommandArgument;
import sk.kubisoft.exifutils.core.CommandRunner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class RenameCommandRunner implements CommandRunner {

    @Inject
    public RenameCommandRunner() {
    }

    @Override
    public int runCommand(CommandLine command) {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "rename";
    }

    @Override
    public String getCommandDescription() {
        return "Renames media files based on EXIF date";
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
