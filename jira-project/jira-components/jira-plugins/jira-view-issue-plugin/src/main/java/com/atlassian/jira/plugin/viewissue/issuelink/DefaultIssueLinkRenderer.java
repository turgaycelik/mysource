package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.issuelink.AbstractIssueLinkRenderer;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Implements the default issue link renderer.
 *
 * @since v5.0
 */
public class DefaultIssueLinkRenderer extends AbstractIssueLinkRenderer
{
    public static final String DEFAULT_ICON_URL = "/images/icons/generic_link_16.png";

    @Override
    public Map<String, Object> getInitialContext(RemoteIssueLink remoteIssueLink, Map<String, Object> context)
    {
        final I18nHelper i18n = getValue(context, "i18n", I18nHelper.class);
        final String baseUrl = getValue(context, "baseurl", String.class);
        return createContext(remoteIssueLink, i18n, baseUrl);
    }

    private <T> T getValue(Map<String, Object> context, String key, Class<T> klass)
    {
        Object obj = context.get(key);
        if (obj == null)
        {
            throw new IllegalArgumentException(String.format("Expected '%s' to exist in the context map", key));
        }
        return klass.cast(obj);
    }

    private static Map<String, Object> createContext(RemoteIssueLink remoteIssueLink, I18nHelper i18n, String baseUrl)
    {
        ImmutableMap.Builder<String, Object> contextBuilder = ImmutableMap.builder();

        String tooltip = getTooltip(remoteIssueLink);
        final String iconUrl = StringUtils.defaultIfEmpty(remoteIssueLink.getIconUrl(), baseUrl + DEFAULT_ICON_URL);
        final String iconTooltip = getIconTooltip(remoteIssueLink, i18n);
        final String statusName;
        if (remoteIssueLink.getStatusIconUrl() != null)
        {
             statusName = StringUtils.defaultIfEmpty(remoteIssueLink.getStatusName(), "");
        }
        else
        {
            statusName = remoteIssueLink.getStatusName();
        }


        ImmutableMap<String, Object> statusCategory = null;
        if (remoteIssueLink.hasStatusCategory())
        {
            ImmutableMap.Builder<String, Object> sc = ImmutableMap.builder();
            putMap(sc, "key", remoteIssueLink.getStatusCategoryKey());
            putMap(sc, "colorName", remoteIssueLink.getStatusCategoryColorName());
            statusCategory = sc.build();
        }

        ImmutableMap.Builder<String, Object> s = ImmutableMap.builder();
        putMap(s, "name", statusName);
        putMap(s, "description", remoteIssueLink.getStatusDescription());
        putMap(s, "statusCategory", statusCategory);
        putMap(s, "iconUrl", remoteIssueLink.getStatusIconUrl());
        ImmutableMap<String,Object> status = s.build();

        putMap(contextBuilder, "id", remoteIssueLink.getId());
        putMap(contextBuilder, "url", remoteIssueLink.getUrl());
        putMap(contextBuilder, "title", remoteIssueLink.getTitle());
        putMap(contextBuilder, "iconUrl", iconUrl);
        putMap(contextBuilder, "iconTooltip", iconTooltip);
        putMap(contextBuilder, "tooltip", tooltip);
        putMap(contextBuilder, "summary", remoteIssueLink.getSummary());
        putMap(contextBuilder, "statusIconUrl", remoteIssueLink.getStatusIconUrl());
        putMap(contextBuilder, "statusIconTooltip", remoteIssueLink.getStatusIconTitle());
        if (status.size() > 0)
        {
            putMap(contextBuilder, "status", status);
        }
        putMap(contextBuilder, "statusIconLink", remoteIssueLink.getStatusIconLink());
        putMap(contextBuilder, "resolved", remoteIssueLink.isResolved() == null ? false : remoteIssueLink.isResolved());
        return contextBuilder.build();
    }

    private static void putMap(ImmutableMap.Builder<String, Object> mapBuilder, String key, Object value)
    {
        if (value != null)
        {
            mapBuilder.put(key, value);
        }
    }

    private static String getIconTooltip(RemoteIssueLink remoteIssueLink, I18nHelper i18n)
    {
        final boolean hasApplicationName = StringUtils.isNotEmpty(remoteIssueLink.getApplicationName());
        final boolean hasIconText = StringUtils.isNotEmpty(remoteIssueLink.getIconTitle());

        if (hasApplicationName && hasIconText)
        {
            return "[" + remoteIssueLink.getApplicationName() + "] " + remoteIssueLink.getIconTitle();
        }
        else if (hasApplicationName)
        {
            return "[" + remoteIssueLink.getApplicationName() + "]";
        }
        else if (hasIconText)
        {
            return remoteIssueLink.getIconTitle();
        }
        else
        {
            return i18n.getText("issuelinking.remote.link.weblink.title");
        }
    }

    private static String getTooltip(RemoteIssueLink remoteIssueLink)
    {
        final boolean hasApplicationName = StringUtils.isNotEmpty(remoteIssueLink.getApplicationName());
        final boolean hasSummary = StringUtils.isNotEmpty(remoteIssueLink.getSummary());

        if (hasApplicationName && hasSummary)
        {
            return "[" + remoteIssueLink.getApplicationName() + "] " + remoteIssueLink.getTitle() + ": " + remoteIssueLink.getSummary();
        }
        else if (hasApplicationName)
        {
            return "[" + remoteIssueLink.getApplicationName() + "] " + remoteIssueLink.getTitle();
        }
        else if (hasSummary)
        {
            return remoteIssueLink.getTitle() + ": " + remoteIssueLink.getSummary();
        }
        else
        {
            return remoteIssueLink.getTitle();
        }
    }
}
