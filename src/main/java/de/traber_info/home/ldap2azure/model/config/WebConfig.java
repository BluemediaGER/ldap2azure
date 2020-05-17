package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for all configuration variables that are used by the web management interface and the REST API
 *
 * @author Oliver Traber
 */
public class WebConfig {

    /** Boolean representing if the web management and REST API should be enabled */
    @JsonProperty(value = "featureEnabled", defaultValue = "false")
    private boolean enabled;

    /** Password used to protect the web management interface */
    @JsonProperty(value = "password")
    private String password;

    /** Port on which the web application server should be listening */
    @JsonProperty(value = "port")
    private int port;

    /**
     * Check if the web management interface should be enabled
     * @return true if the web management interface should be enabled, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the password used to protect the web management interface
     * @return The password used to protect the web management interface
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the port on which the web application server should be listening
     * @return The port on which the web application server should be listening
     */
    public int getPort() {
        return port;
    }

}
