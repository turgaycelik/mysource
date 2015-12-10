package com.atlassian.jira.plugin.viewissue.issuelink;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.Map;

/**
 * Issue link context object. Used by the velocity template renderer.
 *
 * @since v5.0
 */
public final class IssueLinkContext
{
    private final String htmlElementId;
    private final String html;
    private final String deleteUrl;
    private final boolean remote;
    private final Long id;
    private final boolean requiresAsyncLoading;
    private final Map<String, Object> map;

    private IssueLinkContext(String htmlElementId, String deleteUrl, boolean remote, String html, Long id, boolean requiresAsyncLoading, Map<String, Object> map)
    {
        this.htmlElementId = htmlElementId;
        this.deleteUrl = deleteUrl;
        this.remote = remote;
        this.html = html;
        this.id = id;
        this.requiresAsyncLoading = requiresAsyncLoading;
        this.map = map;
    }

    public Map<String, Object> getMap()
    {
        return map;
    }

    public String getHtmlElementId()
    {
        return htmlElementId;
    }

    public String getHtml()
    {
        return html;
    }

    public String getDeleteUrl()
    {
        return deleteUrl;
    }

    public boolean isRemote()
    {
        return remote;
    }

    public Long getId()
    {
        return id;
    }

    public boolean isRequiresAsyncLoading()
    {
        return requiresAsyncLoading;
    }

    public static IssueLinkContext newRemoteIssueLinkContext(String htmlElementId, String deleteUrl, boolean remote, String html, Long id, boolean requiresAsyncLoading)
    {
        return new IssueLinkContext(htmlElementId, deleteUrl, remote, html, id, requiresAsyncLoading, Collections.<String, Object>emptyMap());
    }

    public static IssueLinkContext newLocalIssueLinkContext(String htmlElementId, String deleteUrl, boolean remote, Map<String, Object> map)
    {
        return new IssueLinkContext(htmlElementId, deleteUrl, remote, null, null, false, map);
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

        IssueLinkContext that = (IssueLinkContext) o;

        if (remote != that.remote)
        {
            return false;
        }
        if (deleteUrl != null ? !deleteUrl.equals(that.deleteUrl) : that.deleteUrl != null)
        {
            return false;
        }
        if (html != null ? !html.equals(that.html) : that.html != null)
        {
            return false;
        }
        if (htmlElementId != null ? !htmlElementId.equals(that.htmlElementId) : that.htmlElementId != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (map != null ? !map.equals(that.map) : that.map != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = htmlElementId != null ? htmlElementId.hashCode() : 0;
        result = 31 * result + (html != null ? html.hashCode() : 0);
        result = 31 * result + (deleteUrl != null ? deleteUrl.hashCode() : 0);
        result = 31 * result + (remote ? 1 : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (map != null ? map.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("htmlElementId", htmlElementId)
                .append("deleteUrl", deleteUrl)
                .append("remote", remote)
                .append("html", html)
                .append("map", map)
                .append("id", id)
                .toString();
    }
}
