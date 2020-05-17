package de.traber_info.home.ldap2azure.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for the patterns by which attributes of Azure AD users are build
 *
 * @author Oliver Traber
 */
public class PatternConfig {

    /** Pattern by which the given name is build */
    @JsonProperty("givenNamePattern")
    private String givenNamePattern;

    /** Pattern by which the surname is build */
    @JsonProperty("surnamePattern")
    private String surnamePattern;

    /** Pattern by which the display name is build */
    @JsonProperty("displayNamePattern")
    private String displayNamePattern;

    /** Pattern by which the on-premises immutable id is build */
    @JsonProperty("onPremisesImmutableIdPattern")
    private String onPremisesImmutableIdPattern;

    /** Pattern by which the mail nickname is build */
    @JsonProperty("mailNicknamePattern")
    private String mailNicknamePattern;

    /** Pattern by which the user principal name is build */
    @JsonProperty("userPrincipalNamePattern")
    private String userPrincipalNamePattern;

    /**
     * Get the pattern by which the given name is build
     * @return The pattern by which the given name is build
     */
    public String getGivenNamePattern() {
        return givenNamePattern;
    }

    /**
     * Get the pattern by which the surname is build
     * @return The pattern by which the surname is build
     */
    public String getSurnamePattern() {
        return surnamePattern;
    }

    /**
     * Get the pattern by which the display name is build
     * @return The pattern by which the display name is build
     */
    public String getDisplayNamePattern() {
        return displayNamePattern;
    }

    /**
     * Get the pattern by which the on-premises immutable id is build
     * @return The pattern by which the on-premises immutable id is build
     */
    public String getOnPremisesImmutableIdPattern() {
        return onPremisesImmutableIdPattern;
    }

    /**
     * Get the pattern by which the mail nickname is build
     * @return The pattern by which the mail nickname is build
     */
    public String getMailNicknamePattern() {
        return mailNicknamePattern;
    }

    /**
     *Get the pattern by which the user principal name is build
     * @return The pattern by which the user principal name is build
     */
    public String getUserPrincipalNamePattern() {
        return userPrincipalNamePattern;
    }

}
