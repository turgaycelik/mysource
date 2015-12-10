package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ReporterIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.ReporterSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.*;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

public class ReporterSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    public static final String NAME_KEY = "navigator.filter.reportedby";

    final SearcherInformation<SearchableField> searcherInformation;
    final SearchRenderer searchRenderer;
    final SearchInputTransformer searchInputTransformer;

    public ReporterSearcher(
            VelocityRequestContextFactory velocityRequestContextFactory,
            VelocityTemplatingEngine templatingEngine,
            ApplicationProperties applicationProperties,
            UserPickerSearchService userPickerSearchService,
            GroupManager groupManager, UserManager userManager,
            final PermissionManager permissionManager,
            final FieldVisibilityManager fieldVisibilityManager, final UserHistoryManager userHistoryManager)
    {
        UserFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forReporter();
        final UserFitsNavigatorHelper userFitsNavigatorHelper = new UserFitsNavigatorHelper(userPickerSearchService);

        searcherInformation = new GenericSearcherInformation<SearchableField>(searchConstants.getSearcherId(), NAME_KEY, Collections.<Class<? extends FieldIndexer>>singletonList(ReporterIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        searchRenderer = new ReporterSearchRenderer(NAME_KEY, velocityRequestContextFactory, applicationProperties, templatingEngine, userPickerSearchService, userManager, permissionManager, fieldVisibilityManager);
        searchInputTransformer = new KickassUserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, groupManager, userManager, userHistoryManager);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }
}