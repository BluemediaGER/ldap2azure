package de.traber_info.home.ldap2azure.rest.model.object;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import de.traber_info.home.ldap2azure.h2.persister.LocalDateTimePersister;
import de.traber_info.home.ldap2azure.util.RandomString;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ApiSession object model that holds all information about an REST api session including the sessions key.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "api_sessions")
public class ApiSession {

    /** Instance of the {@link RandomString} used to generate the session keys */
    private static final RandomString random;

    // Init SecureRandom
    static {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        chars = chars + chars.toLowerCase();
        chars = chars + "0123456789";
        random = new RandomString(32,new SecureRandom(), chars);
    }

    /** Internal id of the session */
    @DatabaseField(id = true)
    private String sessionId;

    /** Key used by the client to authenticate itself */
    @DatabaseField
    private String sessionKey;

    /** Id of the {@link ApiUser} this {@link ApiSession} was created for */
    @DatabaseField
    private String parentApiUserId;

    /** {@link LocalDateTime} the session was last used to make an api call.
     * This is used to let session expire after a period of inactivity.
     */
    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    private LocalDateTime lastAccessTime;

    /** Default constructor for OrmLite */
    private ApiSession() {}

    /**
     * Create an new instance and generate an random session id and an random session key.
     */
    public ApiSession(String parentApiUserId) {
        this.sessionId = UUID.randomUUID().toString();
        this.sessionKey = random.nextString();
        this.lastAccessTime = LocalDateTime.now();
        this.parentApiUserId = parentApiUserId;
    }

    /**
     * Get the sessions id.
     * @return The sessions id.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Get the {@link LocalDateTime} the session was last used to make an api call.
     * @return {@link LocalDateTime} the session was last used to make an api call.
     */
    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Get the session key used by the client to authenticate itself.
     * @return Session key used by the client to authenticate itself.
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Get the id of the parent {@link ApiUser} of the session.
     * @return Id of the parent {@link ApiUser} of the session.
     */
    public String getParentApiUserId() {
        return parentApiUserId;
    }

    /**
     * Reset the {@link LocalDateTime} the session was last used to make an api call to the current time.
     */
    public void resetLasAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

}
