package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.DateSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.RelativeDateSearcherInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;

import java.util.Collections;

/**
 *
 * @since v4.4
 */
public class AbstractRelativeDateSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public AbstractRelativeDateSearcher
            (
                    final SimpleFieldSearchConstants constants,
                    final String nameKey,
                    final Class<? extends BaseFieldIndexer> indexer,
                    final JqlOperandResolver operandResolver,
                    final ApplicationProperties applicationProperties,
                    final VelocityRequestContextFactory velocityRenderContext,
                    final VelocityTemplatingEngine templatingEngine,
                    final CalendarLanguageUtil calendarUtils,
                    final FieldVisibilityManager fieldVisibilityManager,
                    final CustomFieldInputHelper customFieldInputHelper,
                    final JqlLocalDateSupport jqlLocalDateSupport,
                    final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        final DateSearcherConfig config = new DateSearcherConfig(constants.getUrlParameter(), constants.getJqlClauseNames(), constants.getJqlClauseNames().getPrimaryName());
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(),
                nameKey, Collections.<Class<? extends FieldIndexer>>singletonList(indexer),
                fieldReference, SearcherGroupType.DATE);
        this.searchInputTransformer = new RelativeDateSearcherInputTransformer(config, operandResolver, jqlLocalDateSupport, customFieldInputHelper, dateTimeFormatterFactory);
        this.searchRenderer = new DateSearchRenderer(constants, config, nameKey, velocityRenderContext,
                applicationProperties, templatingEngine, calendarUtils, fieldVisibilityManager);
    }


    @Override
    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer()
    {
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }
}
