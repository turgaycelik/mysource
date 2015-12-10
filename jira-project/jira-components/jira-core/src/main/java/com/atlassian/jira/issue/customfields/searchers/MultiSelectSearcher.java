package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.MultiSelectCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.MultiSelectCustomFieldSearchRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.MultiSelectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.MultiSelectCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.context.SelectCustomFieldClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.SelectCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.query.ValidatingDecoratorQueryFactory;
import com.atlassian.jira.jql.resolver.CustomFieldOptionResolver;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.jql.validator.SelectCustomFieldValidator;
import com.atlassian.jira.jql.values.CustomFieldOptionsClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class MultiSelectSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private volatile ClauseNames clauseNames;

    private final ComponentLocator componentLocator;
    private final ComponentFactory componentFactory;
    private CustomFieldInputHelper customFieldInputHelper;

    public MultiSelectSearcher(final ComponentLocator componentLocator, final ComponentFactory componentFactory)
    {
        this.componentFactory = notNull("componentFactory", componentFactory);
        this.componentLocator = notNull("componentLocator", componentLocator);
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(CustomField field)
    {
        clauseNames = field.getClauseNames();

        final FieldVisibilityManager fieldVisibilityManager = componentLocator.getComponentInstanceOfType(FieldVisibilityManager.class);
        final JqlOperandResolver jqlOperandResolver = componentLocator.getComponentInstanceOfType(JqlOperandResolver.class);
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = componentLocator.getComponentInstanceOfType(JqlSelectOptionsUtil.class);
        final QueryContextConverter queryContextConverter = componentLocator.getComponentInstanceOfType(QueryContextConverter.class);
        customFieldInputHelper = componentLocator.getComponentInstanceOfType(CustomFieldInputHelper.class);
        final MultiClauseDecoratorContextFactory.Factory multiFactory = componentLocator.getComponentInstanceOfType(MultiClauseDecoratorContextFactory.Factory.class);

        final FieldIndexer indexer = new MultiSelectCustomFieldIndexer(fieldVisibilityManager, field);
        final CustomFieldValueProvider customFieldValueProvider = new MultiSelectCustomFieldValueProvider();
        final OperatorUsageValidator usageValidator = componentLocator.getComponentInstanceOfType(OperatorUsageValidator.class);
        final CustomFieldOptionResolver customFieldOptionResolver = componentLocator.getComponentInstanceOfType(CustomFieldOptionResolver.class);

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new MultiSelectCustomFieldSearchRenderer(clauseNames, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager, jqlSelectOptionsUtil);
        this.searchInputTransformer = new MultiSelectCustomFieldSearchInputTransformer(searcherInformation.getId(), clauseNames, field, jqlOperandResolver, jqlSelectOptionsUtil, queryContextConverter, customFieldInputHelper);

        ClauseQueryFactory queryFactory = new SelectCustomFieldClauseQueryFactory(field, jqlSelectOptionsUtil, jqlOperandResolver, customFieldOptionResolver);
        queryFactory = new ValidatingDecoratorQueryFactory(usageValidator, queryFactory);

        this.customFieldSearcherClauseHandler = new SimpleCustomFieldContextValueGeneratingClauseHandler(
                componentFactory.createObject(SelectCustomFieldValidator.class, field),
                queryFactory,
                multiFactory.create(componentFactory.createObject(SelectCustomFieldClauseContextFactory.class, field), false),
                componentLocator.getComponentInstanceOfType(CustomFieldOptionsClauseValuesGenerator.class),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
                JiraDataTypes.OPTION);
    }

    public SearcherInformation<CustomField> getSearchInformation()
    {
        if (searcherInformation == null)
        {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        if (searchInputTransformer == null)
        {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        if (searchRenderer == null)
        {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
        if (customFieldSearcherClauseHandler == null)
        {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }

    public StatisticsMapper getStatisticsMapper(final CustomField customField)
    {
        if (clauseNames == null)
        {
            throw new IllegalStateException("Attempt to retrieve Statistics Mapper off uninitialised custom field searcher.");
        }
        final SelectConverter selectConverter = componentLocator.getComponentInstanceOfType(SelectConverter.class);
        return new SelectStatisticsMapper(customField, selectConverter, ComponentAccessor.getJiraAuthenticationContext(), customFieldInputHelper);
    }

    public LuceneFieldSorter getSorter(CustomField customField)
    {
        return getStatisticsMapper(customField);
    }
}
