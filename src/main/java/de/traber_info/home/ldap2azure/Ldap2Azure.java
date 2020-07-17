package de.traber_info.home.ldap2azure;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.model.config.GraphClientConfig;
import de.traber_info.home.ldap2azure.model.config.WebConfig;
import de.traber_info.home.ldap2azure.quartz.SyncJob;
import de.traber_info.home.ldap2azure.rest.RestApplication;
import de.traber_info.home.ldap2azure.service.AzureSyncService;
import de.traber_info.home.ldap2azure.service.LdapImportService;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import de.traber_info.home.ldap2azure.util.GraphClientUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.SQLException;

/**
 * Main class for ldap2azure. Performs the first initialisation of all components.
 *
 * @author Oliver Traber
 */
public class Ldap2Azure {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(Ldap2Azure.class.getName());

    /** Global Quartz scheduler used to schedule sync tasks */
    private static Scheduler quartzScheduler;

    /** Embedded Jetty application server */
    private static Server jetty;

    /**
     * Main function of ldap2azure initializes everything that is needed to run ldap2azure
     * @param args Arguments passed in by the commandline
     */
    public static void main(String[] args) {
        // Add shutdown hook to cleanly shutdown the program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Performing clean shutdown");
            H2Helper.close();
            try {
                jetty.stop();
            } catch (Exception ex) {
                LOG.error("An unexpected error occurred", ex);
            }
            if (quartzScheduler != null) {
                try {
                    quartzScheduler.shutdown();
                } catch (SchedulerException ex) {
                    LOG.error("An unexpected error occurred", ex);
                }
            }
        }));

        // Initialize H2 database
        H2Helper.init(ConfigUtil.getConfig().getGeneralConfig().isDebuggingEnabled());

        // Initialize GraphClientUtil
        GraphClientConfig graphClientConfig = ConfigUtil.getConfig().getGraphClientConfig();
        GraphClientUtil.init(graphClientConfig.getTenantSpecificAuthority(), graphClientConfig.getClientId(),
                graphClientConfig.getClientSecret());

        // Initialize the http management server when enabled in the config file
        if (ConfigUtil.getConfig().getWebConfig().isEnabled()) initWebServer();

        LOG.info("Running initial sync...");

        // Import from source ldap
        LdapImportService.run();

        // Run sync with Azure AD
        try {
            new AzureSyncService().run();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }

        LOG.info("Initial sync done.");

        try {
            // Prepare default Quartz scheduler
            quartzScheduler = StdSchedulerFactory.getDefaultScheduler();

            // Prepare sync job
            JobDetail syncJob = JobBuilder.newJob(SyncJob.class)
                    .withIdentity("syncJob1")
                    .build();

            // Prepare cron trigger with cron expression from config
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("syncTrigger1")
                    .startNow()
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule(
                                    ConfigUtil.getConfig().getGeneralConfig().getCronExpression()))
                    .build();

            // Schedule job and start scheduler
            quartzScheduler.scheduleJob(syncJob, trigger);
            quartzScheduler.start();
            LOG.info("Scheduled sync interval with cron expression {}",
                    ConfigUtil.getConfig().getGeneralConfig().getCronExpression());
        } catch (SchedulerException ex) {
            LOG.error("An unexpected error occurred", ex);
        }

    }

    /**
     * Initialize the http management server.
     */
    private static void initWebServer() {
        // Initialize rest api and web-based management interface if enabled in the config file
        WebConfig config = ConfigUtil.getConfig().getWebConfig();
        // Create embedded Jetty server
        jetty = new Server(config.getPort());

        // Create servlet context handler for handling the rest api
        ServletContextHandler srvCtxHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(new RestApplication()));
        srvCtxHandler.addServlet(jerseyServlet, "/api/*");

        // Get base path for static files
        String baseStr = "/webapp"; // Resource folder "webapp"
        URL baseUrl = Ldap2Azure.class.getResource(baseStr);
        String basePath = baseUrl.toExternalForm();

        // Create context handler and resource handler to serve static content a.k.a. web-based management interface
        ContextHandler ctxHandler = new ContextHandler("/*");
        ResourceHandler resHandler = new ResourceHandler();
        resHandler.setResourceBase(basePath);
        resHandler.setDirectoriesListed(false);
        resHandler.setWelcomeFiles(new String[]{ "index.html" });
        ctxHandler.setHandler(resHandler);

        // Combine handlers to handler list and add the list to the Jetty server
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{srvCtxHandler, ctxHandler});
        jetty.setHandler(handlerList);

        // Start the server thread
        try {
            jetty.start();
            LOG.info("[Http-Management] Management interface and rest api successfully started on port {}",
                    config.getPort());
        } catch (Exception ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

}
