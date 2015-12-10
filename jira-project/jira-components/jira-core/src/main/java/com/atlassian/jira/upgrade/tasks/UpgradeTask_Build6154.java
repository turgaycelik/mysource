package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericValue;

/**
 * Removing JIRA property jira.clone.link.legacy.direction
 */
public class UpgradeTask_Build6154 extends AbstractUpgradeTask
{
    private final TransactionSupport transactionSupport;
    private final OfBizDelegator ofBizDelegator;
    private final EntityEngine entityEngine;
    private final ApplicationProperties applicationProperties;

    public UpgradeTask_Build6154(TransactionSupport transactionSupport, OfBizDelegator ofBizDelegator,
            EntityEngine entityEngine, ApplicationProperties applicationProperties)
    {
        super(false);
        this.transactionSupport = transactionSupport;
        this.ofBizDelegator = ofBizDelegator;
        this.entityEngine = entityEngine;
        this.applicationProperties = applicationProperties;
    }

    public String getBuildNumber()
    {
        return "6154";
    }

    public String getShortDescription()
    {
        return "Removing JIRA property jira.clone.link.legacy.direction";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Transaction transaction = transactionSupport.begin();

        try
        {
            final GenericValue flag = entityEngine.run(Select.from("OSPropertyEntry")
                    .whereEqual("propertyKey", UpgradeTask_Build849.JIRA_CLONE_LINK_LEGACY_DIRECTION)).singleValue();
            if (flag != null)
            {
                ofBizDelegator.removeValue(flag);
                final GenericValue value = entityEngine.run(Select.from("OSPropertyNumber")
                        .whereEqual("id", flag.getLong("id"))).singleValue();
                if (value != null)
                {
                    ofBizDelegator.removeValue(value);
                }
            }

            transaction.commit();
            applicationProperties.refresh();
        }
        finally
        {
            transaction.finallyRollbackIfNotCommitted();
        }

    }
}