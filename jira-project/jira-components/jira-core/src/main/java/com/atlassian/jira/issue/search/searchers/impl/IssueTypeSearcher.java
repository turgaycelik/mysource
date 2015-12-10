package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueTypeIndexer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.IssueTypeSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.IssueTypeSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Collections;

/**
 * Searcher for the {@link com.atlassian.jira.issue.fields.IssueTypeSystemField Issue Type} system field.
 */

public class IssueTypeSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;
    private final SearcherInformation<SearchableField> searchInformation;

    public IssueTypeSearcher(final ConstantsManager constantsManager,
            final JqlOperandResolver operandResolver, final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final IssueTypeResolver issueTypeResolver, final PermissionManager permissionsManager,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final OptionSetManager optionSetManager, final SubTaskManager subTaskManager,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry, final ProjectManager projectManager)
    {
        final IndexInfoResolver indexInfoResolver = new IssueConstantInfoResolver<IssueType>(issueTypeResolver);
        final SimpleFieldSearchConstants constants = SystemSearchConstants.forIssueType();

        this.searchInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(), "navigator.filter.issuetype",
                Collections.<Class<? extends FieldIndexer >>singletonList(IssueTypeIndexer.class), fieldReference, SearcherGroupType.CONTEXT);
        this.searchInputTransformer = new IssueTypeSearchInputTransformer(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, issueTypeResolver);
        this.searchRenderer = new IssueTypeSearchRenderer(applicationProperties, constantsManager, issueTypeSchemeManager, optionSetManager, permissionsManager, constants, searchInformation.getNameKey(), subTaskManager, templatingEngine, velocityRequestContextFactory);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return this.searchInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return this.searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }
}