package com.atlassian.jira.help;

import com.atlassian.jira.config.FeatureManager;
import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.2.4
 */
public class SwitchingHelpUrlBuilderFactory implements HelpUrlBuilder.Factory
{
    private final FeatureManager featureManager;
    private final HelpUrlBuilder.Factory analyticsFactory;
    private final HelpUrlBuilder.Factory simpleFactory;

    public SwitchingHelpUrlBuilderFactory(final FeatureManager featureManager,
            final AnalyticsHelpUrlBuilder.Factory analyticsFactory, final SimpleHelpUrlBuilder.Factory simpleFactory)
    {
        this.analyticsFactory = notNull("analyticsFactory", analyticsFactory);
        this.simpleFactory = notNull("simpleFactory", simpleFactory);
        this.featureManager = notNull("featureManager", featureManager);
    }

    @VisibleForTesting
    SwitchingHelpUrlBuilderFactory(final FeatureManager featureManager,
            HelpUrlBuilder.Factory analyticsFactory, HelpUrlBuilder.Factory simpleFactory)
    {
        this.analyticsFactory = notNull("analyticsFactory", analyticsFactory);
        this.simpleFactory = notNull("simpleFactory", simpleFactory);
        this.featureManager = notNull("featureManager", featureManager);
    }

    @Override
    public HelpUrlBuilder get(String prefix, String suffix)
    {
        return featureManager.isOnDemand() ? analyticsFactory.get(prefix, suffix) : simpleFactory.get(prefix, suffix);
    }
}
