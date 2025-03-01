package sk.kubisoft.exifutils.shiftdate;

import javax.inject.Inject;
import javax.inject.Singleton;

import sk.kubisoft.exifutils.core.file.FileExplorer;
import sk.kubisoft.exifutils.core.logging.Console;

@Singleton
public class ShiftDateCommand {

	private final Console console;

	private final FileExplorer fileExplorer;

	@Inject
	public ShiftDateCommand(Console console, FileExplorer fileExplorer) {
		this.console = console;
		this.fileExplorer = fileExplorer;
	}

	public void execute(ShiftDateCommandInput input) {

	}
