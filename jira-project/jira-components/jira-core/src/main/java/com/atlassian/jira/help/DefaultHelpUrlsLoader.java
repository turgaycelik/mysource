package com.atlassian.jira.help;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.resourcebundle.ResourceBundleLoader;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.2.4
 */
public class DefaultHelpUrlsLoader implements HelpUrlsLoader
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHelpUrlsLoader.class);

    private static final String DEFAULT_HELP_URL = "https://confluence.atlassian.com/display/JIRA/";

    private final ResourceBundleLoader loader;
    private final JiraAuthenticationContext ctx;
    private final FeatureManager featureManager;
    private final LocalHelpUrls localHelpUrls;
    private final I18nHelper.BeanFactory i18n;
    private final HelpUrlsParser parser;

    public DefaultHelpUrlsLoader(final ResourceBundleLoader loader, final JiraAuthenticationContext ctx,
            final FeatureManager featureManager, LocalHelpUrls localHelpUrls,
            final I18nHelper.BeanFactory i18n, final HelpUrlsParser parser)
    {
        this.i18n = i18n;
        this.parser = parser;
        this.loader = loader.helpText();
        this.ctx = ctx;
        this.featureManager = featureManager;
        this.localHelpUrls = localHelpUrls;
    }

    @Nonnull
    @Override
    public HelpUrlsLoaderKey keyForCurrentUser()
    {
        return new LoaderKey(ctx.getLocale(), featureManager.isOnDemand());
    }

    @Override
    public HelpUrls apply(final HelpUrlsLoaderKey input)
    {
        notNull(input);

        //Just make sure that the passed HelpUrlsLoaderKey actually is something that this class generated.
        if (input instanceof LoaderKey)
        {
            LOG.debug("Loading help urls for key '{}'.", input);
            return apply((LoaderKey) input);
        }
        else
        {
            throw new IllegalArgumentException("'input' was not created by a call to keyForCurrentUser.");
        }
    }

    private HelpUrls apply(LoaderKey key)
    {
        HelpUrls externalHelpUrls = getExternalHelpUrls(key);
        return new ImmutableHelpUrls(externalHelpUrls.getDefaultUrl(), Iterables.concat(localHelpUrls.load(), externalHelpUrls));
    }

    private HelpUrls getExternalHelpUrls(final LoaderKey key)
    {
        return parser
                .onDemand(key.onDemand)
                .defaultUrl(DEFAULT_HELP_URL, i18n.getInstance(key.locale).getText("jira.help.paths.help.title"))
                .parse(loader.locale(key.locale).load().getData());
    }

    /**
     * Private implementation of {@link com.atlassian.jira.help.HelpUrlsLoader.HelpUrlsLoaderKey} returned by
     * this instance. This it encapsulates all the state needed to lookup a {@link HelpUrls} instance using this
     * class. It is used internally by {@link #apply(com.atlassian.jira.help.HelpUrlsLoader.HelpUrlsLoaderKey)}
     * as arguments for the query. It is used by callers to cache the result of the the {@code apply} call.
     */
    @VisibleForTesting
    static class LoaderKey implements HelpUrlsLoaderKey
    {
        private final Locale locale;
        private final boolean onDemand;

        @VisibleForTesting
        LoaderKey(final Locale locale, final boolean onDemand)
        {
            this.locale = locale;
            this.onDemand = onDemand;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final LoaderKey getKey = (LoaderKey) o;

            if (onDemand != getKey.onDemand) { return false; }
            if (!locale.equals(getKey.locale)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = locale.hashCode();
            result = 31 * result + (onDemand ? 1 : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("locale", locale)
                    .append("onDemand", onDemand)
                    .toString();
        }
    }
}
