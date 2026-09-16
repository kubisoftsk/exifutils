package sk.kubisoft.exifutils.info;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.file.FileFilter;
import sk.kubisoft.exifutils.core.file.FileFilterOptions;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InfoCommandInputParser {

    @Inject
    public InfoCommandInputParser() {
    }

    public InfoCommandInput parseInput(CommandLine cmd) throws ParseException {
        String[] args = cmd.getArgs();

        var printAll = cmd.hasOption(InfoCommandRunner.PRINT_ALL.getOpt());

        FileFilter filter = FileFilterOptions.parse(cmd);

        return new InfoCommandInput(args, printAll, filter);
    }
}
