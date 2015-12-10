package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.status.category.StatusCategoryImpl;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;


/**
 * Fills Issue Status category column
 *
 * @since v6.1
 */
public class UpgradeTask_Build6139 extends AbstractUpgradeTask
{

    public static final String ENTITY_STATUS = "Status";

    public UpgradeTask_Build6139()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6139";
    }

    @Override
    public String getShortDescription()
    {
        return "Fills Issue Status category column with its default value";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        StatusCategory defaultStatusCategory = StatusCategoryImpl.getDefault();
        Update.into(ENTITY_STATUS).set("statuscategory", defaultStatusCategory.getId()).all().execute(getOfBizDelegator());
    }
}
