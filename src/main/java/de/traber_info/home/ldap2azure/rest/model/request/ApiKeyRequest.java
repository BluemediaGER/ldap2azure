package de.traber_info.home.ldap2azure.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * RequestModel used to create an new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey}.
 *
 * @author Oliver Traber
 */
public class ApiKeyRequest {

    /** Name for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey} */
    @JsonProperty("keyName")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String keyName;

    /** Private default constructor for Jackson deserialization */
    private ApiKeyRequest() {}

    /**
     * Get the name for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey}.
     * @return Name for the new {@link de.traber_info.home.ldap2azure.rest.model.object.ApiKey}.
     */
    public String getKeyName() {
        return keyName;
    }

}
