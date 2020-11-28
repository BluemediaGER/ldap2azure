package de.traber_info.home.ldap2azure.quartz;

import de.traber_info.home.ldap2azure.service.AzureSyncService;
import de.traber_info.home.ldap2azure.service.LdapImportService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Quartz job to run an full sync period.
 *
 * @author Oliver Traber
 */
public class SyncJob implements Job {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(SyncJob.class.getName());

    /**
     * Method executed by Quartz to run the job.
     * @param jobExecutionContext Quartz context containing optional preferences.
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        long changedUsers = LdapImportService.run();
        // Run Azure sync if one or more users changed in the source LDAP.
        if (changedUsers > 0) {
            try {
                new AzureSyncService().run();
            } catch (SQLException ex) {
                LOG.error("An unexpected error occurred", ex);
            }
        }
    }

}
