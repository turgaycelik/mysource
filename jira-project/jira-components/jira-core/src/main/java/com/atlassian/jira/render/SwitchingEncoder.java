package com.atlassian.jira.render;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.web.HttpRequestLocal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Encoder that delegates to the correct implementation depending on whether it is running in OnDemand or not.
 *
 * @since v5.0.7
 */
public class SwitchingEncoder implements Encoder
{
    private final FeatureManager featureManager;
    private final HttpRequestLocal<Encoder> requestEncoder = new HttpRequestLocal<Encoder>(SwitchingEncoder.class.getName()) {
        @Override
        protected Encoder initialValue()
        {
            return createEncoder();
        }
    };

    public SwitchingEncoder(final FeatureManager featureManager)
    {
        this.featureManager = featureManager;
    }

    @Nonnull
    @Override
    public String encodeForHtml(@Nullable final Object input)
    {
        // returns null if there is no current HTTP request
        Encoder encoder = requestEncoder.get();
        if (encoder == null)
        {
            encoder = createEncoder();
        }

        return encoder.encodeForHtml(input);
    }

    @Nonnull
    protected Encoder createEncoder()
    {
        if (featureManager.isOnDemand())
        {
            return new StrictEncoder();
        }

        return new NoOpEncoder();
    }
}
