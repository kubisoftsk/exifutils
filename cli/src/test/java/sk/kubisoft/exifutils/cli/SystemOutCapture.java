package sk.kubisoft.exifutils.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to capture System.out and System.err output during tests.
 */
public class SystemOutCapture implements AutoCloseable {
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private final ByteArrayOutputStream outCapture;
    private final ByteArrayOutputStream errCapture;

    public SystemOutCapture() {
        this.originalOut = System.out;
        this.originalErr = System.err;
        this.outCapture = new ByteArrayOutputStream();
        this.errCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture));
        System.setErr(new PrintStream(errCapture));
    }

    public String getOut() {
        return outCapture.toString(StandardCharsets.UTF_8);
    }

    public String getErr() {
        return errCapture.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}
