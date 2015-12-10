package com.atlassian.jira.dev.reference.plugin.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.ExactTextCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.CustomFieldLabelsStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.jql.util.SimpleIndexValueConverter;
import com.atlassian.jira.jql.validator.ExactTextCustomFieldValidator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Referemce class that implements a custom field searcher - in this case for labels.
 *
 * @since v4.3
 */
public class LabelSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private SearcherInformation<CustomField> searcherInformation;
    private SearchInputTransformer searchInputTransformer;
    private SearchRenderer searchRenderer;
    final private FieldVisibilityManager fieldVisibilityManager;
    final private JqlOperandResolver jqlOperandResolver;
    final private CustomFieldInputHelper customFieldInputHelper;
    private CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private final JiraAuthenticationContext authenticationContext;

    public LabelSearcher(final JqlOperandResolver jqlOperandResolver, final CustomFieldInputHelper customFieldInputHelper,
            final JiraAuthenticationContext authenticationContext, final FieldVisibilityManager fieldVisibilityManager)
    {
           this.fieldVisibilityManager = fieldVisibilityManager;
           this.jqlOperandResolver = jqlOperandResolver;
           this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
           this.authenticationContext = notNull("authenticationCotext",authenticationContext);
    }

    public void init(CustomField field)
    {

        final ClauseNames names = field.getClauseNames();
        final FieldIndexer indexer = new LabelsCustomFieldIndexer(fieldVisibilityManager, field);
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

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
        return customFieldSearcherClauseHandler;
    }

    public LuceneFieldSorter getSorter(CustomField customField)
    {
        return new TextFieldSorter(customField.getId());
    }

    public StatisticsMapper getStatisticsMapper(CustomField customField)
    {
        return new CustomFieldLabelsStatisticsMapper(customField, customFieldInputHelper, authenticationContext, false);
    }
}

