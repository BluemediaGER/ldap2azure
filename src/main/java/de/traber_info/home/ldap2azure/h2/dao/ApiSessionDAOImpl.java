package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import de.traber_info.home.ldap2azure.rest.model.object.ApiSession;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class used to retrieve, create and update {@link ApiSession} objects in the database.
 *
 * @author Oliver Traber
 */
public class ApiSessionDAOImpl extends GenericDAOImpl<ApiSession> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public ApiSessionDAOImpl(Dao<ApiSession, String> dao) {
        super(dao);
    }

    /**
     * Cleanup expired {@link ApiSession} from the database.
     * @throws SQLException Exception if an error occurred.
     */
    public void cleanup() throws SQLException {
        DeleteBuilder<ApiSession, String> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().lt("lastAccessTime", LocalDateTime.now().minusMinutes(20));
        deleteBuilder.delete();
    }

}
