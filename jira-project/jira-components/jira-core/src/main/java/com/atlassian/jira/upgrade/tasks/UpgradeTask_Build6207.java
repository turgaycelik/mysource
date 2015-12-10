package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

/**
 * Upgrade task to set the base sequence number for issue constants.
 *
 * This upgrade task was moved from build number 6156 in order to make it run in OnDemand.
 *
 * @since v6.1.2, v6.2-OD4
 */
public class UpgradeTask_Build6207 extends AbstractUpgradeTask
{
    protected static final String[] ISSUE_CONSTANT_ENTITIES = {"IssueType", "Resolution", "Priority", "Status"};

    private static final Logger logger = Logger.getLogger(UpgradeTask_Build6207.class);

    private final OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build6207(OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6207";
    }

    @Override
    public String getShortDescription()
    {
        return "Set base sequence number for issue constants.";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        for (final String entityName : ISSUE_CONSTANT_ENTITIES)
        {
            logger.info("Updating sequence id for issue constant " + entityName);
            Long initialId = getInitialId(entityName);
            logger.debug("Setting sequence id for issue constant " + entityName + " to initial value " + initialId);
            setNextId(entityName, initialId);
        }
    }

    private Long getInitialId(final String entityName)
    {
        // We start at 10000 to make IssueConstant id's more JIRA-like.
        long maxID = 10000;
        for (final GenericValue entity : ofBizDelegator.findAll(entityName))
        {
            try
            {
                final long entityId = Long.parseLong(entity.getString("id"));
                if (entityId >= maxID)
                {
                    maxID = entityId + 1;
                }
            }
            catch (final NumberFormatException nfe)
            {
                // ignore - we don't care about String constant IDs
            }
        }
        return maxID;
    }

    /**
     * Set the next SequenceValueItem id for the given entity name.
     *
     * @param entityName
     * @param nextId
     * @throws org.ofbiz.core.entity.GenericEntityException
     */
    private void setNextId(String entityName, Long nextId)
    {
        // First ensure we have an entry in SequenceValueItem table
        ofBizDelegator.getDelegatorInterface().getNextSeqId(entityName);
        // Now set it to nextId
        GenericValue sequenceItem = EntityUtil.getOnly(ofBizDelegator.findByAnd("SequenceValueItem", ImmutableMap.of("seqName", entityName)));
        sequenceItem.set("seqId", nextId);

        ofBizDelegator.store(sequenceItem);
        ofBizDelegator.refreshSequencer();
    }
}
