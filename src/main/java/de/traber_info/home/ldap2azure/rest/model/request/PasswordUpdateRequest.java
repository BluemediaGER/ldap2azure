package de.traber_info.home.ldap2azure.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Request model used to change the password of an existing
 * {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
 *
 * @author Oliver Traber
 */
public class PasswordUpdateRequest {

    /** New password that should be set for the user */
    @JsonProperty("password")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String password;

    /** Default constructor for Jackson deserialization */
    public PasswordUpdateRequest() {}

    /**
     * Get the new password for for the user.
     * @return New password for for the user.
     */
    public String getPassword() {
        return password;
    }
}
