package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequestInfo;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.issue.navigator.ToolOptionGroup;
import com.atlassian.jira.web.action.issue.navigator.ToolOptionItem;
import com.atlassian.jira.web.action.issue.util.ConditionalDescriptorPredicate;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.apache.log4j.Logger;
import webwork.action.ServletActionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility for determining view options to display on the issue navigator
 */
public class IssueNavigatorViewsHelper
{
    private static final Logger log = Logger.getLogger(IssueNavigatorViewsHelper.class);

    private final JiraAuthenticationContext authContext;
    private final PluginAccessor pluginAccessor;
    private final IssueSearchLimits issueSearchLimits;
    private final SearchRequestInfo searchRequestInfo;
    private final SearchService searchService;

    public IssueNavigatorViewsHelper(JiraAuthenticationContext authContext, PluginAccessor pluginAccessor,
            IssueSearchLimits issueSearchLimits, SearchService searchService, SearchRequestInfo searchRequestInfo)
    {
        this.authContext = authContext;
        this.pluginAccessor = pluginAccessor;
        this.issueSearchLimits = issueSearchLimits;
        this.searchRequestInfo = searchRequestInfo;
        this.searchService = searchService;
    }

    public List<ToolOptionGroup> getViewOptions(String view)
    {
        List<ToolOptionGroup> groups = Lists.newArrayList();

        ToolOptionGroup viewGroup = new ToolOptionGroup("viewOptions-dropdown", "");

        if ("bulk".equals(view))
        {
            String href = "?view=&tempMax=-1&decorator=";
            viewGroup.addItem(new ToolOptionItem("", getText("navigator.results.currentview.browser"), href, null, "nofollow"));
        }
        addModuleDescriptor(viewGroup, searchRequestInfo, getPrintable(), "printable", getText("navigator.results.currentview.browser.printable"));
        addModuleDescriptor(viewGroup, searchRequestInfo, getFullContent(), "fullContent", getText("navigator.results.currentview.browser.full"));
        addModuleDescriptor(viewGroup, searchRequestInfo, getXml(), "xml", getText("navigator.results.currentview.xml"));
        addIfNonEmpty(groups, viewGroup);

        ToolOptionGroup rssGroup = new ToolOptionGroup();
        addModuleDescriptor(rssGroup, searchRequestInfo, getRssIssues(), "rssIssues", getText("navigator.results.currentview.rss") + " (" + getText("navigator.results.currentview.rss.issues") + ")");
        addModuleDescriptor(rssGroup, searchRequestInfo, getRssComments(), "rssComments", getText("navigator.results.currentview.rss") + " (" + getText("navigator.results.currentview.rss.comments") + ")");
        addIfNonEmpty(groups, rssGroup);

        ToolOptionGroup wordGroup = new ToolOptionGroup();
        addModuleDescriptor(wordGroup, searchRequestInfo, getWord(), "word", getText("navigator.results.currentview.word"));
        addIfNonEmpty(groups, wordGroup);

        ToolOptionGroup excelGroup = new ToolOptionGroup();
        addModuleDescriptor(excelGroup, searchRequestInfo, getAllExcelFields(), "allExcelFields", getText("navigator.results.currentview.excel") + " (" + getText("navigator.results.currentview.excel.full") + ")");
        addModuleDescriptor(excelGroup, searchRequestInfo, getCurrentExcelFields(), "currentExcelFields", getText("navigator.results.currentview.excel") + " (" + getText("navigator.results.currentview.excel.current") + ")");
        addIfNonEmpty(groups, excelGroup);


        SearchRequestViewModuleDescriptor chart = getChart();
        if (null != chart && null != getLoggedInUser())
        {
            ToolOptionGroup chartGroup = new ToolOptionGroup();
            chartGroup.addItem(new ToolOptionItem("charts", getText("navigator.results.currentview.charts"), "#", true, null, "nofollow"));
            groups.add(chartGroup);

            ToolOptionGroup dashboardGroup = new ToolOptionGroup();
            dashboardGroup.addItem(new ToolOptionItem("onDashboard", getText("navigator.results.currentview.on.dashboard"), "#", true, null, "nofollow"));
            groups.add(dashboardGroup);
        }

        Collection<SearchRequestViewModuleDescriptor> nonSystemSearchRequestViews = getNonSystemSearchRequestViews();
        if (!nonSystemSearchRequestViews.isEmpty())
        {
            ToolOptionGroup nonSystemGroup = new ToolOptionGroup();
            for (SearchRequestViewModuleDescriptor moduleDescriptor : nonSystemSearchRequestViews)
            {
                String href = moduleDescriptor.getURLWithoutContextPath(searchRequestInfo);
                nonSystemGroup.addItem(new ToolOptionItem(null, moduleDescriptor.getName(), href, null, "nofollow"));
            }
            addIfNonEmpty(groups, nonSystemGroup);
        }

        return groups;
    }

    private void addModuleDescriptor(ToolOptionGroup options, SearchRequestInfo searchRequestInfo, SearchRequestViewModuleDescriptor moduleDescriptor, String id, String labelKey)
    {
        if (null != moduleDescriptor)
        {
            String href = this.getRestricted(moduleDescriptor.getURLWithoutContextPath(searchRequestInfo));
            options.addItem(new ToolOptionItem(id, labelKey, href, null, "nofollow"));
        }
    }

    private void addIfNonEmpty(List<ToolOptionGroup> groups, ToolOptionGroup group)
    {
        if (!group.isEmpty())
        {
            groups.add(group);
        }
    }

    public SearchRequestViewModuleDescriptor getPrintable()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-printable");
    }

    public SearchRequestViewModuleDescriptor getFullContent()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-fullcontent");
    }

    public SearchRequestViewModuleDescriptor getXml()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-xml");
    }

    public SearchRequestViewModuleDescriptor getRssIssues()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-rss");
    }

    public SearchRequestViewModuleDescriptor getRssComments()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-comments-rss");
    }

    public SearchRequestViewModuleDescriptor getWord()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-word");
    }

    public SearchRequestViewModuleDescriptor getAllExcelFields()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-excel-all-fields");
    }

    public SearchRequestViewModuleDescriptor getCurrentExcelFields()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-excel-current-fields");
    }

    public SearchRequestViewModuleDescriptor getChart()
    {
        return (SearchRequestViewModuleDescriptor) pluginAccessor.getEnabledPluginModule("jira.issueviews:searchrequest-charts-view");
    }

    public String getRestricted(final String url)
    {
        return URLUtil.addRequestParameter(url, getRestrictionClause());
    }

    public String getSearchRequestJqlString()
    {
        if (searchRequestInfo != null)
        {
            return searchService.getJqlString(searchRequestInfo.getQuery());
        }
        else
        {
            return "";
        }
    }

    private String getRestrictionClause()
    {
        final int count = issueSearchLimits.getMaxResults();
        if (count <= 0)
        {
            return null;
        }
        return "tempMax=" + count;
    }

    public Collection<SearchRequestViewModuleDescriptor> getNonSystemSearchRequestViews()
    {
        final List<SearchRequestViewModuleDescriptor> enabledModuleDescriptorsByClass =
                new ArrayList<SearchRequestViewModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(SearchRequestViewModuleDescriptor.class));
        for (final Iterator<SearchRequestViewModuleDescriptor> iterator = enabledModuleDescriptorsByClass.iterator(); iterator.hasNext();)
        {
            final SearchRequestViewModuleDescriptor moduleDescriptor = iterator.next();
            // remove the views that ship with JIRA. (see exceptions below.
            if ("jira.issueviews:searchrequest-printable".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-fullcontent".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-xml".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-rss".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-comments-rss".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-word".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-excel-all-fields".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-excel-current-fields".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
            if ("jira.issueviews:searchrequest-charts-view".equals(moduleDescriptor.getCompleteKey()))
            {
                iterator.remove();
            }
        }
        return Ordering.from(ModuleDescriptorComparator.COMPARATOR).compound(Ordering.natural().onResultOf(new Function<SearchRequestViewModuleDescriptor, Comparable>()
        {
            @Override
            public Comparable apply(@Nullable SearchRequestViewModuleDescriptor input)
            {
                return input == null ? null : input.getName();
            }
        })).sortedCopy(Iterables.filter(enabledModuleDescriptorsByClass,
                new ConditionalDescriptorPredicate(makeContext(getLoggedInUser(), new JiraHelper(ServletActionContext.getRequest())))));
    }

    public static Map<String, Object> makeContext(User remoteUser, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();
        params.put(JiraWebInterfaceManager.CONTEXT_KEY_USER, remoteUser);
        params.put(JiraWebInterfaceManager.CONTEXT_KEY_HELPER, jiraHelper);

        return params;
    }

    public User getLoggedInUser()
    {
        return authContext.getLoggedInUser();
    }

    private String getText(String key)
    {
        return authContext.getI18nHelper().getText(key);
    }

    private String getText(String key, Object values)
    {
        return authContext.getI18nHelper().getText(key, values);
    }
}
