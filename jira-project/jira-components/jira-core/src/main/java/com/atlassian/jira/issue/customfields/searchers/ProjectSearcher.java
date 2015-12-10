package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.ProjectCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.ProjectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ProjectCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.CustomFieldProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.permission.ProjectClauseValueSanitiser;
import com.atlassian.jira.jql.query.ProjectCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.jql.validator.ProjectValidator;
import com.atlassian.jira.jql.values.ProjectClauseValuesGenerator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class ProjectSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private final JqlOperandResolver jqlOperandResolver;
    private final ProjectConverter projectConverter;
    private final ProjectResolver projectResolver;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private I18nHelper.BeanFactory beanFactory;

    private final CustomFieldInputHelper customFieldInputHelper;
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private volatile ClauseNames clauseNames;

    public ProjectSearcher(final FieldVisibilityManager fieldVisibilityManager, final JqlOperandResolver jqlOperandResolver,
            ProjectConverter projectConverter, ProjectResolver projectResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry,
            ProjectManager projectManager, PermissionManager permissionManager, final CustomFieldInputHelper customFieldInputHelper)
    {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.projectConverter = projectConverter;
        this.projectResolver = projectResolver;
        this.fieldFlagOperandRegistry = fieldFlagOperandRegistry;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.beanFactory = ComponentAccessor.getI18nHelperFactory();
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(CustomField field)
    {
        this.clauseNames = field.getClauseNames();

        final ProjectCustomFieldIndexer indexer = new ProjectCustomFieldIndexer(fieldVisibilityManager, field, projectConverter);
        final ProjectIndexInfoResolver projectIndexInfoResolver = new ProjectIndexInfoResolver(projectResolver);
        final CustomFieldValueProvider customFieldValueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new CustomFieldRenderer(clauseNames, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new ProjectCustomFieldSearchInputTransformer(searcherInformation.getId(), clauseNames, field, jqlOperandResolver, projectIndexInfoResolver, fieldFlagOperandRegistry, projectResolver, customFieldInputHelper);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldClauseSanitiserValueGeneratingHandler(
                new ProjectValidator(projectResolver, jqlOperandResolver,permissionManager, projectManager, beanFactory),
                new ProjectCustomFieldClauseQueryFactory(field.getId(), projectResolver, jqlOperandResolver),
                new ProjectClauseValueSanitiser(permissionManager, jqlOperandResolver, projectResolver),
                new ProjectClauseValuesGenerator(permissionManager),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
                JiraDataTypes.PROJECT);
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
        return new CustomFieldProjectStatisticsMapper(projectManager, customField, customFieldInputHelper, ComponentAccessor.getJiraAuthenticationContext());
    }

    public StatisticsMapper getStatisticsMapper(CustomField customField)
    {
        return new CustomFieldProjectStatisticsMapper(projectManager, customField, customFieldInputHelper, ComponentAccessor.getJiraAuthenticationContext());
    }
}
