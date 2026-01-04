package sk.kubisoft.exifutils.cli.logging;

import ch.qos.logback.core.PropertyDefinerBase;
import sk.kubisoft.exifutils.core.utils.EnvironmentUtils;

public class LoggingDirectoryProperty extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return EnvironmentUtils.getLogsDirectory().toString();
    }

}
