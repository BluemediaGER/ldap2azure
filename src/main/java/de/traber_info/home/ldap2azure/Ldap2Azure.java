package de.traber_info.home.ldap2azure;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for ldap2azure. Performs the first initialisation of all components.
 *
 * @author Oliver Traber
 */
public class Ldap2Azure {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(Ldap2Azure.class.getName());

    /**
     * Main function of ldap2azure initializes everything that is needed to run ldap2azure
     * @param args Arguments passed in by the commandline
     */
    public static void main(String[] args) {

        // Add shutdown hook to cleanly shutdown the program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Performing clean shutdown");
            H2Helper.close();
        }));

        // Initialize H2 databases
        H2Helper.init(ConfigUtil.getConfig().getGeneralConfig().isDebuggingEnabled());

    }

}
