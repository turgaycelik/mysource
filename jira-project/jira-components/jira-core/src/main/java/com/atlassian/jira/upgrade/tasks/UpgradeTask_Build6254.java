package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Set default project description mode in JIRA settings
 *
 * @since v6.2
 */
public class UpgradeTask_Build6254 extends AbstractUpgradeTask
{
    private final ApplicationProperties applicationProperties;
    private final FeatureManager featureManager;

    public UpgradeTask_Build6254(final ApplicationProperties applicationProperties, final FeatureManager featureManager)
    {
        super(false);

        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "6254";
    }

    @Override
    public String getShortDescription()
    {
        return "Set default project description mode in JIRA settings";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        // on fresh installed BTF instance use "wiki", which is default
        // otherwise if we upgrade an existing instance, change to html in order to not break
        // any existing project descriptions which may be using html
        if(!featureManager.isOnDemand() && !setupMode)
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_PROJECT_DESCRIPTION_HTML_ENABLED, true);
        }
    }
}
