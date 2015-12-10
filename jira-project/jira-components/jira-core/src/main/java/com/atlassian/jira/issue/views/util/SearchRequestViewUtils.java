package com.atlassian.jira.issue.views.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mail.SubscriptionMailQueueMockRequest;
import com.atlassian.jira.plugin.issueview.IssueView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestView;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.PluginAccessor;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class SearchRequestViewUtils
{
    private final static BuildUtilsInfo BUILD_UTILS_INFO = new BuildUtilsInfoImpl();

    public static String getLink(final SearchRequest searchRequest, final String baseUrl, final User user)
    {
        final StringBuilder link = new StringBuilder(baseUrl).append("/secure/IssueNavigator.jspa?");
        if (searchRequest.getId() != null)
        {
            link.append("requestId=").append(searchRequest.getId());
        }
        else
        {
            link.append("reset=true").append(getSearchService().getQueryString(user, searchRequest.getQuery()));
        }

        return link.toString();
    }

    private static SearchService getSearchService()
    {
        return ComponentAccessor.getComponent(SearchService.class);
    }

    public static <V extends IssueView> V getIssueView(final Class<V> issueViewClass)
    {
        final PluginAccessor pluginAccessor = ComponentAccessor.getComponentOfType(PluginAccessor.class);
        final List<V> modules = pluginAccessor.getEnabledModulesByClass(issueViewClass);
        if (!modules.isEmpty())
        {
            return modules.get(0);
        }
        else
        {
            return null;
        }
    }

    public static <V extends SearchRequestView> V getSearchRequestView(final Class<V> searchRequestViewClass)
    {
        final PluginAccessor pluginAccessor = ComponentAccessor.getComponentOfType(PluginAccessor.class);
        final List<V> modules = pluginAccessor.getEnabledModulesByClass(searchRequestViewClass);
        if (!modules.isEmpty())
        {
            return modules.get(0);
        }
        else
        {
            return null;
        }
    }

    public static String getTitle(final SearchRequest searchRequest, final String jiraTitle)
    {
        return searchRequest.getName() != null ? searchRequest.getName() + " (" + jiraTitle + ")" : jiraTitle;
    }

    public static String getGeneratedInfo(final User user)
    {
        final I18nBean i18nBean = new I18nBean(user);
        if (user != null)
        {
            return i18nBean.getText("rss.search.request.view.header.text.with.user", new Date().toString(), user.getDisplayName(),
                BUILD_UTILS_INFO.getBuildInformation());
        }
        return i18nBean.getText("rss.search.request.view.header.text.with.out.user", new Date().toString(), BUILD_UTILS_INFO.getBuildInformation());
    }

    public static HttpServletRequest getMockRequest(final String baseUrl)
    {
        return new SubscriptionMailQueueMockRequest(getContextPath(baseUrl));
    }

    /**
     * Extracts the context path (if any) from a base URL.
     * @param baseURL base url to extract the context path from
     * @return context path
     */
    private static String getContextPath(final String baseURL)
    {
        try
        {
            return new URL(baseURL).getPath();
        }
        catch (final MalformedURLException e)
        {
            return "Incorrect baseURL format: " + baseURL;
        }
    }
}
