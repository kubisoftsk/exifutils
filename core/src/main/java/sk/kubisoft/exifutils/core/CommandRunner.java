package sk.kubisoft.exifutils.core;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.List;

public interface CommandRunner {

    int runCommand(CommandLine command);

    String getCommandName();

    String getCommandDescription();

    Options getOptions();

    List<CommandArgument> getCommandArguments();

}
