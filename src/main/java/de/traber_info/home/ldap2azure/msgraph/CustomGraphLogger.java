package de.traber_info.home.ldap2azure.msgraph;

import com.microsoft.graph.logger.ILogger;
import com.microsoft.graph.logger.LoggerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom implementation of {@link ILogger} to integrate with slf4j / logback.
 *
 * @author Oliver Traber
 */
public class CustomGraphLogger implements ILogger {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(CustomGraphLogger.class.getName());

    private boolean logActive = true;

    /**
     * Method to temporarily deactivate logging completely.
     * @param logActive Set to false to disable logging, set to true to enable logging.
     */
    public void setLogActive(boolean logActive) {
        this.logActive = logActive;
    }

    /**
     * Method to set the loggers loglevel. This method has no functionality in this implementation.
     * @param loggerLevel LoggerLevel that should be set. Has no function in this implementation.
     */
    @Override
    public void setLoggingLevel(LoggerLevel loggerLevel) {
        // Do nothing since the log level is handled by Logback.
    }

    /**
     * Get the current {@link LoggerLevel} of this logger. This is set statically in this implementation.
     * @return Always level ERROR since the actual level is controlled by logback.
     */
    @Override
    public LoggerLevel getLoggingLevel() {
        return LoggerLevel.ERROR;
    }

    /**
     * Method called by the GraphServiceClient to log debug messages.
     * @param msg Message that should be logged.
     */
    @Override
    public void logDebug(String msg) {
        if (logActive) LOG.debug(msg);
    }

    /**
     * Method called by the GraphServiceClient to log occurring errors.
     * @param msg Message that should be logged.
     * @param throwable Throwable that caused the error.
     */
    @Override
    public void logError(String msg, Throwable throwable) {
        if (logActive) LOG.error(msg, throwable);
    }

}
