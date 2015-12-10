package com.atlassian.jira.upgrade.tasks;

/**
 * Indexes attachments by name so they are JQL searchable.
 */
public class UpgradeTask_Build6205 extends AbstractReindexUpgradeTask
{
    @Override
    public String getBuildNumber()
    {
        return "6205";
    }

    @Override
    public String getShortDescription()
    {
        return "Indexes if issue has attachments so they are JQL searchable.";
    }

}
