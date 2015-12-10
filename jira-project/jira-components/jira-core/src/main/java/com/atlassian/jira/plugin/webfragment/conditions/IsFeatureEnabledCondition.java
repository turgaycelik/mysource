package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p/>
 * Will allow displaying given fragment if a particular feature is enabled.
 *
 * <p/>
 * The feature must be provided as a parameter to this condition named 'featureKey'.
 *
 * @since v4.4
 */
public class IsFeatureEnabledCondition implements Condition
{
    private static final String FEATURE_KEY = "featureKey";

    private final FeatureManager featureManager;
    private String featureKey;

    public IsFeatureEnabledCondition(FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        this.featureKey = params.get(FEATURE_KEY);
        notNull(FEATURE_KEY + " parameter must be provided to this condition", featureKey);
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return featureManager.isEnabled(featureKey);
    }
}
