package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.model.object.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to retrieve, create and update {@link Sync} objects in the database.
 *
 * @author Oliver Traber
 */
public class SyncDAOImpl {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(SyncDAOImpl.class.getName());

    /** {@link Dao} that should be used for database operations */
    private Dao<Sync, String> dao;

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public SyncDAOImpl(Dao<Sync, String> dao) {
        this.dao = dao;
    }

    /**
     * Save an {@link Sync} to the database.
     * @param sync {@link Sync} that should be saved to the database.
     */
    public void persist(Sync sync) {
        try {
            dao.create(sync);
            LOG.trace("Persisted sync with id {} to database", sync.getId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Update an {@link Sync} in the database.
     * @param sync {@link Sync} that should be updated.
     */
    public void update(Sync sync) {
        try {
            dao.update(sync);
            LOG.trace("Updated sync with id {} in the database", sync.getId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete an {@link Sync} from the database.
     * @param sync {@link Sync} that should be deleted from the database.
     */
    public void delete(Sync sync) {
        try {
            dao.delete(sync);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Retrieve all {@link Sync} objects that are contained in the database.
     * @return List of {@link Sync} objects that are currently stored in the database.
     */
    public List<Sync> getAll() {
        List<Sync> list = new ArrayList<>();
        try {
            list = dao.queryForAll();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return list;
    }

    /**
     * Retrieve an single {@link Sync} from the local database using any attribute.
     * @return Instance of the found {@link Sync}, or null if no entry could be found.
     */
    public Sync getByAttributeMatch(String attributeName, String attributeValue) {
        QueryBuilder<Sync, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(attributeName, attributeValue);
            List<Sync> results = dao.query(queryBuilder.prepare());
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
     * Get the newest syncs in the database. Limit the number of returned objects.
     * @param amount Maximal amount of {@link Sync} objects to return.
     * @return {@link List} congaing the newest {@link Sync} objects from the database.
     */
    public List<Sync> getRecent(long amount) {
        QueryBuilder<Sync, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.orderBy("startTime", false).limit(amount);
            return dao.query(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Get the amount of all syncs currently persisted in the database.
     * @return Amount of all syncs currently persisted in the database.
     */
    public long getAmount() {
        try {
            return dao.countOf();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return 0;
    }

}
