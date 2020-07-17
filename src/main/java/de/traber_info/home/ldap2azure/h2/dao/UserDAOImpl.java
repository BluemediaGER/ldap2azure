package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Class used to retrieve, create and update {@link User} objects in the database.
 *
 * @author Oliver Traber
 */
public class UserDAOImpl extends GenericDAOImpl<User> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(UserDAOImpl.class.getName());

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public UserDAOImpl(Dao<User, String> dao) {
        super(dao);
    }

    /**
     * Get the amount of all users that have the OK sync state.
     * @return Amount of all users that have the OK sync state.
     */
    public long getOkAmount() {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.setCountOf(true).where().eq("syncState", SyncState.OK.toValue());
            return dao.countOf(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

    /**
     * Get the amount of all users that have the PENDING sync state.
     * @return Amount of all users that have the PENDING sync state.
     */
    public long getPendingAmount() {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.setCountOf(true).where().eq("syncState", SyncState.PENDING.toValue());
            return dao.countOf(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

    /**
     * Get the amount of all users that have the FAILED sync state.
     * @return Amount of all users that have the FAILED sync state.
     */
    public long getFailedAmount() {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.setCountOf(true).where().eq("syncState", SyncState.FAILED.toValue());
            return dao.countOf(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

}
