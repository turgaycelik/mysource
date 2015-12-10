package com.atlassian.jira.help;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * @since v6.2.4
 */
public class MockHelpUrls implements HelpUrls
{
    private Map<String, HelpUrl> urls = Maps.newHashMap();
    private HelpUrl defaultUrl;

    public MockHelpUrls(HelpUrl defaultUrl, Iterable<? extends HelpUrl> urls)
    {
        for (HelpUrl url : urls)
        {
            this.urls.put(url.getKey(), url);
        }
        this.urls.put(defaultUrl.getKey(), defaultUrl);
        this.defaultUrl = defaultUrl;
    }

    public MockHelpUrls(HelpUrl defaultUrl, HelpUrl...urls)
    {
        this(defaultUrl, Arrays.asList(urls));
    }

    public MockHelpUrls()
    {
        this(MockHelpUrl.simpleUrl("default"));
    }

    @Nonnull
    @Override
    public HelpUrl getUrl(@Nonnull final String key)
    {
        final HelpUrl helpUrl = urls.get(key);
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
        return Collections.unmodifiableSet(urls.keySet());
    }

    @Override
    public Iterator<HelpUrl> iterator()
    {
        return Collections.unmodifiableCollection(urls.values()).iterator();
    }

    public MockHelpUrls defaultUrl(HelpUrl url)
    {
        this.defaultUrl = url;
        return addUrl(url);
    }

    public MockHelpUrl createDefault()
    {
        return createDefault("default");
    }

    public MockHelpUrl createDefault(String key)
    {
        MockHelpUrl url = MockHelpUrl.simpleUrl(key);
        defaultUrl(url);
        return url;
    }

    public MockHelpUrls addUrl(HelpUrl url)
    {
        urls.put(url.getKey(), url);
        return this;
    }

    public MockHelpUrl createSimpleUrl(String key)
    {
        MockHelpUrl url = MockHelpUrl.simpleUrl(key);
        addUrl(url);
        return url;
    }

    public MockHelpUrl createUrl(String key)
    {
        MockHelpUrl result = new MockHelpUrl().setKey(key);
        addUrl(result);
        return result;
    }
}
