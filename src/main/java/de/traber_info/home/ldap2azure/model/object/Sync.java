package de.traber_info.home.ldap2azure.model.object;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import de.traber_info.home.ldap2azure.h2.persister.LocalDateTimePersister;

import java.time.LocalDateTime;

/**
 * Sync object model that holds all information about a completed sync.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "sync")
public class Sync {

    /** Internal id of the sync */
    @DatabaseField(id = true)
    private String id;

    /** Time the sync began */
    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime syncBegin;

    /** Time the sync completed */
    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime syncEnd;

    /** Amount of users created in this sync */
    @DatabaseField
    private long usersCreated;

    /** Amount of users changed in this sync */
    @DatabaseField
    private long usersChanged;

    /** Amount of users deleted in this sync */
    @DatabaseField
    private long usersDeleted;

    /** Amount of users that failed to sync */
    @DatabaseField
    private long usersFailed;

    /**
     * Default constructor for deserialization.
     */
    private Sync() {}

    /**
     * Public constructor used to create a new user object.
     * @param id Internal id of the sync
     * @param syncBegin Time the sync began
     * @param syncEnd Time the sync completed
     * @param usersCreated Amount of users created in this sync
     * @param usersChanged Amount of users changed in this sync
     * @param usersDeleted Amount of users deleted in this sync
     * @param usersFailed Amount of users that failed to sync
     */
    public Sync(String id, LocalDateTime syncBegin, LocalDateTime syncEnd, long usersCreated,
                long usersChanged, long usersDeleted, long usersFailed) {
        this.id = id;
        this.syncBegin = syncBegin;
        this.syncEnd = syncEnd;
        this.usersCreated = usersCreated;
        this.usersChanged = usersChanged;
        this.usersDeleted = usersDeleted;
        this.usersFailed = usersFailed;
    }

    /**
     * Get the internal id of the sync.
     * @return Internal id of the sync.
     */
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    /**
     * Get the time the sync began.
     * @return Time the sync began.
     */
    @JsonProperty("syncBegin")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    public LocalDateTime getSyncBegin() {
        return syncBegin;
    }

    /**
     * Get the time the sync completed.
     * @return Time the sync completed.
     */
    @JsonProperty("syncEnd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    public LocalDateTime getSyncEnd() {
        return syncEnd;
    }

    /**
     * Get the amount of users created by this sync.
     * @return Amount of users created by this sync.
     */
    @JsonProperty("usersCreated")
    public long getUsersCreated() {
        return usersCreated;
    }

    /**
     * Get the amount of users changed by this sync.
     * @return Amount of users changed by this sync.
     */
    @JsonProperty("usersChanged")
    public long getUsersChanged() {
        return usersChanged;
    }

    /**
     * Get the amount of users deleted by this sync.
     * @return Amount of users deleted by this sync.
     */
    @JsonProperty("usersDeleted")
    public long getUsersDeleted() {
        return usersDeleted;
    }

    /**
     * Get the of users that failed to sync.
     * @return Amount of users that failed to sync.
     */
    @JsonProperty("usersFailed")
    public long getUsersFailed() {
        return usersFailed;
    }
}
