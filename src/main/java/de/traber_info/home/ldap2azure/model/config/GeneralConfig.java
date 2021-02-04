package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.traber_info.home.ldap2azure.util.ConfigUtil;

/**
 * Model for all general configuration variables used by ldap2azure
 *
 * @author Oliver Traber
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralConfig {

    /** Cron expression representing the schedule at which syncs are run */
    @JsonProperty(value = "syncCronExpression", required = true)
    private String cronExpression;

    /** Boolean representing if debugging functionalities should be enabled */
    @JsonProperty("debuggingEnabled")
    private boolean debuggingEnabled = false;

    /** Optional jdbc url for use with other databases like mysql */
    @JsonProperty("databaseJDBCUrl")
    private String databaseJDBCUrl;

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

    /**
     * Get the JDBC url that should be used to persist the internal userid.
     * @return JDBC url that should be used for the internal database. If not set to fall back to local H2 database.
     */
    public String getDatabaseJDBCUrl() {
        if (databaseJDBCUrl == null || "".equals(databaseJDBCUrl)) {
            return "jdbc:h2:" + ConfigUtil.getJarPath() + "/ldap2azure";
        }
        return databaseJDBCUrl;
    }
}