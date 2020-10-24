package de.traber_info.home.ldap2azure.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * RequestModel used to create an new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
 *
 * @author Oliver Traber
 */
public class ApiUserCreateRequest {

    /** Username of the new user */
    @JsonProperty("username")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String username;

    /** Password of the new user */
    @JsonProperty("password")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String password;

    /** {@link Permission} for the new user */
    @JsonProperty("permission")
    @NotNull(message = "field is not supplied or invalid")
    private Permission permission;

    /** Default constructor for Jackson deserialization */
    public ApiUserCreateRequest() {}

    /**
     * Get the username for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey}.
     * @return Username for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the password for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
     * @return Password for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the {@link Permission} for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
     * @return {@link Permission} for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
     */
    public Permission getPermission() {
        return permission;
    }

}
