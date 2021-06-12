package de.traber_info.home.ldap2azure.h2;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import de.traber_info.home.ldap2azure.h2.dao.*;
import de.traber_info.home.ldap2azure.model.object.Sync;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;
import de.traber_info.home.ldap2azure.rest.model.object.ApiSession;
import de.traber_info.home.ldap2azure.rest.model.object.ApiUser;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Class for access to and management of the H2 databases
 *
 * @author Oliver Traber
 */
public class H2Helper {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(H2Helper.class.getName());

    /** Connection source for the persistent database */
    private static JdbcPooledConnectionSource persistentConnectionSource;

    /** Connection source for the in memory database */
    private static ConnectionSource inMemoryConnectionSource;

    /** {@link UserDAOImpl} used to persist {@link User} objects to the database */
    private static UserDAOImpl userDao;

    /** {@link SyncDAOImpl} used to persist {@link Sync} objects to the database */
    private static SyncDAOImpl syncDao;

    /** {@link ApiSessionDAOImpl} used to persist {@link ApiSession} objects to the database */
    private static ApiSessionDAOImpl apiSessionDao;

    /** {@link ApiKeyDAOImpl} used to persist {@link ApiKey} objects to the database */
    private static ApiKeyDAOImpl apiKeyDao;

    /** {@link ApiUserDAOImpl} used to persist {@link ApiUser} objects to the database */
    private static ApiUserDAOImpl apiUserDao;

    /**
     * Initialize the H2 database connections, tables and DAOs and start the debugging console if needed
     * @param enableDebuggingConsole Set true to enable H2's web based console on TCP port 8082
     */
    public static void init(boolean enableDebuggingConsole) {
        try {
            String persistenceJDBCUrl = ConfigUtil.getConfig().getGeneralConfig().getDatabaseJDBCUrl();
            persistentConnectionSource = new JdbcPooledConnectionSource(persistenceJDBCUrl);
            persistentConnectionSource.setMaxConnectionAgeMillis(5 * 60 * 1000);
            persistentConnectionSource.setTestBeforeGet(true);

            inMemoryConnectionSource = new JdbcConnectionSource("jdbc:h2:mem:cache");

            userDao = new UserDAOImpl(DaoManager.createDao(persistentConnectionSource, User.class));
            TableUtils.createTableIfNotExists(persistentConnectionSource, User.class);

            syncDao = new SyncDAOImpl(DaoManager.createDao(persistentConnectionSource, Sync.class));
            TableUtils.createTableIfNotExists(persistentConnectionSource, Sync.class);

            apiSessionDao = new ApiSessionDAOImpl(DaoManager.createDao(inMemoryConnectionSource, ApiSession.class));
            TableUtils.createTableIfNotExists(inMemoryConnectionSource, ApiSession.class);

            apiKeyDao = new ApiKeyDAOImpl(DaoManager.createDao(persistentConnectionSource, ApiKey.class));
            TableUtils.createTableIfNotExists(persistentConnectionSource, ApiKey.class);

            apiUserDao = new ApiUserDAOImpl(DaoManager.createDao(persistentConnectionSource, ApiUser.class));
            TableUtils.createTableIfNotExists(persistentConnectionSource, ApiUser.class);

            if (enableDebuggingConsole) {
                LOG.warn("Debugging mode is active. This will open an unsecured H2 Console on port 8082 of your host machine and is not recommended in an production environment.");
                LOG.info("DEBUG - PersistentDB - {}", persistenceJDBCUrl);
                LOG.info("DEBUG - RamDB - jdbc:h2:mem:cache");
                Server.createWebServer("-web", "-webAllowOthers", "-webPort" , "8082").start();
            }
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /** Close the H2 database connections */
    public static void close() {
        try {
            if (persistentConnectionSource != null) {
                persistentConnectionSource.close();
            }
            if (inMemoryConnectionSource != null) {
                inMemoryConnectionSource.close();
            }
        } catch (IOException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Get the {@link UserDAOImpl} used to persist {@link User} objects to the database.
     * @return {@link UserDAOImpl} used to persist {@link User} objects to the database.
     */
    public static UserDAOImpl getUserDao() {
        return userDao;
    }

    /**
     * Get the {@link SyncDAOImpl} used to persist {@link Sync} objects to the database.
     * @return {@link SyncDAOImpl} used to persist {@link Sync} objects to the database.
     */
    public static SyncDAOImpl getSyncDao() {
        return syncDao;
    }

    /**
     * Get the {@link ApiSessionDAOImpl} used to persist {@link ApiSession} objects to the database.
     * @return {@link ApiSessionDAOImpl} used to persist {@link ApiSession} objects to the database.
     */
    public static ApiSessionDAOImpl getApiSessionDao() {
        return apiSessionDao;
    }

    /**
     * Get the {@link ApiKeyDAOImpl} used to persist {@link ApiKey} objects to the database.
     * @return {@link ApiKeyDAOImpl} used to persist {@link ApiKey} objects to the database.
     */
    public static ApiKeyDAOImpl getApiKeyDao() {
        return apiKeyDao;
    }

    /**
     * Get the {@link ApiUserDAOImpl} used to persist {@link ApiUser} objects to the database.
     * @return {@link ApiUserDAOImpl} used to persist {@link ApiUser} objects to the database.
     */
    public static ApiUserDAOImpl getApiUserDao() {
        return apiUserDao;
    }

}
