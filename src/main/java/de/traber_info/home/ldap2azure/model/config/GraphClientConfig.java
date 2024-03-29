package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.traber_info.home.ldap2azure.model.type.DeleteBehavior;

/**
 * Model for all configuration variables used by the Microsoft Graph SDK for communication with the Microsoft Graph API
 *
 * @author Oliver Traber
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphClientConfig {

    /** Id of the Azure AD tenant used to contact the tenant specific OAuth 2.0 Authority */
    @JsonProperty(value = "msGraphTenantId", required = true)
    private String tenantId;

    /** Client id generated by Azure AD used to identify the corresponding application in Azure AD */
    @JsonProperty(value = "msGraphClientId", required = true)
    private String clientId;

    /** Client secret used to authenticate against Azure AD */
    @JsonProperty(value = "msGraphClientSecret", required = true)
    private String clientSecret;

    /** Usage location for new users. Necessary for some Office apps */
    @JsonProperty(value = "usageLocation", required = true)
    private String usageLocation;

    /** Behavior how users are deleted from Azure AD */
    @JsonProperty("deleteBehavior")
    private DeleteBehavior deleteBehavior = DeleteBehavior.SOFT;

    /**
     * Get the tenant specific authorisation authority from the config file
     * @return Tenant specific authority
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get the Azure AD client id from the config file
     * @return Azure AD client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the Azure AD client secret from the config file
     * @return Azure AD client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get the usage location for new users from the config file
     * @return Usage location for new users
     */
    public String getUsageLocation() {
        return usageLocation;
    }

    /**
     * Get the behavior how users are deleted from Azure AD
     * @return Behavior how users are deleted from Azure AD
     */
    public DeleteBehavior getDeleteBehavior() {
        return deleteBehavior;
    }
}
