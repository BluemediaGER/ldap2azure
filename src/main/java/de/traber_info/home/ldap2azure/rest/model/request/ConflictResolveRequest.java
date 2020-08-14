package de.traber_info.home.ldap2azure.rest.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.traber_info.home.ldap2azure.rest.model.types.ConflictResolveStrategy;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Request model used to resolve user conflicts.
 *
 * @author Oliver Traber
 */
public class ConflictResolveRequest {

    /**
     * Azure user id that should be merged or deleted to resolve the conflict
     */
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    @JsonProperty("azureImmutableId")
    private String azureImmutableId;

    /**
     * Strategy that defines how the conflict get's resolves.
     * MERGE updates the Azure user to resolve the conflict. RECREATE deletes the supplied user and creates a new one.
     */
    @NotNull(message = "field is empty or invalid")
    @JsonProperty("strategy")
    private ConflictResolveStrategy strategy;

    /** Default constructor for Jackson deserialization */
    public ConflictResolveRequest() {}

    /**
     * Get the azureImmutableId sent by the client.
     * @return azureImmutableId sent by the client.
     */
    public String getAzureImmutableId() {
        return azureImmutableId;
    }

    /**
     * Get the resolve strategy sent by the client.
     * @return resolve strategy sent by the client.
     */
    public ConflictResolveStrategy getStrategy() {
        return strategy;
    }

}
