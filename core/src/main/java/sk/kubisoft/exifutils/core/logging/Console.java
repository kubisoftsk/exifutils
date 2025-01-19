package sk.kubisoft.exifutils.core.logging;

public interface Console {

    boolean confirmAction(String message);

    void print(String message);

    void println(String message);

    void verbose(String message);

    void verboseln(String message);

    void error(String message);

    void errorln(String message);

}
