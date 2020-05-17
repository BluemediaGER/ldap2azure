package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for all general configuration variables used by ldap2azure
 *
 * @author Oliver Traber
 */
public class GeneralConfig {

    /** Cron expression representing the schedule at which syncs are run */
    @JsonProperty("syncCronExpression")
    private String cronExpression;

    /** Boolean representing if debugging functionalities should be enabled */
    @JsonProperty("debuggingEnabled")
    private boolean debuggingEnabled;

    /**
     * Get the sync cron expression from the config file
     * @return Sync cron expression
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * Get if debugging functionalities should be enabled
     * @return true if debugging should be enabled, otherwise false
     */
    public boolean isDebuggingEnabled() {
        return debuggingEnabled;
    }

}