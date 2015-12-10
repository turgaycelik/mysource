package com.atlassian.jira.upgrade.tasks;

import static java.lang.String.format;

/**
 * Ensures that a reindex is performed to support quoted phrase queries on text fields.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6141 extends AbstractReindexUpgradeTask
{
    @Override
    public String getBuildNumber()
    {
        return "6141";
    }

    @Override
    public String getShortDescription()
    {
        return format
                (
                        "%s This is necessary for the index to support quoted phrase queries on text fields.",
                        super.getShortDescription()
                );
    }
}
