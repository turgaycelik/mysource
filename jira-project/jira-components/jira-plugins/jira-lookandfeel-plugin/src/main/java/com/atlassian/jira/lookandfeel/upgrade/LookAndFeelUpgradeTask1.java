package com.atlassian.jira.lookandfeel.upgrade;

import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.lookandfeel.LookAndFeelProperties;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.upgrade.PluginUpgradeTask;

import java.util.Collection;
import java.util.Collections;

/**
 * Reset favicon URLs to the new default values.
 *
 * @since v5.0
 */
public class LookAndFeelUpgradeTask1 implements PluginUpgradeTask
{

    private final LookAndFeelProperties lookAndFeelProperties;
    private final FeatureManager featureManager;

    public LookAndFeelUpgradeTask1(LookAndFeelProperties lookAndFeelProperties, FeatureManager featureManager)
    {
        this.lookAndFeelProperties = lookAndFeelProperties;
        this.featureManager = featureManager;
    }

    @Override
    public int getBuildNumber()
    {
        return 1;
    }

    @Override
    public String getShortDescription()
    {
        return "Upgrade JIRA favicons URLs";
    }

    @Override
    public Collection<Message> doUpgrade() throws Exception
    {
        // JIRA standalone only, OnDemand has its own default values
        if (!featureManager.isOnDemand())
        {
            lookAndFeelProperties.resetDefaultFavicon();
        }
        return Collections.emptyList();
    }

    @Override
    public String getPluginKey()
    {
        return "com.atlassian.jira.lookandfeel";
    }
}
