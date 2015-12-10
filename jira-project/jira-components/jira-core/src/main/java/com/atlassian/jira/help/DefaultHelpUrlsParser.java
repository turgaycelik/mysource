package com.atlassian.jira.help;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * @since v6.2.4
 */
class DefaultHelpUrlsParser implements HelpUrlsParser
{
    private static final String URL_SUFFIX = ".url";
    private static final String ALT_SUFFIX = ".alt";
    private static final String TITLE_SUFFIX = ".title";
    private static final String ONDEMAND_SUFFIX = ".ondemand";

    private static final String DEFAULT_KEY = "default";

    private final HelpUrlBuilder.Factory helpUrlBuilderFactory;
    private final Supplier<Boolean> onDemand;
    private final String defaultUrl;
    private final String defaultTitle;
    private final LocalHelpUrls localUrls;

    DefaultHelpUrlsParser(final HelpUrlBuilder.Factory helpUrlBuilderFactory, final LocalHelpUrls localUrls,
            final Supplier<Boolean> onDemand, final String defaultUrl, final String defaultTitle)
    {
        this.localUrls = notNull("localUrls", localUrls);
        this.helpUrlBuilderFactory = notNull("helpUrlBuilderFactory", helpUrlBuilderFactory);
        this.onDemand = notNull("onDemand", onDemand);
        this.defaultUrl = trimToNull(defaultUrl);
        this.defaultTitle = trimToNull(defaultTitle);
    }

    @Nonnull
    @Override
    public HelpUrlsParser onDemand(boolean onDemand)
    {
        if (onDemand == this.onDemand.get())
        {
            return this;
        }
        else
        {
            return new DefaultHelpUrlsParser(helpUrlBuilderFactory, localUrls, Suppliers.ofInstance(onDemand), defaultUrl, defaultTitle);
        }
    }

    @Nonnull
    @Override
    public HelpUrlsParser defaultUrl(String url, String title)
    {
        return new DefaultHelpUrlsParser(helpUrlBuilderFactory, localUrls, onDemand, url, title);
    }

    @Nonnull
    @Override
    public HelpUrls parse(@Nonnull Properties properties)
    {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key != null && value != null)
            {
                builder.put(key.toString(), value.toString());
            }
        }
        return parse(builder.build());
    }

    @Nonnull
    @Override
    public HelpUrls parse(@Nonnull final Properties externalProperties, @Nonnull final Properties internalProperties)
    {
        HelpUrls urls = parse(externalProperties);
        Iterable<HelpUrl> internal = localUrls.parse(internalProperties);

        return new ImmutableHelpUrls(urls.getDefaultUrl(), Iterables.concat(internal, urls));
    }

    @Nonnull
    @Override
    public HelpUrls parse(@Nonnull Map<String, String> properties)
    {
        return new LookupInvocation(properties).execute();
    }

    /**
     * A command that implements the lookup of a {@link com.atlassian.jira.help.HelpUrls}. It is implemented
     * as a command so I can use global variables to pass state between methods without making
     * {@code DefaultHelpUrlsLoader} stateful.
     */
    private class LookupInvocation
    {
        private Map<String, String> properties;
        private HelpUrlBuilder template;

        private LookupInvocation(Map<String, String> properties)
        {
            this.properties = properties;
        }

        private HelpUrls execute()
        {
            properties = flattenNamespaces(properties);
            template = createDefaultUrl();
            template = loadHelpPath(DEFAULT_KEY);

            final HelpUrl defaultUrl = template.build();
            return new ImmutableHelpUrls(defaultUrl, parseUrls());
        }

        /**
         * Build an initial link that we will fall back to when things are really really broken. This will be used
         * if there are no plugins configured to serve URLs. This is an exceptional case as a system plugin
         * serves JIRA's default help paths.
         *
         * @return an initial link that we will fall back to when things are really really broken.
         */
        private HelpUrlBuilder createDefaultUrl()
        {
            return helpUrlBuilderFactory.get(properties.get("url-prefix"), properties.get("url-suffix"))
                    .key(DEFAULT_KEY)
                    .url(defaultUrl)
                    .title(defaultTitle);
        }

        /**
         * Removes the magic namespaces from keys and returns a true representation of the passed map based on
         * the current state of JIRA.
         *
         * Property keys can be namespaced with the {@code .ondemand} suffix to ensure they are used only
         * in OD. For example consider the {@code {help.url: /btf/btf.html, help.url.ondemand: /od/od.html,
         * help.title: title}} map. This method returns a map
         * {@code {help.url: /od/od.html, help.title: title}} in OD while in BTF it returns
         * {@code {help.url: /btf/btf.html, help.title: title}}.
         *
         * @param mapWithNamespace the map with namespaced keys.
         * @return new map without namespace keys but whose values are based on namespaces and the current
         *  state of JIRA.
         */
        private Map<String, String> flattenNamespaces(Map<String, String> mapWithNamespace)
        {
            final boolean isOnDemandInstance = onDemand.get();
            final Map<String, String> result = Maps.newHashMap();
            final Map<String, String> overrides = Maps.newHashMap();

            for (Map.Entry<String, String> entry : mapWithNamespace.entrySet())
            {
                final String key = entry.getKey();
                final String value = entry.getValue();

                if (key != null && value != null)
                {
                    final boolean isOnDemandProperty = endsWith(key, ONDEMAND_SUFFIX);

                    if (isOnDemandProperty)
                    {
                        if (isOnDemandInstance)
                        {
                            overrides.put(removeEnd(key, ONDEMAND_SUFFIX), value);
                        }
                    }
                    else
                    {
                        result.put(key, value);
                    }
                }
            }
            result.putAll(overrides);
            return result;
        }

        private Iterable<HelpUrl> parseUrls()
        {
            List<HelpUrl> newPaths = Lists.newArrayList();
            for (String property : properties.keySet())
            {
                //use '.url' as the key for whether it is a property or not
                if (endsWith(property, URL_SUFFIX))
                {
                    final String key = removeEnd(property, URL_SUFFIX);
                    newPaths.add(loadHelpPath(key).build());
                }
            }

            return newPaths;
        }

        private HelpUrlBuilder loadHelpPath(String key)
        {
            HelpUrlBuilder builder = template.copy().key(key);
            String url = properties.get(key + URL_SUFFIX);
            if (url != null)
            {
                builder = builder.url(url);
            }

            String alt = properties.get(key + ALT_SUFFIX);
            if (alt != null)
            {
                builder = builder.alt(alt);
            }

            String title = properties.get(key + TITLE_SUFFIX);
            if (title != null)
            {
                builder = builder.title(title);
            }

            return builder;
        }
    }
}
