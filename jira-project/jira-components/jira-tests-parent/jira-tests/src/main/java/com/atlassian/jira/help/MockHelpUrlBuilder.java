package com.atlassian.jira.help;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @since v6.2.4
 */
public class MockHelpUrlBuilder implements HelpUrlBuilder
{
    private String prefix;
    private String suffix;
    private String url;
    private String alt;
    private String title;
    private String key;
    private boolean local;

    public MockHelpUrlBuilder()
    {
        this(null, null);
    }

    public MockHelpUrlBuilder(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public HelpUrlBuilder key(final String key)
    {
        this.key = key;
        return this;
    }

    @Override
    public HelpUrlBuilder alt(final String alt)
    {
        this.alt = alt;
        return this;
    }

    @Override
    public HelpUrlBuilder title(final String title)
    {
        this.title = title;
        return this;
    }

    @Override
    public HelpUrlBuilder url(final String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public HelpUrlBuilder local(final boolean local)
    {
        this.local = local;
        return this;
    }

    @Override
    public HelpUrlBuilder copy()
    {
        return new MockHelpUrlBuilder(prefix, suffix)
                .key(key)
                .alt(alt)
                .title(title)
                .url(url)
                .local(local);
    }

    @Override
    public HelpUrl build()
    {
        return new MockHelpUrl()
                .setAlt(alt)
                .setTitle(title)
                .setUrl(generateUrl())
                .setLocal(local)
                .setKey(key);
    }

    public String suffix()
    {
        return suffix;
    }

    public String prefix()
    {
        return prefix;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final MockHelpUrlBuilder that = (MockHelpUrlBuilder) o;

        if (alt != null ? !alt.equals(that.alt) : that.alt != null) { return false; }
        if (key != null ? !key.equals(that.key) : that.key != null) { return false; }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) { return false; }
        if (suffix != null ? !suffix.equals(that.suffix) : that.suffix != null) { return false; }
        if (title != null ? !title.equals(that.title) : that.title != null) { return false; }
        if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (alt != null ? alt.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String generateUrl()
    {
        StringBuilder builder = new StringBuilder();
        if (prefix != null)
        {
            builder.append(prefix);
        }
        if (url != null)
        {
            builder.append(url);
        }
        if (suffix != null)
        {
            builder.append(suffix);
        }
        return stripToNull(builder.toString());
    }

    public static Factory factory()
    {
        return new MockFactory();
    }

    private static class MockFactory implements Factory
    {
        @Override
        public HelpUrlBuilder get(String prefix, String suffix)
        {
            return new MockHelpUrlBuilder(prefix, suffix);
        }
    }
}
