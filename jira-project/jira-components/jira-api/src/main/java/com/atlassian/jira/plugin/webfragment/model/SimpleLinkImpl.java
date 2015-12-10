package com.atlassian.jira.plugin.webfragment.model;

import java.util.Map;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink}.  Simple bean containing no
 * real logic.
 *
 * @since v4.0
 */
public class SimpleLinkImpl extends SimpleLinkSectionImpl implements SimpleLink
{
    final private String url;
    private final String accessKey;

    /**
     * Constructor taking all attributes of a link
     *
     * @param id        The unique id of the link
     * @param label     The optional label to display for the link
     * @param title     The optional title (tooltip) of the link
     * @param iconUrl   The optional url pointing an image for the link
     * @param style     The optional style to apply to the link
     * @param url       The url that the link points to
     * @param accessKey The optional easy access key for the link
     */
    public SimpleLinkImpl(String id, String label, String title, String iconUrl, String style,
            @Nonnull String url, String accessKey)
    {
        this(id, label, title, iconUrl, style, null, url, accessKey);
    }

    /**
     * Constructor taking all attributes of a link
     *
     * @param id        The unique id of the link
     * @param label     The optional label to display for the link
     * @param title     The optional title (tooltip) of the link
     * @param iconUrl   The optional url pointing an image for the link
     * @param style     The optional style to apply to the link
     * @param params    map of parameters
     * @param url       The url that the link points to
     * @param accessKey The optional easy access key for the link
     */
    public SimpleLinkImpl(String id, String label, String title, String iconUrl, String style, Map<String,String> params,
            @Nonnull String url, String accessKey)
    {
        this(id, label, title, iconUrl, style, params, url, accessKey, null);
    }

    /**
     * Constructor taking all attributes of a link
     *
     * @param id        The unique id of the link
     * @param label     The optional label to display for the link
     * @param title     The optional title (tooltip) of the link
     * @param iconUrl   The optional url pointing an image for the link
     * @param style     The optional style to apply to the link
     * @param params    map of parameters
     * @param url       The url that the link points to
     * @param accessKey The optional easy access key for the link
     */
    public SimpleLinkImpl(String id, String label, String title, String iconUrl, String style, Map<String,String> params,
            @Nonnull String url, String accessKey, Integer weight)
    {
        super(id, label, title, iconUrl, style, params, weight);
        notNull("url", url);
        this.url = url;
        this.accessKey = accessKey;
    }

    @Nonnull
    public String getUrl()
    {
        return url;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SimpleLinkImpl that = (SimpleLinkImpl) o;

        if (accessKey != null ? !accessKey.equals(that.accessKey) : that.accessKey != null)
        {
            return false;
        }
        if (label != null ? !label.equals(that.label) : that.label != null)
        {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null)
        {
            return false;
        }
        if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null)
        {
            return false;
        }
        if (style != null ? !style.equals(that.style) : that.style != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (!url.equals(that.url))
        {
            return false;
        }
        if (weight != null ? !weight.equals(that.weight) : that.weight != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = url.hashCode();
        result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (style != null ? style.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "SimpleLinkImpl{" +
                "label'" + label + "'" +
                ", title'" + title + "'" +
                ", iconUrl'" + iconUrl + "'" +
                ", style'" + style + "'" +
                ", url'" + url + "'" +
                ", id='" + id + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", weight='" + weight + '\'' +
                '}';
    }
}
