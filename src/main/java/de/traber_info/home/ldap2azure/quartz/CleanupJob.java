package de.traber_info.home.ldap2azure.quartz;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Quartz job that performs regular cleanup activities on the database.
 */
public class CleanupJob implements Job {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(CleanupJob.class.getName());

    /**
     * Method executed by Quartz to run the job.
     * @param jobExecutionContext Quartz context containing optional preferences.
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            H2Helper.getApiSessionDao().cleanup();
            H2Helper.getSyncDao().cleanup();
        } catch (SQLException ex) {
            LOG.error("An unexpected error occurred", ex);
        }
    }

}
