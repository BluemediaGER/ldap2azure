package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.model.object.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to retrieve, create and update {@link Sync} objects in the database.
 *
 * @author Oliver Traber
 */
public class SyncDAOImpl extends GenericDAOImpl<Sync> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(SyncDAOImpl.class.getName());

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public SyncDAOImpl(Dao<Sync, String> dao) {
        super(dao);
    }

    /**
     * Get the newest syncs in the database. Limit the number of returned objects.
     * @param amount Maximal amount of {@link Sync} objects to return.
     * @return {@link List} congaing the newest {@link Sync} objects from the database.
     */
    public List<Sync> getRecent(long amount) {
        QueryBuilder<Sync, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.orderBy("syncBegin", false).limit(amount);
            return dao.query(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Cleanup {@link Sync} objects from the database if they are older than 7 days.
     * @throws SQLException Exception if an error occurred.
     */
    public void cleanup() throws SQLException {
        DeleteBuilder<Sync, String> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().lt("syncEnd", LocalDateTime.now().minusDays(7));
        deleteBuilder.delete();
    }

}
