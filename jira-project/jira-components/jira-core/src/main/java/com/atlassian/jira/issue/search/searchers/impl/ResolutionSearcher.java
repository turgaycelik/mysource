package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ResolutionIndexer;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.ResolutionSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.IssueConstantSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ResolutionIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ResolutionResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import java.util.Collections;

public class ResolutionSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    public static final TerminalClause UNRESOLVED_CLAUSE = new TerminalClauseImpl(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), Operator.IS, EmptyOperand.EMPTY);

    private final SearcherInformation<SearchableField> searchInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public ResolutionSearcher(FieldVisibilityManager fieldVisibilityManager,
            final ConstantsManager constantsManager,
            JqlOperandResolver operandResolver, VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, ResolutionResolver resolutionResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        final ResolutionIndexInfoResolver resolutionIndexInfoResolver = new ResolutionIndexInfoResolver(resolutionResolver);

        final SimpleFieldSearchConstants constants = SystemSearchConstants.forResolution();

        this.searchInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(), "navigator.filter.matchingresolutions",
                Collections.<Class<? extends FieldIndexer>>singletonList(ResolutionIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        this.searchInputTransformer = new IssueConstantSearchInputTransformer<Resolution>(constants.getJqlClauseNames(), resolutionIndexInfoResolver,
                operandResolver, fieldFlagOperandRegistry, resolutionResolver);
        this.searchRenderer = new ResolutionSearchRenderer(this.searchInformation.getNameKey(), constantsManager,
                velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager);
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
