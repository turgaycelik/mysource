package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.search.quicksearch.ComponentQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.CreatedQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.DueDateQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.FixForQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.IssueTypeQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.ModifiableQuickSearchResult;
import com.atlassian.jira.issue.search.quicksearch.MyIssuesQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.OverdueQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.PriorityQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.ProjectQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.QuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.QuickSearchResult;
import com.atlassian.jira.issue.search.quicksearch.RaisedInVersionQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.ReporterQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.ResolutionQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.StatusQuickSearchHandler;
import com.atlassian.jira.issue.search.quicksearch.UpdatedQuickSearchHandler;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultQueryCreator implements QueryCreator
{
    // TODO this should make jql based URLs rather than old-school issue nav URLs
    private final List<QuickSearchHandler> queryHandlers = new ArrayList<QuickSearchHandler>();
    private ApplicationProperties applicationProperties;

    public DefaultQueryCreator(ProjectManager projectManager, ConstantsManager constantsManager,
            VersionManager versionManager, PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext, ProjectComponentManager projectComponentManager,
            DateTimeFormatterFactory dateTimeFormatterFactory, TimeZoneManager timeZoneManager,
            ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        queryHandlers.add(new ProjectQuickSearchHandler(projectManager, permissionManager, authenticationContext));
        queryHandlers.add(new IssueTypeQuickSearchHandler(constantsManager));
        queryHandlers.add(new StatusQuickSearchHandler(constantsManager));
        queryHandlers.add(new FixForQuickSearchHandler(versionManager, projectManager, permissionManager, authenticationContext)); //this needs to go after the project quick search handler
        queryHandlers.add(new RaisedInVersionQuickSearchHandler(versionManager, projectManager, permissionManager, authenticationContext)); //this needs to go after the project quick search handler
        queryHandlers.add(new ComponentQuickSearchHandler(projectComponentManager, projectManager, permissionManager, authenticationContext)); //this needs to go after the project quick search handler
        queryHandlers.add(new MyIssuesQuickSearchHandler());
        queryHandlers.add(new OverdueQuickSearchHandler());
        queryHandlers.add(new ReporterQuickSearchHandler());
        queryHandlers.add(new ResolutionQuickSearchHandler(constantsManager));
        queryHandlers.add(new PriorityQuickSearchHandler(constantsManager));
        queryHandlers.add(new CreatedQuickSearchHandler(dateTimeFormatterFactory));
        queryHandlers.add(new UpdatedQuickSearchHandler(dateTimeFormatterFactory));
        queryHandlers.add(new DueDateQuickSearchHandler(timeZoneManager));

    }

    public String createQuery(String searchString)
    {
        if (searchString == null)
        {
            return NULL_QUERY;
        }

        QuickSearchResult quickSearchResult = searchQuickSearch(searchString);
        String quickSearchQuery = quickSearchResult.getQueryString();
        String query = QUERY_PREFIX + getDefaultQuery(quickSearchResult.getSearchInput()) + quickSearchQuery;
        if (TextUtils.stringSet(quickSearchQuery))
        {
            query = query + "&usedQuickSearch=true&originalQuickSearchQuery=" + encode(searchString);
        }

        return query;
    }

    private QuickSearchResult searchQuickSearch(String searchString)
    {
        QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult(searchString);
        for (QuickSearchHandler quickSearchHandler : queryHandlers)
        {
            quickSearchHandler.modifySearchResult(quickSearchResult);
        }
        return quickSearchResult;
    }


    private String getDefaultQuery(String searchString)
    {
        if (TextUtils.stringSet(searchString))
        {
            return "&text=" + encode(searchString);
        }
        else
        {
            return "";
        }
    }

    private String encode(final String searchString)
    {
        // we call this so that the static ComponentAccessor is not used and we are not detestible
        return JiraUrlCodec.encode(searchString, applicationProperties.getEncoding(), true);
    }

}
