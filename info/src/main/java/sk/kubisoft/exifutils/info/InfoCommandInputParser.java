package sk.kubisoft.exifutils.info;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import sk.kubisoft.exifutils.core.file.FileExplorer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.List;

@Singleton
public class InfoCommandInputParser {

    private final FileExplorer fileExplorer;

    @Inject
    public InfoCommandInputParser(FileExplorer fileExplorer) {
        this.fileExplorer = fileExplorer;
    }

    public InfoCommandInput parseInput(CommandLine cmd) throws ParseException {
        String[] args = cmd.getArgs();
        if (args.length == 0) {
            throw new ParseException("No source file / directory provided.");
        }

        List<Path> sourceDirs = fileExplorer.listFiles(args);

        var printAll = cmd.hasOption(InfoCommandRunner.PRINT_ALL.getOpt());

        return new InfoCommandInput(sourceDirs, printAll);
    }
}
