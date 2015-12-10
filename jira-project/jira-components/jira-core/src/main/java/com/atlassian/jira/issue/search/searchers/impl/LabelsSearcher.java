package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.LabelsIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.LabelsSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.LabelsSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.LabelIndexInfoResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;

/**
 * A Searcher for the Labels system field.
 *
 * @since v4.2
 */
public class LabelsSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    final private SearcherInformation<SearchableField> searcherInformation;
    final private SearchInputTransformer searchInputTransformer;
    final private SearchRenderer searchRenderer;

    public LabelsSearcher(VelocityTemplatingEngine templatingEngine, VelocityRequestContextFactory contextFactory, FieldVisibilityManager fieldVisibilityManager,
            ApplicationProperties applicationProperties, JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry)
    {
        final SimpleFieldSearchConstantsWithEmpty constants = SystemSearchConstants.forLabels();

        searcherInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(), "common.filters.labels",
                Collections.<Class<? extends FieldIndexer>>singletonList(LabelsIndexer.class), fieldReference, SearcherGroupType.ISSUE);
        searchRenderer = new LabelsSearchRenderer(constants, contextFactory, fieldVisibilityManager, applicationProperties, templatingEngine, searcherInformation.getNameKey());
        searchInputTransformer = new LabelsSearchInputTransformer(new LabelIndexInfoResolver(false), operandResolver, fieldFlagOperandRegistry);
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
