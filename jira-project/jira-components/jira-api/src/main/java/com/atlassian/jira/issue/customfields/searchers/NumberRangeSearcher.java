package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.NumberRangeCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.NumberRangeCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.NumberCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.NumericFieldStatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.util.NumberIndexValueConverter;
import com.atlassian.jira.jql.validator.NumberCustomFieldValidator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@PublicSpi
@PublicApi
public class NumberRangeSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher
{
    public static final String LESS_THAN_PARAM = "lessThan";
    public static final String GREATER_THAN_PARAM = "greaterThan";

    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final DoubleConverter doubleConverter;
    private final CustomFieldInputHelper customFieldInputHelper;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private final I18nHelper.BeanFactory beanFactory;

    public NumberRangeSearcher(final JqlOperandResolver jqlOperandResolver, final DoubleConverter doubleConverter,
            final CustomFieldInputHelper customFieldInputHelper, I18nHelper.BeanFactory beanFactory, FieldVisibilityManager fieldVisibilityManager)
    {
        this.beanFactory = notNull("beanFactory", beanFactory);
        this.fieldVisibilityManager = notNull("fieldVisibilityManager", fieldVisibilityManager);
        this.jqlOperandResolver = jqlOperandResolver;
        this.doubleConverter = doubleConverter;
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(CustomField field)
    {
        final ClauseNames names = field.getClauseNames();
        final NumberIndexValueConverter indexValueConverter = new NumberIndexValueConverter(doubleConverter);
        final FieldIndexer indexer = new NumberCustomFieldIndexer(fieldVisibilityManager, field, doubleConverter);
        final CustomFieldValueProvider customFieldValueProvider = new NumberRangeCustomFieldValueProvider();

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new CustomFieldRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new NumberRangeCustomFieldSearchInputTransformer(names, field, searcherInformation.getId(), doubleConverter, jqlOperandResolver, customFieldInputHelper);

        final Set<Operator> supportedOperators = OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY;
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(new NumberCustomFieldValidator(jqlOperandResolver, indexValueConverter, beanFactory),
                        new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, indexValueConverter, true),
                supportedOperators, JiraDataTypes.NUMBER);
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

    public LuceneFieldSorter getSorter(CustomField customField)
    {
        return new NumericFieldStatisticsMapper(customField.getId());
    }
}
