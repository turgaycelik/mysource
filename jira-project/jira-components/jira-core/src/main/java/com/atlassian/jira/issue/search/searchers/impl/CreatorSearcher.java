package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CreatorIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.CreatorSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.KickassUserSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

/**
 * Class is responsible for searching for Creators
 *
 * @since v6.2
 */
public class CreatorSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    public static final String NAME_KEY = "navigator.filter.createdby";

    final SearcherInformation<SearchableField> searcherInformation;
    final SearchRenderer searchRenderer;
    final SearchInputTransformer searchInputTransformer;

    public CreatorSearcher(
            VelocityRequestContextFactory velocityRequestContextFactory,
            VelocityTemplatingEngine templatingEngine,
            ApplicationProperties applicationProperties,
            UserPickerSearchService userPickerSearchService,
            GroupManager groupManager, UserManager userManager,
            final PermissionManager permissionManager,
            final FieldVisibilityManager fieldVisibilityManager, final UserHistoryManager userHistoryManager)
    {
        UserFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forCreator();
        final UserFitsNavigatorHelper userFitsNavigatorHelper = new UserFitsNavigatorHelper(userPickerSearchService);

        searcherInformation = new GenericSearcherInformation<SearchableField>(searchConstants.getSearcherId(), NAME_KEY, Collections.<Class<? extends FieldIndexer>>singletonList(CreatorIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        searchRenderer = new CreatorSearchRenderer(NAME_KEY, velocityRequestContextFactory, applicationProperties, templatingEngine, userPickerSearchService, userManager, permissionManager, fieldVisibilityManager);
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