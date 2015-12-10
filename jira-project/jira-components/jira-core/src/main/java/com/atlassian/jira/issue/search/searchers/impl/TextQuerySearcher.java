package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DescriptionIndexer;
import com.atlassian.jira.issue.index.indexers.impl.EnvironmentIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SummaryIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.TextQuerySearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.TextQuerySearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Searcher for the multi-field text search input introduced 2012 as a gradual replacement for the orignal QuerySearcher.
 * This maps to the LHS of JQL query "text ~ foo" and it searches for multiple fields.
 *
 * @since v5.2
 */
public class TextQuerySearcher implements IssueSearcher<SearchableField>
{

    private static final String ID = "text";
    private static final String NAME_KEY = "common.words.query";

    private static final List<Class<? extends FieldIndexer>> CLASSIC_FIELD_INDEXERS = CollectionBuilder.<Class<? extends FieldIndexer>>newBuilder(DescriptionIndexer.class, SummaryIndexer.class, EnvironmentIndexer.class).asList();

    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public TextQuerySearcher(JqlOperandResolver operandResolver, VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine)
    {
        AtomicReference<SearchableField> fieldRef = new AtomicReference<SearchableField>();
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(ID, NAME_KEY, CLASSIC_FIELD_INDEXERS, fieldRef, SearcherGroupType.TEXT);
        this.searchInputTransformer = new TextQuerySearchInputTransformer(ID, SystemSearchConstants.forAllText(), operandResolver);
        this.searchRenderer = new TextQuerySearchRenderer(ID, ID, NAME_KEY, velocityRequestContextFactory, applicationProperties, templatingEngine, searchInputTransformer);
    }

    @Override
    public void init(SearchableField field)
    {
        // not supposed to have a field
        throw new UnsupportedOperationException("This searcher not supposed to have a field");
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
