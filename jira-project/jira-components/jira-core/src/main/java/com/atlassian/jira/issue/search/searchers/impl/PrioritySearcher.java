package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.PriorityIndexer;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.PrioritySearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.IssueConstantSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.PriorityResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

public class PrioritySearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearcherInformation<SearchableField> searchInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public PrioritySearcher(FieldVisibilityManager fieldVisibilityManager,
            final ConstantsManager constantsManager,
            JqlOperandResolver operandResolver, VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, PriorityResolver priorityResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        final IssueConstantInfoResolver<Priority> constantInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);

        final SimpleFieldSearchConstants constants = SystemSearchConstants.forPriority();

        this.searchInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(), "navigator.filter.matchingpriorities", Collections.<Class<? extends FieldIndexer>>singletonList(PriorityIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        this.searchInputTransformer = new IssueConstantSearchInputTransformer<Priority>(constants.getJqlClauseNames(), constantInfoResolver, operandResolver, fieldFlagOperandRegistry, priorityResolver);
        this.searchRenderer = new PrioritySearchRenderer(searchInformation.getNameKey(), constantsManager,
                velocityRequestContextFactory, applicationProperties, templatingEngine,
                fieldVisibilityManager);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return searchInformation;
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
