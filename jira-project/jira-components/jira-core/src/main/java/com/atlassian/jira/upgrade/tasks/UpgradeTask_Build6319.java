package com.atlassian.jira.upgrade.tasks;

import static java.lang.String.format;

/**
 * Triggers a reindex necessary to rebuild the exact-text field document fields so stop words are not ignored
 * by exact phrase searches.
 *
 * @since v6.3
 */
public class UpgradeTask_Build6319 extends AbstractReindexUpgradeTask
{
    @Override
    public String getBuildNumber()
    {
        return "6319";
    }

    @Override
    public String getShortDescription()
    {
        return format
                (
                        "%s Necessary so stop words are not ignored in exact phrase searches.",
                        super.getShortDescription()
                );
    }
}
