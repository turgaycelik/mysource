package com.atlassian.jira.upgrade.tasks;

/**
 * Reindexes JIRA due to a bug in the way {@link com.atlassian.jira.issue.customfields.impl.MultiSelectCFType} fields
 * were being indexed.
 *
 * @since v5.1
 */
public class UpgradeTask_Build751 extends AbstractReindexUpgradeTask
{

    @Override
    public String getBuildNumber()
    {
        return "751";
    }

    @Override
    public String getShortDescription()
    {
        return super.getShortDescription() + " due to changes to way select values are indexed.";
    }
}
