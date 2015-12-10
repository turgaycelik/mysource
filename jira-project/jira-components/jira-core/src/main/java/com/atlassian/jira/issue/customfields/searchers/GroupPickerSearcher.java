package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.GroupCustomFieldSearchRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.MultiGroupCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.customfields.statistics.GroupPickerStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.GroupCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.util.GroupCustomFieldIndexValueConverter;
import com.atlassian.jira.jql.validator.GroupCustomFieldValidator;
import com.atlassian.jira.jql.values.GroupValuesGenerator;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class GroupPickerSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private volatile ClauseNames clauseNames;
    private final GroupConverter groupConverter;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private GroupManager groupManager;
    private final GroupValuesGenerator groupValuesGenerator;
    private final CustomFieldInputHelper customFieldInputHelper;

    public GroupPickerSearcher(GroupConverter groupConverter, FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver,
            GroupManager groupManager, GroupValuesGenerator groupValuesGenerator, final CustomFieldInputHelper customFieldInputHelper)
    {
        this.groupConverter = groupConverter;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.groupManager = groupManager;
        this.groupValuesGenerator = groupValuesGenerator;
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(final CustomField field)
    {
        clauseNames = field.getClauseNames();

        final FieldIndexer indexer = new GroupCustomFieldIndexer(fieldVisibilityManager, field, groupConverter);
        final GroupCustomFieldIndexValueConverter fieldIndexValueConverter = new GroupCustomFieldIndexValueConverter(groupConverter);

        final CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new GroupCustomFieldSearchRenderer(clauseNames, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new MultiGroupCustomFieldSearchInputTransformer(searcherInformation.getId(), clauseNames, field, jqlOperandResolver, customFieldInputHelper, groupConverter);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(new GroupCustomFieldValidator(jqlOperandResolver, fieldIndexValueConverter),
                new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, fieldIndexValueConverter, false), groupValuesGenerator,
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.GROUP);
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
        return new GroupPickerStatisticsMapper(customField, groupManager, ComponentAccessor.getJiraAuthenticationContext(), customFieldInputHelper);
    }

    public LuceneFieldSorter getSorter(final CustomField customField)
    {
        return new GroupPickerStatisticsMapper(customField, groupManager, ComponentAccessor.getJiraAuthenticationContext(), customFieldInputHelper);
    }
}
