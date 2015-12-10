package com.atlassian.jira.help;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;

import static com.google.common.collect.Iterables.concat;

/**
 * @since v6.2.4
 */
public class MockHelpUrlsParser implements HelpUrlsParser
{
    private static final String DEFAULT_KEY = "default";

    private boolean onDemand;
    private MockHelpUrl defaultUrl;
    private Map<String, MockHelpUrl> btfUrls = Maps.newHashMap();
    private Map<String, MockHelpUrl> odUrls = Maps.newHashMap();

    public MockHelpUrlsParser()
    {
        this(true, new MockHelpUrl().setKey(DEFAULT_KEY).setUrl("").setTitle(""));
    }

    private MockHelpUrlsParser(boolean onDemand, MockHelpUrl defaultUrl)
    {
        this.onDemand = onDemand;
        this.defaultUrl = defaultUrl;
    }

    @Nonnull
    @Override
    public HelpUrlsParser onDemand(final boolean onDemand)
    {
        this.onDemand = onDemand;
        return this;
    }

    @Nonnull
    @Override
    public HelpUrlsParser defaultUrl(final String url, final String title)
    {
        defaultUrl.setUrl(url).setTitle(title);
        return this;
    }

    @Nonnull
    @Override
    public HelpUrls parse(@Nonnull final Properties properties)
    {
        Map<String, String> maps = Maps.newHashMap();
        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            maps.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return parse(maps);
    }

    @Nonnull
    @Override
    public HelpUrls parse(@Nonnull final Properties externalProperties, @Nonnull final Properties internalProperties)
    {
        HelpUrls external = parse(externalProperties);
        HelpUrls internal = parse(internalProperties);
        return new MockHelpUrls(external.getDefaultUrl(), concat(external, internal));
    }

    @Nonnull
    @Override
    public HelpUrls parse(@Nonnull final Map<String, String> properties)
    {
        MockHelpUrl defaultUrl = this.defaultUrl.copy();
        defaultUrl.setAlt(properties.get(DEFAULT_KEY));

        List<HelpUrl> urls = Lists.newArrayList();
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            String key = entry.getKey();
            if (!DEFAULT_KEY.equals(key))
            {
                urls.add(getGeneratedUrl(getUrl(entry.getKey()), entry.getValue()));
            }
        }
        return new MockHelpUrls(defaultUrl, urls);
    }

    private MockHelpUrl getUrl(final String key)
    {
        if (DEFAULT_KEY.equals(key))
        {
            return defaultUrl;
        }

        MockHelpUrl result = null;
        if (onDemand)
        {
            result = odUrls.get(key);
        }
        if (result == null)
        {
            result = btfUrls.get(key);
        }
        if (result == null)
        {
            throw new IllegalStateException(String.format("Unable to find URL with key '%s.'", key));
        }
        return result;
    }

    public HelpUrl getDefault()
    {
        return defaultUrl.copy();
    }

    public MockHelpUrlsParser registerOd(HelpUrl url)
    {
        odUrls.put(url.getKey(), new MockHelpUrl(url));
        return this;
    }

    public MockHelpUrlsParser register(HelpUrl url)
    {
        btfUrls.put(url.getKey(), new MockHelpUrl(url));
        return this;
    }

    public MockHelpUrl createUrl(String key, String url)
    {
        MockHelpUrl mockHelpUrl = new MockHelpUrl().setKey(key).setUrl(url);
        register(mockHelpUrl);
        return mockHelpUrl;
    }

    public MockHelpUrl createUrlOd(String key, String url)
    {
        MockHelpUrl mockHelpUrl = new MockHelpUrl().setKey(key).setUrl(url);
        registerOd(mockHelpUrl);
        return mockHelpUrl;
    }

    public HelpUrl getGeneratedUrl(HelpUrl url, String value)
    {
        return new MockHelpUrl(url).setAlt(value);
    }

    public HelpUrl getGeneratedDefault(String value)
    {
        return defaultUrl.copy().setAlt(value);
    }
}
