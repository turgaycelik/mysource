package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * Reindexes JIRA due to a bug in the way {@link com.atlassian.jira.issue.customfields.impl.MultiSelectCFType} fields
 * were being indexed.
 *
 * @since v5.0
 */
public class UpgradeTask_Build704 extends AbstractReindexUpgradeTask
{

    @Override
    public String getBuildNumber()
    {
        return "704";
    }

    @Override
    public String getShortDescription()
    {
        return super.getShortDescription() + " due to changes to way multi select values are indexed.";
    }
}
