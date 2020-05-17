package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for the main configuration file of ldap2azure
 *
 * @author Oliver Traber
 */
public class Config {

    /** Configuration file section used to configure general settings like e.g. the sync schedule */
    @JsonProperty("general")
    private GeneralConfig generalConfig;

    /** Configuration file section used to configure the Microsoft Graph API client */
    @JsonProperty("msGraph")
    private GraphClientConfig graphClientConfig;

    /** Configuration file section used to configure LDAP connectivity */
    @JsonProperty("ldap")
    private LdapConfig ldapConfig;

    /** Configuration file section used to define patterns for building user objects */
    @JsonProperty("userBuildPattern")
    private PatternConfig patternConfig;

    /** Configuration file section used to configure the auto licensing feature */
    @JsonProperty("autoLicensing")
    private AutoLicensingConfig autoLicensingConfig;

    /** Configuration file section used to configure the web management interface and the REST API */
    @JsonProperty("web")
    private WebConfig webConfig;

    /**
     * Get the general section of the config file.
     * @return General section of the config file.
     */
    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    /**
     * Get the Microsoft Graph section of the config file.
     * @return Microsoft Graph section of the config file.
     */
    public GraphClientConfig getGraphClientConfig() {
        return graphClientConfig;
    }

    /**
     * Get the LDAP section of the config file.
     * @return LDAP section of the config file.
     */
    public LdapConfig getLdapConfig() {
        return ldapConfig;
    }

    /**
     * Get the pattern config section of the config file.
     * @return Pattern config section pof the config file.
     */
    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    /**
     * Get the auto licensing config section of the config file.
     * @return Auto licensing section pof the config file.
     */
    public AutoLicensingConfig getAutoLicencingConfig() {
        return autoLicensingConfig;
    }

    /**
     * Get the webService config section of the config file.
     * @return Web service section pof the config file.
     */
    public WebConfig getWebConfig() {
        return webConfig;
    }

}
