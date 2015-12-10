package com.atlassian.jira.help;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @since v6.2.4
 */
public class MockHelpUrl implements HelpUrl
{
    private String url;
    private String alt;
    private String title;
    private String key;
    private boolean local;

    public MockHelpUrl()
    {
    }

    public MockHelpUrl(HelpUrl url)
    {
        this.url = url.getUrl();
        this.title = url.getTitle();
        this.alt = url.getAlt();
        this.key = url.getKey();
        this.local = url.isLocal();
    }

    public static MockHelpUrl simpleUrl(String key)
    {
        return new MockHelpUrl()
                .setUrl(String.format("url.%s", key))
                .setAlt(String.format("alt.%s", key))
                .setTitle(String.format("title.%s", key))
                .setKey(String.format("key.%s", key));
    }

    public MockHelpUrl setUrl(final String url)
    {
        this.url = url;
        return this;
    }

    public MockHelpUrl setAlt(final String alt)
    {
        this.alt = alt;
        return this;
    }

    public MockHelpUrl setTitle(final String title)
    {
        this.title = title;
        return this;
    }

    public MockHelpUrl setKey(final String key)
    {
        this.key = key;
        return this;
    }

    public MockHelpUrl setLocal(final boolean local)
    {
        this.local = local;
        return this;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public String getAlt()
    {
        return alt;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public boolean isLocal()
    {
        return local;
    }

    public MockHelpUrl copy()
    {
        return new MockHelpUrl(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final MockHelpUrl that = (MockHelpUrl) o;

        if (local != that.local) { return false; }
        if (alt != null ? !alt.equals(that.alt) : that.alt != null) { return false; }
        if (key != null ? !key.equals(that.key) : that.key != null) { return false; }
        if (title != null ? !title.equals(that.title) : that.title != null) { return false; }
        if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (alt != null ? alt.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (local ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
