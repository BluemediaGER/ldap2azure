package de.traber_info.home.ldap2azure.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;

import jakarta.validation.constraints.NotNull;

/**
 * Request model used to change the permission of an existing
 * {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey} or
 * {@link de.traber_info.home.ldap2azure.rest.model.object.ApiUser}.
 *
 * @author Oliver Traber
 */
public class PermissionUpdateRequest {

    /**
     * New {@link Permission} that should be set on the updated object.
     */
    @JsonProperty("permission")
    @NotNull(message = "field is not supplied or invalid")
    private Permission permission;

    /** Default constructor for Jackson deserialization */
    public PermissionUpdateRequest() {}

    /**
     * Get the new {@link Permission} for the updated object.
     * @return {@link Permission} for the updated object.
     */
    public Permission getPermission() {
        return permission;
    }

}
