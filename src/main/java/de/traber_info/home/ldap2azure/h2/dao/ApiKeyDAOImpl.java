package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;

public class ApiKeyDAOImpl extends GenericDAOImpl<ApiKey> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public ApiKeyDAOImpl(Dao<ApiKey, String> dao) {
        super(dao);
    }

}
