package de.traber_info.home.ldap2azure.h2.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic DAO implementation to provide base functionality to other implemented classes.
 *
 * @author Oliver Traber
 */
public class GenericDAOImpl<T> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(GenericDAOImpl.class.getName());

    /** {@link Dao} that should be used for database operations */
    protected Dao<T, String> dao;

    /**
     * Default constructor to instantiate this class.
     * @param dao {@link Dao} that should be used for database operations.
     */
    public GenericDAOImpl(Dao<T, String> dao) {
        this.dao = dao;
    }

    /**
     * Save an object to the database.
     * @param object Object that should be saved to the database.
     */
    public void persist(T object) {
        try {
            dao.create(object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Update an object in the database.
     * @param object Object that should be updated.
     */
    public void update(T object) {
        try {
            dao.update(object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Delete an object from the database.
     * @param object Object that should be deleted from the database.
     */
    public void delete(T object) {
        try {
            dao.delete(object);
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Retrieve all objects that are contained in the database.
     * @return List of objects that are currently stored in the database.
     */
    public List<T> getAll() {
        List<T> list = new ArrayList<>();
        try {
            list = dao.queryForAll();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return list;
    }

    /**
     * Retrieve an single object from the local database using any attribute.
     * @return Instance of the found, or null if no entry could be found.
     */
    public T getByAttributeMatch(String attributeName, String attributeValue) {
        QueryBuilder<T, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(attributeName, attributeValue);
            List<T> results = dao.query(queryBuilder.prepare());
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
     * Retrieve all matching objects from the local database using any attribute.
     * @return Instance of the found, or null if no entry could be found.
     */
    public List<T> getAllByAttributeMatch(String attributeName, String attributeValue) {
        QueryBuilder<T, String> queryBuilder = dao.queryBuilder();
        try {
            queryBuilder.where().eq(attributeName, attributeValue);
            List<T> results = dao.query(queryBuilder.prepare());
            if (results.size() == 0) {
                return null;
            } else {
                return results;
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
    public QueryBuilder<T, String> getQueryBuilder() {
        return dao.queryBuilder();
    }

    /**
     * Get a list of results matching the given query.
     * @param queryBuilder Query the found objects must match.
     * @return List of results matching the given query.
     */
    public List<T> query(QueryBuilder<T, String> queryBuilder) {
        try {
            return dao.query(queryBuilder.prepare());
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
        return new ArrayList<>();
    }

    /**
     * Get the amount of all objects currently persisted in the database.
     * @return Amount of all objects currently persisted in the database.
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
