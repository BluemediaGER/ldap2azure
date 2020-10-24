package de.traber_info.home.ldap2azure.rest.model.response;

import de.traber_info.home.ldap2azure.model.object.Sync;

import java.util.List;

/**
 * Response send when the client is successfully logged in. Contains some details for the dashboard.
 *
 * @author Oliver Traber
 */
public class DashboardResponse {

    /** List containing the last five syncs an their details */
    private List<Sync> lastSyncs;

    /** Total number of users present in ldap2azure */
    private long userCount;

    /** Total number of users that are in sync */
    private long usersFine;

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
    public DashboardResponse(List<Sync> lastSyncs,
                             long userCount, long usersFine, long usersPending, long usersFailed) {
        this.lastSyncs = lastSyncs;
        this.userCount = userCount;
        this.usersFine = usersFine;
        this.usersPending = usersPending;
        this.usersFailed = usersFailed;
    }

    /**
     * Method to get the last five {@link Sync} objects.
     * @return Last five {@link Sync} objects.
     */
    public List<Sync> getLastSyncs() {
        return lastSyncs;
    }

    /**
     * Get the total number of users present in ldap2azure.
     * @return Total number of users present in ldap2azure.
     */
    public long getUserCount() {
        return userCount;
    }

    /**
     * Get the total number of users that are in sync.
     * @return Total number of users that are in sync.
     */
    public long getUsersFine() {
        return usersFine;
    }

    /**
     * Get the number of users pending for synchronisation.
     * @return number of users pending for synchronisation.
     */
    public long getUsersPending() {
        return usersPending;
    }

    /**
     * Get the number of users that failed to sync.
     * @return Number of users that failed to sync.
     */
    public long getUsersFailed() {
        return usersFailed;
    }

}
