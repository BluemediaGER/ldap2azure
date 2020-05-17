package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for all configuration variables used to configure the auto licensing feature of ldap2azure
 *
 * @author Oliver Traber
 */
public class AutoLicensingConfig {

    /** Boolean representing if auto license assignment should be enabled */
    @JsonProperty(value = "featureEnabled", defaultValue = "false")
    private boolean enabled;

    /** Default license SKU that should be assigned when new users are created */
    @JsonProperty("defaultLicenseSkuId")
    private String defaultLicenceSkuId;

    /**
     * Check if the auto licensing feature should be enabled
     * @return true if auto licensing should be enabled, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the default license SKU that should be assigned when new users are created
     * @return The default license SKU that should be assigned when new users are created
     */
    public String getDefaultLicenceSkuId() {
        return defaultLicenceSkuId;
    }

}
