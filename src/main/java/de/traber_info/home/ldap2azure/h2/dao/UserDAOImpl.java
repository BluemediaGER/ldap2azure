package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to retrieve, create and update {@link User} objects in the database.
 *
 * @author Oliver Traber
 */
public class UserDAOImpl {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(UserDAOImpl.class.getName());

    /** {@link Dao} that should be used for database operations */
    private Dao<User, String> dao;

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public UserDAOImpl(Dao<User, String> dao) {
        this.dao = dao;
    }

    /**
     * Save an {@link User} to the database.
     * @param user {@link User} that should be saved to database.
     */
    public void persist(User user) {
        try {
            dao.create(user);
            LOG.trace("Persisted user with hash {} and id {} to local cache", user.getHash(), user.getId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Update an {@link User} in the database.
     * @param user {@link User} that should be updated.
     */
    public void update(User user) {
        try {
            dao.update(user);
            LOG.trace("Updated user with hash {} and id {} in local cache", user.getHash(), user.getId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete an {@link User} from the database.
     * @param user {@link User} that should be deleted from the database.
     */
    public void delete(User user) {
        try {
            dao.delete(user);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Retrieve all {@link User} that are contained in the database.
     * @return List of {@link User} that are currently stored in the database.
     */
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        try {
            list = dao.queryForAll();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return list;
    }

    /**
     * Retrieve an single {@link User} from the database using any attribute.
     * @return Instance of the found {@link User}, or null if no entry could be found.
     */
    public User getByAttributeMatch(String attributeName, String attributeValue) {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(attributeName, attributeValue);
            List<User> results = dao.query(queryBuilder.prepare());
            if (results.size() == 0) {
                return null;
            } else {
                return results.get(0);
            }
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return null;
    }

    /**
     * Get an QueryBuilder instance from the DAO.
     * @return QueryBuilder instance from the DAO.
     */
    public QueryBuilder<User, String> getQueryBuilder() {
        return dao.queryBuilder();
    }

    /**
     * Get a list of results matching the given query.
     * @param queryBuilder Query the found objects must match.
     * @return List of results matching the given query.
     */
    public List<User> query(QueryBuilder<User, String> queryBuilder) {
        try {
            return dao.query(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieve multiple {@link User} instances from the database using any attribute.
     * @return List of the found {@link User} instances, where the given attribute matches.
     */
    public List<User> getAllByAttributeMatch(String attributeName, String attributeValue) {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(attributeName, attributeValue);
            return dao.query(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Get the amount of all users currently persisted in the database.
     * @return Amount of all users currently persisted in the database.
     */
    public long getAmount() {
        try {
            return dao.countOf();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

    /**
     * Get the amount of all users that have the OK sync state.
     * @return Amount of all users that have the OK sync state.
     */
    public long getOkAmount() {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.setCountOf(true).where().eq("syncStatus", SyncState.OK.toValue());
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
            queryBuilder.setCountOf(true).where().eq("syncStatus", SyncState.PENDING.toValue());
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
            queryBuilder.setCountOf(true).where().eq("syncStatus", SyncState.FAILED.toValue());
            return dao.countOf(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

}
