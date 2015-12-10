package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.AssigneeIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.AssigneeSearchRenderer;
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

public class AssigneeSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    public static final String NAME_KEY = "navigator.filter.assignee";

    final SearcherInformation<SearchableField> searcherInformation;
    final SearchRenderer searchRenderer;
    final SearchInputTransformer searchInputTransformer;

    public AssigneeSearcher(
            VelocityRequestContextFactory velocityRequestContextFactory,
            VelocityTemplatingEngine templatingEngine,
            ApplicationProperties applicationProperties,
            UserPickerSearchService userPickerSearchService,
            FieldVisibilityManager fieldVisibilityManager,
            GroupManager groupManager, UserManager userManager,
            final PermissionManager permissionManager, final UserHistoryManager userHistoryManager)
    {
        UserFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forAssignee();
        final UserFitsNavigatorHelper userFitsNavigatorHelper = new UserFitsNavigatorHelper(userPickerSearchService);

        searcherInformation = new GenericSearcherInformation<SearchableField>(searchConstants.getSearcherId(), NAME_KEY, Collections.<Class<? extends FieldIndexer>>singletonList(AssigneeIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        searchRenderer = new AssigneeSearchRenderer(NAME_KEY, velocityRequestContextFactory, applicationProperties, templatingEngine, userPickerSearchService, fieldVisibilityManager, userManager, permissionManager);
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