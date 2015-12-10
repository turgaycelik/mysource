package com.atlassian.jira.help;

import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * @since v6.2.4
 */
class ImmutableHelpUrls implements HelpUrls
{
    private final Map<String, HelpUrl> urls;
    private final HelpUrl defaultUrl;

    ImmutableHelpUrls(HelpUrl defaultUrl, Iterable<? extends HelpUrl> urls)
    {
        Assertions.notNull(urls);

        this.defaultUrl = Assertions.notNull(defaultUrl);

        final Map<String, HelpUrl> index = Maps.newHashMap();
        for (HelpUrl url : urls)
        {
            index.put(url.getKey(), url);
        }
        index.put(defaultUrl.getKey(), defaultUrl);
        this.urls = ImmutableMap.copyOf(index);
    }

    @Nonnull
    @Override
    public HelpUrl getUrl(@Nonnull final String key)
    {
        final HelpUrl helpUrl = urls.get(Assertions.notNull("key", key));
        return helpUrl == null ? defaultUrl : helpUrl;
    }

    @Nonnull
    @Override
    public HelpUrl getDefaultUrl()
    {
        return defaultUrl;
    }

    @Nonnull
    @Override
    public Set<String> getUrlKeys()
    {
        return urls.keySet();
    }

    @Override
    public Iterator<HelpUrl> iterator()
    {
        return urls.values().iterator();
    }
}
