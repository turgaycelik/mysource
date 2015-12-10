package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.NaturallyOrderedCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.ExactTextCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ExactTextCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.jql.util.SimpleIndexValueConverter;
import com.atlassian.jira.jql.validator.ExactTextCustomFieldValidator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A searcher for custom fields that contain exact text (e.g. url)
 */
@PublicSpi
@PublicApi
public class ExactTextSearcher  extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, NaturallyOrderedCustomFieldSearcher
{
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final CustomFieldInputHelper customFieldInputHelper;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    /**
     * Old Constructor - deprecated.
     * @param jqlOperandResolver
     * @param customFieldInputHelper
     *
     * @deprecated Use {@link #ExactTextSearcher(JqlOperandResolver, CustomFieldInputHelper, FieldVisibilityManager)} instead. Since v4.4.
     */
    public ExactTextSearcher(final JqlOperandResolver jqlOperandResolver, final CustomFieldInputHelper customFieldInputHelper)
    {
        this(jqlOperandResolver, customFieldInputHelper, ComponentAccessor.getComponent(FieldVisibilityManager.class));
    }

    public ExactTextSearcher(JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper, FieldVisibilityManager fieldVisibilityManager)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
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
        final FieldIndexer indexer = new ExactTextCustomFieldIndexer(fieldVisibilityManager, field);
        final IndexValueConverter indexValueConverter = new SimpleIndexValueConverter(false);

        final CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new CustomFieldRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new ExactTextCustomFieldSearchInputTransformer(field, names, searcherInformation.getId(), customFieldInputHelper);

        final Set<Operator> supportedOperators = OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY;
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(new ExactTextCustomFieldValidator(),
                       new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, indexValueConverter, false),
                supportedOperators, JiraDataTypes.TEXT);
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
        return new TextFieldSorter(customField.getId());
    }

    @Override
    public String getSortField(CustomField customField)
    {
        return DocumentConstants.LUCENE_SORTFIELD_PREFIX + customField.getId();
    }
}
