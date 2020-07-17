package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import de.traber_info.home.ldap2azure.rest.model.object.ApiSession;

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

}
