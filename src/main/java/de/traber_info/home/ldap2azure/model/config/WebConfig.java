package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for all configuration variables that are used by the web management interface and the REST API
 *
 * @author Oliver Traber
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebConfig {

    /** Boolean representing if the web management and REST API should be enabled */
    @JsonProperty(value = "featureEnabled")
    private boolean enabled = false;

    /** Port for HTTP on which the web application server should be listening */
    @JsonProperty(value = "httpPort")
    private int httpPort = 8080;

    /** Port for HTTPs on which the web application server should be listening */
    @JsonProperty(value = "httpsPort")
    private int httpsPort = 8443;

    /** Password for the keystore file if HTTPs should be enabled */
    @JsonProperty(value = "keystorePassword")
    private String keystorePassword = "changeit";

    /** Boolean to set if HTTP requests should be redirected to HTTPs */
    @JsonProperty(value = "redirectHttp")
    private boolean redirectHttp = true;

    /**
     * Check if the web management interface should be enabled
     * @return true if the web management interface should be enabled, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the HTTP port on which the web application server should be listening
     * @return The HTTP port on which the web application server should be listening
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Get the HTTPs port on which the web application server should be listening
     * @return The HTTPs port on which the web application server should be listening
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Get the password for the keystore file if HTTPs should be enabled
     * @return The password for the keystore file if HTTPs should be enabled
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * Get if HTTP requests should be redirected to HTTPs.
     * @return true if HTTP requests should be redirected to HTTPs, otherwise false.
     */
    public boolean shouldRedirectHttp() {
        return redirectHttp;
    }
}
