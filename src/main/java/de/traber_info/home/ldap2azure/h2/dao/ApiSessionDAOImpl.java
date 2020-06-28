package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.rest.model.object.ApiSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to retrieve, create and update {@link ApiSession} objects in the database.
 *
 * @author Oliver Traber
 */
public class ApiSessionDAOImpl {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(ApiSessionDAOImpl.class.getName());

    /** {@link Dao} that should be used for database operations */
    private Dao<ApiSession, String> dao;

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public ApiSessionDAOImpl(Dao<ApiSession, String> dao) {
        this.dao = dao;
    }

    /**
     * Save an {@link ApiSession} to the local database database.
     * @param session {@link ApiSession} that should be saved to database.
     */
    public void persist(ApiSession session) {
        try {
            dao.create(session);
            LOG.trace("Persisted session with id {} to local database", session.getSessionId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Save an list if {@link ApiSession} to the local database database.
     * @param sessions {@link ApiSession} list that should be saved to database.
     */
    public void persist(List<ApiSession> sessions) {
        try {
            dao.create(sessions);
            LOG.trace("Persisted {} sessions to local database", sessions.size());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Update an existing {@link ApiSession} in the local database .
     * @param session {@link ApiSession} that should be updated.
     */
    public void update(ApiSession session) {
        try {
            dao.update(session);
            LOG.trace("Updated session with id {} in local database", session.getSessionId());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete an {@link ApiSession} from the local database database.
     * @param session {@link ApiSession} that should be deleted from the database.
     */
    public void delete(ApiSession session) {
        try {
            dao.delete(session);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Retrieve all {@link ApiSession} that are contained in the local database.
     * @return List of {@link ApiSession} that are currently stored in the database.
     */
    public List<ApiSession> getAll() {
        List<ApiSession> list = new ArrayList<>();
        try {
            list = dao.queryForAll();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return list;
    }

    /**
     * Retrieve an single {@link ApiSession} from the local database using any attribute.
     * @return Instance of the found {@link ApiSession}, or null if no entry could be found.
     */
    public ApiSession getByAttributeMatch(String attributeName, String attributeValue) {
        QueryBuilder<ApiSession, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(attributeName, attributeValue);
            List<ApiSession> results = dao.query(queryBuilder.prepare());
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

}
