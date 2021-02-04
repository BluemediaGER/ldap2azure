package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Model for all configuration variables used to configure the auto licensing feature of ldap2azure
 *
 * @author Oliver Traber
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoLicensingConfig {

    /** Boolean representing if auto license assignment should be enabled */
    @JsonProperty(value = "featureEnabled")
    private boolean enabled = false;

    /** List of default license SKUs that should be assigned when new users are created */
    @JsonProperty("defaultLicenseSkuIDs")
    private List<String> defaultLicenceSkuIDs;

    /**
     * Check if the auto licensing feature should be enabled
     * @return true if auto licensing should be enabled, otherwise false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the list of default license SKUs that should be assigned when new users are created
     * @return The list of default license SKUs that should be assigned when new users are created
     */
    public List<String> getDefaultLicenceSkuIDs() {
        return defaultLicenceSkuIDs;
    }

}
