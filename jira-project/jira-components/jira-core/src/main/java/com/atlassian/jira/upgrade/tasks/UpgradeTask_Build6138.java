package com.atlassian.jira.upgrade.tasks;

import static java.lang.String.format;

/**
 * Ensures that a reindex is performed to support wildcard searches on stemmed fields.
 *
 * @since v6.1
 */
public class UpgradeTask_Build6138 extends AbstractReindexUpgradeTask
{
    @Override
    public String getBuildNumber()
    {
        return "6138";
    }

    @Override
    public String getShortDescription()
    {
        return format
                (
                        "%s This is necessary for the index to support wildcard searches on stemmed fields.",
                        super.getShortDescription()
                );
    }
}
