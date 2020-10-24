package de.traber_info.home.ldap2azure.rest.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import de.traber_info.home.ldap2azure.h2.persister.LocalDateTimePersister;
import de.traber_info.home.ldap2azure.h2.persister.PermissionPersister;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;
import de.traber_info.home.ldap2azure.util.RandomString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ApiKey object model that holds all information about an persistent REST api key including the authentication key.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "api_keys")
public class ApiKey {

    /** Instance of the {@link RandomString} used to generate the session keys */
    private static final RandomString random = new RandomString(64);

    /** Internal id of the session */
    @DatabaseField(id = true)
    private String id;

    /** Name for the api key */
    @DatabaseField
    private String keyName;

    /** Key used by the client to authenticate itself */
    @DatabaseField
    @JsonIgnore
    private String authenticationKey;

    /** Permission level for the api key */
    @DatabaseField(persisterClass = PermissionPersister.class)
    public Permission permission;

    /** {@link LocalDateTime} the api key was last used to make an api call. */
    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    private LocalDateTime lastAccessTime;

    /**
     * Create an new instance and generate an random key id and an random authentication key.
     */
    public ApiKey(String keyName, Permission permission) {
        this.id = UUID.randomUUID().toString();
        this.keyName = keyName;
        this.authenticationKey = random.nextString();
        this.lastAccessTime = LocalDateTime.now();
        this.permission = permission;
    }

    /**
     * No-Arg constructor used by ORMLite
     */
    private ApiKey() {}

    /**
     * Get the api keys id.
     * @return The api keys id.
     */
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    /**
     * Get the api keys name.
     * @return The api keys name.
     */
    @JsonProperty("name")
    public String getKeyName() {
        return keyName;
    }

    /**
     * Get the api keys authentication key.
     * @return The api keys authentication key.
     */
    @JsonIgnore
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    /**
     * Get the {@link LocalDateTime} the api key was last used to make an api call.
     * @return {@link LocalDateTime} the api key was last used to make an api call.
     */
    @JsonProperty("lastUsed")
    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Get the {@link Permission} of the api key.
     * @return {@link Permission} of the api key.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Reset the {@link LocalDateTime} the api key was last used to make an api call to the current time.
     */
    public void resetLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * Update the {@link Permission} of the {@link ApiKey}.
     * @param permission New {@link Permission} that should be set.
     */
    @JsonIgnore
    public void updatePermission(Permission permission) {
        this.permission = permission;
    }

}
