package com.atlassian.jira.help;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * @since v6.2.4
 */
public class MockLocalHelpUrls implements LocalHelpUrls
{
    private List<HelpUrl> urls = Lists.newArrayList();

    @Nonnull
    @Override
    public Iterable<HelpUrl> parse(@Nonnull final Properties properties)
    {
        return load();
    }

    @Nonnull
    @Override
    public Iterable<HelpUrl> load()
    {
        return ImmutableList.copyOf(urls);
    }

    public MockLocalHelpUrls add(HelpUrl url)
    {
        urls.add(url);
        return this;
    }

    public MockHelpUrl add(String key)
    {
        MockHelpUrl mockHelpUrl = MockHelpUrl.simpleUrl(key);
        add(mockHelpUrl);
        return mockHelpUrl;
    }
}
