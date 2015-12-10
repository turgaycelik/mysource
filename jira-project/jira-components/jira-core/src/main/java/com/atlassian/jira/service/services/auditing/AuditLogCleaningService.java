package com.atlassian.jira.service.services.auditing;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.auditing.AuditingRetentionPeriod;
import com.atlassian.jira.auditing.AuditingStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.service.AbstractService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Service from periodical cleaning of Audit lig
 *
 * @since v6.3
 */
public class AuditLogCleaningService extends AbstractService
{
    private static final Logger log = Logger.getLogger(AuditLogCleaningService.class);
    private final AuditingStore auditingStore;
    private final ApplicationProperties applicationProperties;

    public AuditLogCleaningService(AuditingStore auditingStore, ApplicationProperties applicationProperties)
    {
        this.auditingStore = auditingStore;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void run()
    {
        final AuditingRetentionPeriod retentionPeriod = configuredRetentionPeriod();
        if (retentionPeriod != null && !retentionPeriod.isUnlimited())
        {
            final DateTime monthsIntoPast = retentionPeriod.monthsIntoPast();
            if (log.isDebugEnabled())
            {
                log.debug("Removing log entries older than " + monthsIntoPast);
            }
            final long entriesRemoved = auditingStore.removeRecordsOlderThan(monthsIntoPast.getMillis());
            if (log.isDebugEnabled())
            {
                log.debug("Removed " + entriesRemoved + " entries");
            }
        }
        else
        {
            log.debug("Log entries are kept indefinitely");
        }
    }

    private AuditingRetentionPeriod configuredRetentionPeriod()
    {
        return AuditingRetentionPeriod.getByValue(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_AUDITING_LOG_RETENTION_PERIOD_IN_MONTHS));
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("AUDITLOGCLEANINGSERVICE", "services/com/atlassian/jira/service/services/auditing/auditlogcleaningservice.xml", null);
    }
}
