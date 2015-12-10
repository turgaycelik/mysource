package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ProjectIdIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.ProjectSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.ProjectSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Collections;

/**
 * Searcher for the project system field.
 *
 * @since v4.0
 */
public class ProjectSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public ProjectSearcher(JqlOperandResolver operandResolver,
            ProjectResolver projectResolver, ProjectManager projectManager,
            PermissionManager permissionManager, VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine,
            FieldFlagOperandRegistry fieldFlagOperandRegistry, UserProjectHistoryManager projectHistoryManager,
            JiraAuthenticationContext authenticationContext)
    {
        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(projectResolver);
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forProject();

        this.searchInputTransformer = new ProjectSearchInputTransformer(projectIndexInfoResolver, operandResolver, fieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(), "common.concepts.project",
                Collections.<Class<? extends FieldIndexer>>singletonList(ProjectIdIndexer.class), fieldReference, SearcherGroupType.CONTEXT);
        this.searchRenderer = new ProjectSearchRenderer(projectManager, permissionManager,
                velocityRequestContextFactory, applicationProperties,
                templatingEngine, searcherInformation.getNameKey(), projectHistoryManager);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return this.searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return this.searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return this.searchRenderer;
    }

}
