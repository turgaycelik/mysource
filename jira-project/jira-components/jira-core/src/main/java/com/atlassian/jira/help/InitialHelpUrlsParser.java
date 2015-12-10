package com.atlassian.jira.help;

import com.atlassian.jira.config.FeatureManager;
import com.google.common.base.Supplier;

/**
 * This is the actual {@link com.atlassian.jira.help.HelpUrlsParser} in PICO. It provides
 * an {@link DefaultHelpUrlsParser} instance with an initial {@link #onDemand} that does a runtime check
 * to determine how to process {@code .ondemand} properties.
 *
 * @since v6.2.4
 */
public class InitialHelpUrlsParser extends DefaultHelpUrlsParser
{
    public InitialHelpUrlsParser(final HelpUrlBuilder.Factory helpUrlBuilderFactory,
            final LocalHelpUrls localUrls, final FeatureManager featureManager)
    {
        super(helpUrlBuilderFactory, localUrls, new OnDemandSupplier(featureManager), null, null);
    }

    private static class OnDemandSupplier implements Supplier<Boolean>
    {
        private final FeatureManager featureManager;

        private OnDemandSupplier(final FeatureManager featureManager) {this.featureManager = featureManager;}

        @Override
        public Boolean get()
        {
            return featureManager.isOnDemand();
        }
    }
}
