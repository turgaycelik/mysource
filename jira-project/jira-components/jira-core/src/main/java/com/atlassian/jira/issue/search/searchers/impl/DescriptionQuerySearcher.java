package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DescriptionIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.GenericTextSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.TextQuerySearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Searcher for the Description field.
 * This maps to the LHS of JQL query "description ~ foo".
 *
 * @since v5.2
 */
public class DescriptionQuerySearcher implements IssueSearcher<SearchableField>
{

    private static final String NAME_KEY = "issue.field.description";

    private static final List<Class<? extends FieldIndexer>> SUMMARY_INDEXER = CollectionBuilder.<Class<? extends FieldIndexer>>newBuilder(DescriptionIndexer.class).asList();

    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;
    private final AtomicReference<SearchableField> fieldRef = new AtomicReference<SearchableField>();

    public DescriptionQuerySearcher(final JqlOperandResolver operandResolver, final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final FieldVisibilityManager fieldVisibilityManager)
    {

        final SimpleFieldSearchConstants fieldInfo = SystemSearchConstants.forDescription();
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(fieldInfo.getSearcherId(), NAME_KEY, SUMMARY_INDEXER, fieldRef, SearcherGroupType.TEXT);
        this.searchInputTransformer = new TextQuerySearchInputTransformer(fieldInfo.getSearcherId(), fieldInfo, operandResolver);
        this.searchRenderer = new GenericTextSearchRenderer(fieldInfo.getSearcherId(), NAME_KEY, fieldInfo.getFieldId(), velocityRequestContextFactory, applicationProperties, templatingEngine, searchInputTransformer, fieldVisibilityManager);
    }

    @Override
    public void init(SearchableField field)
    {
        fieldRef.set(field);
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
