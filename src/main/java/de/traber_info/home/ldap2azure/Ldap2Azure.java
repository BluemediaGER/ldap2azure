package de.traber_info.home.ldap2azure;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.model.config.GraphClientConfig;
import de.traber_info.home.ldap2azure.msgraph.GraphClientUtil;
import de.traber_info.home.ldap2azure.quartz.CleanupJob;
import de.traber_info.home.ldap2azure.quartz.SyncJob;
import de.traber_info.home.ldap2azure.rest.server.HttpServer;
import de.traber_info.home.ldap2azure.service.AzureSyncService;
import de.traber_info.home.ldap2azure.service.LdapImportService;
import de.traber_info.home.ldap2azure.util.ConfigUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Main function of ldap2azure initializes everything that is needed to run ldap2azure
     * @param args Arguments passed in by the commandline
     */
    public static void main(String[] args) {
        // Add shutdown hook to cleanly shutdown the program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Performing clean shutdown");
            H2Helper.close();
            HttpServer.stop();
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
        if (ConfigUtil.getConfig().getWebConfig().isEnabled()) HttpServer.start();

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
                    .withIdentity("syncJob")
                    .build();

            // Prepare cron trigger with cron expression from config
            Trigger syncTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("syncTrigger")
                    .startNow()
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule(
                                    ConfigUtil.getConfig().getGeneralConfig().getCronExpression()))
                    .build();

            // Prepare cleanup job
            JobDetail cleanupJob = JobBuilder.newJob(CleanupJob.class)
                    .withIdentity("cleanupJob")
                    .build();

            // Prepare trigger that triggers every 10 minutes forever
            Trigger cleanupTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("cleanupTrigger")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(10))
                    .build();

            // Schedule jobs and start scheduler
            quartzScheduler.scheduleJob(syncJob, syncTrigger);
            quartzScheduler.scheduleJob(cleanupJob, cleanupTrigger);
            quartzScheduler.start();
            LOG.info("Scheduled sync interval with cron expression {}",
                    ConfigUtil.getConfig().getGeneralConfig().getCronExpression());
        } catch (SchedulerException ex) {
            LOG.error("An unexpected error occurred", ex);
        }

    }

}
