package de.traber_info.home.ldap2azure.h2;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class H2Provider {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(H2Provider.class.getName());

    /** Connection source for caching database */
    private static ConnectionSource cacheConnectionSource;

    /** Connection source for persistent database */
    private static ConnectionSource persistentConnectionSource;

    /**
     * Initialize the H2 database connections, tables and DAOs and start the debugging console if needed
     * @param enableDebuggingConsole Set true to enable H2's web based console on TCP port 8082
     */
    public static void init(boolean enableDebuggingConsole) {
        try {
            cacheConnectionSource = new JdbcConnectionSource("jdbc:h2:mem:cache");
            persistentConnectionSource = new JdbcConnectionSource("jdbc:h2:" + ConfigUtil.getJarPath() + "/ldap2azure.db");

            if (enableDebuggingConsole) {
                LOG.warn("Debugging mode is active. This will open an unsecured H2 Console on port 8082 of your host machine and is not recommended in an production environment.");
                Server.createWebServer("-web", "-webAllowOthers", "-webPort" , "8082").start();
            }
        } catch (SQLException | URISyntaxException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

    /**
     * Close the H2 database connections
     */
    public static void close() {
        try {
            if (cacheConnectionSource != null) {
                cacheConnectionSource.close();
            }
            if (persistentConnectionSource != null) {
                persistentConnectionSource.close();
            }
        } catch (IOException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

}
