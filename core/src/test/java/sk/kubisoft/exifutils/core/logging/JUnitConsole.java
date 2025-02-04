package sk.kubisoft.exifutils.core.logging;

public class JUnitConsole implements Console {

	@Override
	public boolean confirmAction(String message) {
		// Always confirm for testing purposes
		return true;
	}

	@Override
	public boolean isVerbose() {
		// Return false for testing to minimize output
		return false;
	}

	@Override
	public void print(String message) {
		System.out.print(message);
	}

	@Override
	public void print(String format, Object... args) {
		System.out.print(String.format(format, args));
	}

	@Override
	public void progress(String format, Object... args) {
		// For testing, we'll treat progress same as print
		print(format, args);
	}

	@Override
	public void println(String message) {
		System.out.println(message);
	}

	@Override
	public void println(String format, Object... args) {
		System.out.println(String.format(format, args));
	}

	@Override
	public void verbose(String message) {
		// Skip verbose messages since isVerbose() returns false
	}

	@Override
	public void verbose(String format, Object... args) {
		// Skip verbose messages since isVerbose() returns false
	}

	@Override
	public void verboseln(String message) {
		// Skip verbose messages since isVerbose() returns false
	}

	@Override
	public void verboseln(String format, Object... args) {
		// Skip verbose messages since isVerbose() returns false
	}

	@Override
	public void error(String message) {
		System.err.print(message);
	}

	@Override
	public void error(String message, Throwable t) {
		System.err.print(message);
		t.printStackTrace(System.err);
	}

	@Override
	public void error(String format, Object... args) {
		System.err.print(String.format(format, args));
	}

	@Override
	public void error(String format, Throwable t, Object... args) {
		System.err.print(String.format(format, args));
		t.printStackTrace(System.err);
	}

	@Override
	public void errorln(String message) {
		System.err.println(message);
	}

	@Override
	public void errorln(String message, Throwable t) {
		System.err.println(message);
		t.printStackTrace(System.err);
	}

	@Override
	public void errorln(String format, Object... args) {
		System.err.println(String.format(format, args));
	}

	@Override
	public void errorln(String format, Throwable t, Object... args) {
		System.err.println(String.format(format, args));
		t.printStackTrace(System.err);
	}
}