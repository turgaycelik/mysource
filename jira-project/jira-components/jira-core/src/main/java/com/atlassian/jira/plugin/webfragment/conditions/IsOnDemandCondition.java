package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Checks if JIRA runs in On Demand environment
 *
 * @since v5.0
 */
public class IsOnDemandCondition implements Condition
{
    private final FeatureManager featureManager;

    public IsOnDemandCondition(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return featureManager.isOnDemand();
    }
}
