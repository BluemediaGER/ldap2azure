package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Model for all configuration variables used to connect and read from the source LDAP.
 *
 * @author Oliver Traber
 */
public class LdapConfig {

    /** URL that specifies to which LDAP server ldap2azure should connect to */
    @JsonProperty("ldapUrl")
    private String ldapUrl;

    /** Username which is used to bind to the LDAP server. In most cases this is the DN of the user. */
    @JsonProperty("ldapBindUser")
    private String bindUser;

    /** Password of the user which is used to bind to the LDAP server */
    @JsonProperty("ldapBindPassword")
    private String bindPassword;

    /** Base DN where users are searched in */
    @JsonProperty("ldapSearchBase")
    private String searchBase;

    /** LDAP filter which is used to determine which users should be synced */
    @JsonProperty("ldapSearchFilter")
    private String searchFilter;

    /** Attributes which should be read from the LDAP directory.
     * These can be used in user build pattern to build the user object.
     */
    @JsonProperty("ldapAttributes")
    private List<LdapAttribute> ldapAttributes;

    /** Boolean representing if SSL certificate errors should be ignored while connection to the LDAP server */
    @JsonProperty("ignoreSSLErrors")
    private boolean ignoreSSLErrors;

    /**
     * Get the URL that specifies to which LDAP server ldap2azure should connect to
     * @return The URL that specifies to which LDAP server ldap2azure should connect to
     */
    public String getLdapUrl() {
        return ldapUrl;
    }

    /**
     * Get the username which is used to bind to the LDAP server. In most cases this is the DN of the user.
     * @return The username which is used to bind to the LDAP server
     */
    public String getBindUser() {
        return bindUser;
    }

    /**
     * Get the password of the user which is used to bind to the LDAP server
     * @return The password of the user which is used to bind to the LDAP server
     */
    public String getBindPassword() {
        return bindPassword;
    }

    /**
     * Get the base DN where users are searched in
     * @return The base DN where users are searched in
     */
    public String getSearchBase() {
        return searchBase;
    }

    /**
     * Get the LDAP filter which is used to determine which users should be synced
     * @return The LDAP filter which is used to determine which users should be synced
     */
    public String getSearchFilter() {
        return searchFilter;
    }

    /**
     * Get the properties which should be read from the LDAP directory. These can be used in user build pattern to build the user object.
     * @return The properties which should be read from the LDAP directory
     */
    public List<LdapAttribute> getLdapAttributes() {
        return ldapAttributes;
    }

    /**
     * Check if SSL certificate errors should be ignored while connection to the LDAP server
     * @return true if SSL certificate errors should be ignored while connection to the LDAP server, otherwise false
     */
    public boolean isIgnoreSSLErrors() {
        return ignoreSSLErrors;
    }

}
