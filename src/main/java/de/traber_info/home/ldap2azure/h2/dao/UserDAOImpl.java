package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.model.object.User;
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
     * Save an {@link User} to the local in-memory cache database.
     * @param user {@link User} that should be saved to cache.
     */
    public void persist(User user) {
        try {
            dao.create(user);
            LOG.trace("Persisted user with hash {} and id {} to local cache", user.getHash(), user.getId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    public void update(User user) {
        try {
            dao.update(user);
            LOG.trace("Updated user with hash {} and id {} in local cache", user.getHash(), user.getId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete an {@link User} from the local in-memory cache database.
     * @param user {@link User} that should be deleted from the cache.
     */
    public void delete(User user) {
        try {
            dao.delete(user);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Retrieve all {@link User} that are contained in the local in-memory cache.
     * @return List of {@link User} that are currently stored in the cache.
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
     * Retrieve an single {@link User} from the local in-memory cache using any attribute.
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
     * Get the amount of all users that have the WARN sync state.
     * @return Amount of all users that have the WARN sync state.
     */
    public long getWarnAmount() {
        QueryBuilder<User, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.setCountOf(true).where().eq("syncStatus", "WARN");
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
            queryBuilder.setCountOf(true).where().eq("syncStatus", "FAILED");
            return dao.countOf(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

}
