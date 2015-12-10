package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.DefaultCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CascadingSelectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CascadingSelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CascadingSelectCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.context.CascadingSelectCustomFieldClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.CascadingSelectCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.ValidatingDecoratorQueryFactory;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.CascadingSelectCustomFieldValidator;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.jql.values.CustomFieldOptionsClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class CascadingSelectSearcher extends AbstractInitializationCustomFieldSearcher
    implements CustomFieldSearcher, CustomFieldStattable
{
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private volatile CustomFieldInputHelper customFieldInputHelper;
    private volatile ClauseNames clauseNames;

    private final ComponentLocator componentLocator;
    private final ComponentFactory componentFactory;

    public CascadingSelectSearcher(final ComponentLocator componentLocator, final ComponentFactory componentFactory)
    {
        this.componentLocator = notNull("componentLocator", componentLocator);
        this.componentFactory = notNull("componentFactory", componentFactory);
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(CustomField field)
    {
        final FieldVisibilityManager fieldVisibilityManager = componentLocator.getComponentInstanceOfType(FieldVisibilityManager.class);
        final SelectConverter selectConverter = componentLocator.getComponentInstanceOfType(SelectConverter.class);
        final JqlOperandResolver jqlOperandResolver = componentLocator.getComponentInstanceOfType(JqlOperandResolver.class);
        final JqlSelectOptionsUtil jqlSelectOptionsUtil = componentLocator.getComponentInstanceOfType(JqlSelectOptionsUtil.class);
        final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil = componentLocator.getComponentInstanceOfType(JqlCascadingSelectLiteralUtil.class);
        final QueryContextConverter queryContextConverter = componentLocator.getComponentInstanceOfType(QueryContextConverter.class);
        customFieldInputHelper = componentLocator.getComponentInstanceOfType(CustomFieldInputHelper.class);
        final OperatorUsageValidator usageValidator = componentLocator.getComponentInstanceOfType(OperatorUsageValidator.class);

        clauseNames = field.getClauseNames();
        final FieldIndexer indexer = new CascadingSelectCustomFieldIndexer(fieldVisibilityManager, field);
        final CustomFieldValueProvider customFieldValueProvider = new DefaultCustomFieldValueProvider();

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new CustomFieldRenderer(clauseNames, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new CascadingSelectCustomFieldSearchInputTransformer(clauseNames, field, searcherInformation.getId(), selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        ClauseQueryFactory queryFactory = new CascadingSelectCustomFieldClauseQueryFactory(field, field.getId(), jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);
        queryFactory = new ValidatingDecoratorQueryFactory(usageValidator, queryFactory);
        
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldContextValueGeneratingClauseHandler(
                componentFactory.createObject(CascadingSelectCustomFieldValidator.class, field),
                queryFactory,
                componentFactory.createObject(CascadingSelectCustomFieldClauseContextFactory.class, field),
                componentLocator.getComponentInstanceOfType(CustomFieldOptionsClauseValuesGenerator.class),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.CASCADING_OPTION);
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
        return new CascadingSelectStatisticsMapper(customField, selectConverter, ComponentAccessor.getJiraAuthenticationContext(), customFieldInputHelper);
    }
}
