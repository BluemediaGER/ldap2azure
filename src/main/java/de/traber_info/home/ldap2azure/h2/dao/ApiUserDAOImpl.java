package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import de.traber_info.home.ldap2azure.rest.model.object.ApiUser;

/**
 * Class used to retrieve, create and update {@link ApiUser} objects in the database.
 *
 * @author Oliver Traber
 */
public class ApiUserDAOImpl extends GenericDAOImpl<ApiUser> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public ApiUserDAOImpl(Dao<ApiUser, String> dao) {
        super(dao);
    }
}
