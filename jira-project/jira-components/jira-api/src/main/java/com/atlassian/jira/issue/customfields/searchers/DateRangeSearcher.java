package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.NaturallyOrderedCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.DateCustomFieldSearchRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.LocalDateIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.RelativeDateSearcherInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.LocalDateClauseQueryFactory;
import com.atlassian.jira.jql.util.JqlLocalDateSupport;
import com.atlassian.jira.jql.validator.LocalDateValidator;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.query.operator.Operator;
import com.atlassian.velocity.VelocityManager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

@PublicApi
@PublicSpi
public class DateRangeSearcher extends AbstractInitializationCustomFieldSearcher implements NaturallyOrderedCustomFieldSearcher, CustomFieldSearcher
{
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    private final JqlOperandResolver operandResolver;
    private final JqlLocalDateSupport jqlLocalDateSupport;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final VelocityRequestContextFactory velocityRenderContext;
    private final ApplicationProperties applicationProperties;
    private final VelocityTemplatingEngine templatingEngine;
    private final CalendarLanguageUtil calendarUtils;
    private final FieldVisibilityManager fieldVisibilityManager;

    public DateRangeSearcher
            (
                    final JqlOperandResolver operandResolver,
                    final JqlLocalDateSupport jqlLocalDateSupport,
                    final CustomFieldInputHelper customFieldInputHelper,
                    final DateTimeFormatterFactory dateTimeFormatterFactory,
                    final VelocityRequestContextFactory velocityRenderContext,
                    final ApplicationProperties applicationProperties,
                    final VelocityManager velocityManager,
                    final CalendarLanguageUtil calendarUtils,
                    final FieldVisibilityManager fieldVisibilityManager)
    {
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.jqlLocalDateSupport = notNull("jqlLocalDateSupport", jqlLocalDateSupport);
        this.velocityRenderContext = notNull("velocityRenderContext", velocityRenderContext);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.templatingEngine = getComponent(VelocityTemplatingEngine.class);
        this.calendarUtils = notNull("calendarUtils", calendarUtils);
        this.fieldVisibilityManager = notNull("fieldVisibilityManager" ,fieldVisibilityManager);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.dateTimeFormatterFactory = notNull("dateTimeFormatterFactory", dateTimeFormatterFactory);
    }


    @Override
    public void init(CustomField field)
    {
        final ClauseNames names = field.getClauseNames();

        final Set<Operator> supportedOperators = OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY;
        final SimpleFieldSearchConstants constants = new SimpleFieldSearchConstants(field.getId(), names, field.getId(),
                field.getId(), field.getId(), supportedOperators, JiraDataTypes.DATE);
        final FieldIndexer indexer = new LocalDateIndexer(fieldVisibilityManager, field);
        final String nameKey = "navigator.filter." + field.getId();
        final DateSearcherConfig config = new DateSearcherConfig(field.getId(), names, field.getName());

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchInputTransformer = new RelativeDateSearcherInputTransformer(config, operandResolver, jqlLocalDateSupport, customFieldInputHelper, dateTimeFormatterFactory);
        this.searchRenderer = createSearchRenderer(field, constants, nameKey, config);
        this.customFieldSearcherClauseHandler = new DateRangeSearcherClauseHandler(new LocalDateValidator(operandResolver, jqlLocalDateSupport),
                new LocalDateClauseQueryFactory(constants, jqlLocalDateSupport, operandResolver), supportedOperators);
    }



    DateCustomFieldSearchRenderer createSearchRenderer(final CustomField field, final SimpleFieldSearchConstants constants, final String nameKey, final DateSearcherConfig config)
    {
        return new DateCustomFieldSearchRenderer(false, field, constants, config, velocityRenderContext,
                applicationProperties, templatingEngine, calendarUtils, fieldVisibilityManager);
    }

    @Override
    public SearcherInformation<CustomField> getSearchInformation()
    {
         if (searcherInformation == null)
        {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer()
    {
        if (searchInputTransformer == null)
        {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer()
    {
        if (searchRenderer == null)
        {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }

    public LuceneFieldSorter getSorter(CustomField customField)
    {
        return null;
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
         if (customFieldSearcherClauseHandler == null)
        {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }

    @Override
    public String getSortField(CustomField customField)
    {
        return DocumentConstants.LUCENE_SORTFIELD_PREFIX + customField.getId();
    }
}
