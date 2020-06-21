package de.traber_info.home.ldap2azure.h2;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import de.traber_info.home.ldap2azure.h2.dao.SyncDAOImpl;
import de.traber_info.home.ldap2azure.h2.dao.UserDAOImpl;
import de.traber_info.home.ldap2azure.model.object.Sync;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Class for access to and management of the H2 databases
 *
 * @author Oliver Traber
 */
public class H2Helper {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(H2Helper.class.getName());

    /** Connection source for the database */
    private static ConnectionSource connectionSource;

    /** {@link UserDAOImpl} used to persist {@link User} objects to the database */
    private static UserDAOImpl userDao;

    /** {@link SyncDAOImpl} used to persist {@link Sync} objects to the database */
    private static SyncDAOImpl syncDao;

    /**
     * Initialize the H2 database connections, tables and DAOs and start the debugging console if needed
     * @param enableDebuggingConsole Set true to enable H2's web based console on TCP port 8082
     */
    public static void init(boolean enableDebuggingConsole) {
        try {
            connectionSource = new JdbcConnectionSource("jdbc:h2:" + ConfigUtil.getJarPath() + "/ldap2azure");

            userDao = new UserDAOImpl(DaoManager.createDao(connectionSource, User.class));
            TableUtils.createTableIfNotExists(connectionSource, User.class);

            syncDao = new SyncDAOImpl(DaoManager.createDao(connectionSource, Sync.class));
            TableUtils.createTableIfNotExists(connectionSource, Sync.class);

            if (enableDebuggingConsole) {
                LOG.warn("Debugging mode is active. This will open an unsecured H2 Console on port 8082 of your host machine and is not recommended in an production environment.");
                LOG.info("DEBUG - FileDB - {}", "jdbc:h2:" + ConfigUtil.getJarPath() + "/ldap2azure");
                Server.createWebServer("-web", "-webAllowOthers", "-webPort" , "8082").start();
            }
        } catch (SQLException | URISyntaxException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /** Close the H2 database connections */
    public static void close() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
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
}
