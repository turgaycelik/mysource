package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
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
import com.atlassian.jira.issue.search.searchers.transformer.DateSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;

import java.util.Collections;

/**
 * A simple class that most date searchers will be able to extends to implement searching. 
 *
 * @since v4.0
 */
public class AbstractDateSearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public AbstractDateSearcher(
            final SimpleFieldSearchConstants constants,
            final String nameKey, final Class<? extends BaseFieldIndexer> indexer,
            final DateConverter dateConverter,
            final DateTimeConverter dateTimeConverter,
            final JqlDateSupport dateSupport,
            final JqlOperandResolver operandResolver,
            final ApplicationProperties applicationProperties,
            final VelocityRequestContextFactory velocityRenderContext,
            final VelocityTemplatingEngine templatingEngine,
            final CalendarLanguageUtil calendarUtils,
            final FieldVisibilityManager fieldVisibilityManager,
            final CustomFieldInputHelper customFieldInputHelper,
            final TimeZoneManager timeZoneManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        final DateSearcherConfig config = new DateSearcherConfig(constants.getUrlParameter(), constants.getJqlClauseNames(), constants.getJqlClauseNames().getPrimaryName());
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(constants.getSearcherId(),
                nameKey, Collections.<Class<? extends FieldIndexer>>singletonList(indexer),
                fieldReference, SearcherGroupType.DATE);
        this.searchInputTransformer = new DateSearchInputTransformer(false, config, dateConverter, dateTimeConverter, operandResolver, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
        this.searchRenderer = new DateSearchRenderer(constants, config, nameKey, velocityRenderContext,
                applicationProperties, templatingEngine, calendarUtils, fieldVisibilityManager);
    }

    public final SearcherInformation<SearchableField> getSearchInformation()
    {
        return searcherInformation;
    }

    public final SearchInputTransformer getSearchInputTransformer()
    {
        return searchInputTransformer;
    }

    public final SearchRenderer getSearchRenderer()
    {
        return searchRenderer;
    }

}
