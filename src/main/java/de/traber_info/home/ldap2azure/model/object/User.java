package de.traber_info.home.ldap2azure.model.object;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import de.traber_info.home.ldap2azure.h2.persister.ChangeStatePersister;
import de.traber_info.home.ldap2azure.h2.persister.LocalDateTimePersister;
import de.traber_info.home.ldap2azure.h2.persister.SyncStatePersister;
import de.traber_info.home.ldap2azure.model.type.ChangeState;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * User object model that holds all information about a user that is being synced.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(User.class.getName());

    /** Internal id of the user in ldap2azure */
    @DatabaseField(id = true)
    private String id;

    /** Immutable id of the user in the source ldap */
    @DatabaseField
    private String onPremisesImmutableId;

    /** Immutable id of the user in Azure AD */
    @DatabaseField
    private String azureImmutableId;

    /** Given name of the user in Azure AD */
    @DatabaseField
    private String givenName;

    /** Surname of the user in Azure AD */
    @DatabaseField
    private String surname;

    /** Display name of the user of the user in Azure AD */
    @DatabaseField
    private String displayName;

    /** Mail nickname of the user in Azure AD */
    @DatabaseField
    private String mailNickname;

    /** Principal name of the user in Azure AD */
    @DatabaseField
    private String userPrincipalName;

    /** Hash of the users details for quick check if the user needs to be updated */
    @DatabaseField
    private String hash;

    /** Current sync state of the user */
    @DatabaseField(persisterClass = SyncStatePersister.class)
    private SyncState syncState;

    /** Current change state of the user */
    @DatabaseField(persisterClass = ChangeStatePersister.class)
    private ChangeState changeState;

    /** {@link LocalDateTime} the user was last changed on */
    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    private LocalDateTime lastChanged;

    /** Id of the last sync that made changed on the user object */
    @DatabaseField
    private String lastSyncId;

    /**
     * Default constructor for deserialization.
     */
    private User() {}

    /**
     * Public constructor used to create a new user object.
     * @param id Internal id of the user in ldap2azure
     * @param onPremisesImmutableId Immutable id of the user in the source ldap
     * @param azureImmutableId Immutable id of the user in Azure AD
     * @param givenName Given name of the user in Azure AD
     * @param surname Surname of the user in Azure AD
     * @param displayName Display name of the user of the user in Azure AD
     * @param mailNickname Mail nickname of the user in Azure AD
     * @param userPrincipalName Principal name of the user in Azure AD
     */
    public User(String id, String onPremisesImmutableId, String azureImmutableId, String givenName, String surname,
                String displayName, String mailNickname, String userPrincipalName) {
        this.id = id;
        this.onPremisesImmutableId = onPremisesImmutableId;
        this.azureImmutableId = azureImmutableId;
        this.givenName = givenName;
        this.surname = surname;
        this.displayName = displayName;
        this.mailNickname = mailNickname;
        this.userPrincipalName = userPrincipalName;
        this.lastChanged = LocalDateTime.now();

        String hashBase = this.givenName + this.surname + this.displayName + this.mailNickname + this.userPrincipalName;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            this.hash = Base64.getEncoder().encodeToString(digest.digest(hashBase.getBytes()));
            LOG.trace("Converted {} to hash {}", hashBase, this.hash);
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Set the internal id of the user.
     * @param id Internal id of the user.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the Azure Immutable AD id of the user.
     * @param azureImmutableId Azure Immutable AD id of the user.
     */
    public void setAzureImmutableId(String azureImmutableId) {
        this.azureImmutableId = azureImmutableId;
    }

    /**
     * Create a new instance of {@link User} from existing Azure AD {@link com.microsoft.graph.models.extensions.User}.
     * @param user Existing Azure AD {@link com.microsoft.graph.models.extensions.User} object.
     * @return New instance of {@link User} containing the same attributes as the Azure AD user model.
     */
    public static User fromAzureUser(com.microsoft.graph.models.extensions.User user) {
        User dbUser = new User();
        dbUser.id = user.id;
        dbUser.onPremisesImmutableId = user.onPremisesImmutableId;
        dbUser.givenName = user.givenName;
        dbUser.surname = user.surname;
        dbUser.displayName = user.displayName;
        dbUser.mailNickname = user.mailNickname;
        dbUser.userPrincipalName = user.userPrincipalName;
        dbUser.lastChanged = LocalDateTime.now();

        String hashBase = user.givenName + user.surname + user.displayName + user.mailNickname + user.userPrincipalName;

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            dbUser.hash = Base64.getEncoder().encodeToString(digest.digest(hashBase.getBytes()));
            LOG.trace("Converted {} to hash {}", hashBase, dbUser.hash);
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("An unexpected error occurred", ex);
        }

        return dbUser;
    }

    /**
     * Check if the hash of two user instances is the same.
     * @param user User instance to check against.
     * @return true if the hash of the two user instances are equal, or false if not.
     */
    public boolean isHashEqual(User user) {
        return hash.equals(user.hash);
    }

    /**
     * Get the internal ldap2azure id of the user.
     * @return Internal ldap2azure id of the user.
     */
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    /**
     * Get the immutable id of the user in the source ldap.
     * @return Immutable id of the user in the source ldap.
     */
    @JsonProperty("onPremisesImmutableId")
    public String getOnPremisesImmutableId() {
        return onPremisesImmutableId;
    }

    /**
     * Get the immutable id of the user in Azure AD.
     * @return Immutable id of the user in Azure AD.
     */
    @JsonProperty("azureImmutableId")
    public String getAzureImmutableId() {
        return azureImmutableId;
    }

    /**
     * Get the given name of the user in Azure AD.
     * @return Given name of the user in Azure AD.
     */
    @JsonProperty("givenName")
    public String getGivenName() {
        return givenName;
    }

    /**
     * Get the surname of the user in Azure AD.
     * @return Surname of the user in Azure AD.
     */
    @JsonProperty("surname")
    public String getSurname() {
        return surname;
    }

    /**
     * Get the display name of the user in Azure AD.
     * @return Display name of the user in Azure AD.
     */
    @JsonProperty("displayName")
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the mail nickname of the user in Azure AD.
     * @return Mail nickname of the user in Azure AD.
     */
    @JsonProperty("mailNickname")
    public String getMailNickname() {
        return mailNickname;
    }

    /**
     * Get the user principal name of the user in Azure AD.
     * @return User principal name of the user in Azure AD.
     */
    @JsonProperty("userPrincipalName")
    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    /**
     * Get the MD5 hash of the users details.
     * @return MD5 hash of the users deatils.
     */
    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    /**
     * Get the current sync state of the user.
     * @return Current sync state of the user.
     */
    @JsonProperty("syncState")
    public SyncState getSyncState() {
        return syncState;
    }

    /**
     * Get the current change state of the user.
     * @return Current change state of the user.
     */
    @JsonProperty("changeState")
    public ChangeState getChangeState() {
        return changeState;
    }

    /**
     * Get the {@link LocalDateTime} the user was last changed at.
     * @return {@link LocalDateTime} the user was last changed at.
     */
    @JsonProperty("lastChanged")
    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    /**
     * Get the id of the last sync that made changed to the user.
     * @return Id of the last sync that made changed to the user.
     */
    @JsonProperty("lastSyncId")
    public String getLastSyncId() {
        return lastSyncId;
    }

    /**
     * Set the sync state of the user.
     * @param syncState New {@link SyncState} that should be set for the user.
     */
    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    /**
     * Set the change state of the user.
     * @param changeState New {@link ChangeState} that should be set for the user.
     */
    public void setChangeState(ChangeState changeState) {
        this.changeState = changeState;
    }

    /**
     * Reset the last changed time for the user.
     */
    public void resetLastChanged() {
        this.lastChanged = LocalDateTime.now();
    }

    /**
     * Set the id of the last sync that changed the user object.
     * @param lastSyncId Id of the last sync that changed the user object.
     */
    public void setLastSyncId(String lastSyncId) {
        this.lastSyncId = lastSyncId;
    }

    /**
     * Convert this instance of {@link User} to an instance of {@link com.microsoft.graph.models.extensions.User}
     * @return New instance of an Azure AD {@link com.microsoft.graph.models.extensions.User}
     * containing the same details as this class.
     */
    public com.microsoft.graph.models.extensions.User toAzureUser() {
        com.microsoft.graph.models.extensions.User user = new com.microsoft.graph.models.extensions.User();
        user.id = this.azureImmutableId;
        user.onPremisesImmutableId = this.onPremisesImmutableId;
        user.givenName = this.givenName;
        user.surname = this.surname;
        user.displayName = this.displayName;
        user.mailNickname = this.mailNickname;
        user.userPrincipalName = this.userPrincipalName;
        return user;
    }

}
