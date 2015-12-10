package com.atlassian.jira.upgrade.tasks;

/**
 * Reindexes JIRA due changes to allow native sorting of many system fields by Lucene.
 *
 * @since v5.1
 */
public class UpgradeTask_Build757 extends AbstractReindexUpgradeTask
{

    @Override
    public String getBuildNumber()
    {
        return "757";
    }

    @Override
    public String getShortDescription()
    {
        return super.getShortDescription() + " due to changes to way some values are indexed.";
    }
}
