package com.atlassian.jira.help;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @since v6.2.4
 */
class ImmutableHelpUrl implements HelpUrl
{
    private final String url;
    private final String alt;
    private final String key;
    private final boolean local;
    private final String title;

    ImmutableHelpUrl(String key, String url, String title, String alt, boolean local)
    {
        this.key = Assertions.notNull("key", key);
        this.url = url;
        this.alt = alt;
        this.title = title;
        this.local = local;
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

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final ImmutableHelpUrl that = (ImmutableHelpUrl) o;

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
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (local ? 1 : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("url", url)
                .append("alt", alt)
                .append("key", key)
                .append("local", local)
                .append("title", title)
                .toString();
    }
}
