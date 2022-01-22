package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for an attribute on the source ldap server that should be loaded.
 *
 * @author Oliver Traber
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LdapAttribute {

    /** Name of the ldap attribute that should be loaded */
    @JsonProperty(value = "attributeName", required = true)
    private String attributeName;

    /** Boolean representing if the property is an binary attribute */
    @JsonProperty(value = "binary")
    private boolean isBinary = false;

    /**
     * Get the name of the ldap attribute that should be loaded.
     * @return Name of the ldap attribute that should be loaded.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /** Get if the attribute is an binary attribute.
     * @return true if the attribute is binary, otherwise false.
     */
    public boolean isBinary() {
        return isBinary;
    }
}
