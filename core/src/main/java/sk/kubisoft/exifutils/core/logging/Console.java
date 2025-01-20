package sk.kubisoft.exifutils.core.logging;

public interface Console {

    boolean confirmAction(String message);

    boolean isVerbose();

    void print(String message);
    void print(String format, Object... args);

    void progress(String format, Object... args);

    void println(String message);
    void println(String format, Object... args);

    void verbose(String message);
    void verbose(String format, Object... args);

    void verboseln(String message);
    void verboseln(String format, Object... args);

    void error(String message);
    void error(String message, Throwable t);
    void error(String format, Object... args);
    void error(String format, Throwable t, Object... args);

    void errorln(String message);
    void errorln(String message, Throwable t);
    void errorln(String format, Object... args);
    void errorln(String format, Throwable t, Object... args);

}
