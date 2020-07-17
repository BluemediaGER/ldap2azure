package de.traber_info.home.ldap2azure.rest.model.response;

import de.traber_info.home.ldap2azure.model.object.Sync;

import java.util.List;

/**
 * Response send when the client is successfully logged in. Contains some details for the dashboard.
 *
 * @author Oliver Traber
 */
public class LoginResponse {

    /** List containing the last five syncs an their details */
    private List<Sync> lastSyncs;

    /** Total number of users present in ldap2azure */
    private long userCount;

    /** Number of users pending for synchronisation */
    private long usersPending;

    /** Number of users that failed to sync */
    private long usersFailed;

    /**
     * Create a new instance of the LoginResponse
     * @param lastSyncs {@link List} containing the last five {@link Sync} objects.
     * @param userCount Total number of users present in ldap2azure.
     * @param usersPending Number of users pending for synchronisation.
     * @param usersFailed Number of users that failed to sync.
     */
    public LoginResponse(List<Sync> lastSyncs, long userCount, long usersPending, long usersFailed) {
        this.lastSyncs = lastSyncs;
        this.userCount = userCount;
        this.usersPending = usersPending;
        this.usersFailed = usersFailed;
    }

    public List<Sync> getLastSyncs() {
        return lastSyncs;
    }

    public long getUserCount() {
        return userCount;
    }

    public long getUsersPending() {
        return usersPending;
    }

    public long getUsersFailed() {
        return usersFailed;
    }

}
