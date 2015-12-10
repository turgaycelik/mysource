package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.Select;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * JRA-33781: There's missing record for "issue comment deleted" in database startup xml file.

 * This task provides insert of the missing data. For complete fix we also need to update DB startup xml file.
 *
 * @since v6.3.5
 */
public class UpgradeTask_Build6334 extends AbstractUpgradeTask
{

    private static final Logger log = LoggerFactory.getLogger(UpgradeTask_Build6334.class);

    private final OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build6334(OfBizDelegator ofBizDelegator)
    {
        super(false);
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6334";
    }

    @Override
    public String getShortDescription()
    {
        return "JRA-33781: IllegalArgumentException: No event type with id 17";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            return;
        }

        // check there is no such entity in DB already for this JIRA instance
        if (Select.from(EventType.EVENT_TYPE).byId(17L).runWith(ofBizDelegator).count() > 0)
        {
            log.info(EventType.EVENT_TYPE + " with id 17 already present in DB.");
            return;
        }

        Map<String, Object> values = Maps.newHashMap();
        values.put("id", 17L);
        values.put("name", "Issue Comment Deleted");
        values.put("description", "This is the 'issue comment deleted' event.");
        values.put("type", "jira.system.event.type");

        ofBizDelegator.createValue(EventType.EVENT_TYPE, values);

        log.info("Upgrade task finished");
    }

}
