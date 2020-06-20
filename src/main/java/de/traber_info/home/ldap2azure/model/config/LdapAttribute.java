package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for an attribute on the source ldap server that should be loaded.
 *
 * @author Oliver Traber
 */
public class LdapAttribute {

    /** Name of the ldap attribute that should be loaded */
    @JsonProperty("attributeName")
    private String attributeName;

    /** Boolean representing if the property is an binary attribute */
    @JsonProperty("binary")
    private boolean isBinary;

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
