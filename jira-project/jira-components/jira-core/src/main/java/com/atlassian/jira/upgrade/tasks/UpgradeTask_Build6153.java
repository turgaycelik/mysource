package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.Visitor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Corrects wording of Cloners link if legacy direction was used and disables support for legacy direction of links
 */
public class UpgradeTask_Build6153 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6153.class);

    private final ApplicationProperties applicationProperties;
    private final TransactionSupport transactionSupport;
    private final EntityEngine entityEngine;

    public UpgradeTask_Build6153(ApplicationProperties applicationProperties,
            TransactionSupport transactionSupport,
            EntityEngine entityEngine)
    {
        super(false);
        this.applicationProperties = applicationProperties;
        this.transactionSupport = transactionSupport;
        this.entityEngine = entityEngine;
    }

    public String getBuildNumber()
    {
        return "6153";
    }

    public String getShortDescription()
    {
        return "Corrects wording of Cloners link if legacy direction was used and disables support for legacy direction of links";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final boolean cloneLinkLegacyDirection = applicationProperties.getOption(UpgradeTask_Build849.JIRA_CLONE_LINK_LEGACY_DIRECTION);
        if (cloneLinkLegacyDirection)
        {
            swapDirectionNamesAndDisableFlag();
        }
    }

    private void swapDirectionNamesAndDisableFlag() throws GenericEntityException
    {
        log.info("Cloners link was using legacy directions. Swapping direction names.");

        final Transaction transaction = transactionSupport.begin();
        try
        {
            swapDirectionNamesForClonersLink();
            disableCloneLinkLegacyDirectionFlag();

            transaction.commit();
        }
        finally
        {
            transaction.finallyRollbackIfNotCommitted();
        }
    }

    private void swapDirectionNamesForClonersLink()
    {
        entityEngine.run(Select.from("IssueLinkType")
                .whereEqual("linkname", "Cloners")).visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue link)
            {
                try
                {
                    String tmp = link.getString("outward");
                    link.set("outward", link.getString("inward"));
                    link.set("inward", tmp);
                    link.store();
                }
                catch (GenericEntityException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void disableCloneLinkLegacyDirectionFlag() throws GenericEntityException
    {
        //this property exists since we just got true form it
        final GenericValue flag = entityEngine.run(Select.from("OSPropertyEntry")
                .whereEqual("propertyKey", UpgradeTask_Build849.JIRA_CLONE_LINK_LEGACY_DIRECTION)).singleValue();
        final GenericValue value = entityEngine.run(Select.from("OSPropertyNumber")
                .whereEqual("id", flag.getLong("id"))).singleValue();
        value.set("value", 0);
        value.store();
    }
}