package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.VersionPickerCustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.VersionPickerCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.VersionCustomFieldIndexer;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.CustomFieldVersionStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.ContextSetUtil;
import com.atlassian.jira.jql.context.CustomFieldClauseContextFactory;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.context.IntersectingClauseContextFactory;
import com.atlassian.jira.jql.context.VersionClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.VersionCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.jql.validator.VersionCustomFieldValidator;
import com.atlassian.jira.jql.values.VersionClauseValuesGenerator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * {@link CustomFieldSearcher} for custom fields that allow to pick versions.
 */
public class VersionPickerSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private final VersionManager versionManager;
    private final VersionResolver versionResolver;
    private final JqlOperandResolver operandResolver;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final FieldFlagOperandRegistry fieldFlagOperandRegistry;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private final ClauseContextFactory versionClauseContextFactory;
    private final PermissionManager permissionManager;
    private final ContextSetUtil contextSetUtil;
    private final FieldConfigSchemeClauseContextUtil clauseContextUtil;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final I18nHelper.BeanFactory beanFactory;
    private ProjectManager projectManager;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private ApplicationProperties applicationProperties;
    private VelocityTemplatingEngine templatingEngine;

    public VersionPickerSearcher(final VersionManager versionManager, final FieldVisibilityManager fieldVisibilityManager,
            final VersionResolver versionResolver, final JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry,
            final VersionClauseContextFactory versionClauseContextFactory, final PermissionManager permissionManager,
            final ContextSetUtil contextSetUtil, final FieldConfigSchemeClauseContextUtil clauseContextUtil,
            final CustomFieldInputHelper customFieldInputHelper,
            final ProjectManager projectManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties,
            final VelocityTemplatingEngine templatingEngine)
    {
        this.beanFactory = ComponentAccessor.getI18nHelperFactory();
        this.clauseContextUtil = notNull("clauseContextUtil", clauseContextUtil);
        this.contextSetUtil = notNull("contextSetUtil", contextSetUtil);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.versionClauseContextFactory = versionClauseContextFactory;
        this.versionManager = notNull("versionManager", versionManager);
        this.versionResolver = notNull("versionResolver", versionResolver);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.fieldVisibilityManager = notNull("fieldVisibilityManager", fieldVisibilityManager);
        this.fieldFlagOperandRegistry = notNull("fieldFlagOperandRegistry", fieldFlagOperandRegistry);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.projectManager = notNull("projectManager", projectManager);
        this.velocityRequestContextFactory = notNull("velocityRequestContextFactory", velocityRequestContextFactory);
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.templatingEngine = notNull("templatingEngine", templatingEngine);
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(CustomField field)
    {
        final VersionCustomFieldIndexer indexer = new VersionCustomFieldIndexer(fieldVisibilityManager, field);

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new VersionPickerCustomFieldRenderer(field, projectManager, versionManager, fieldVisibilityManager, velocityRequestContextFactory, applicationProperties, templatingEngine, permissionManager, searcherInformation.getNameKey());
        this.searchInputTransformer = new VersionPickerCustomFieldSearchInputTransformer(field, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldContextValueGeneratingClauseHandler(
                new VersionCustomFieldValidator(versionResolver, operandResolver, permissionManager, versionManager, beanFactory),
                new VersionCustomFieldClauseQueryFactory(field.getId(), versionResolver, operandResolver),
                new IntersectingClauseContextFactory(contextSetUtil, CollectionBuilder.newBuilder(
                        new CustomFieldClauseContextFactory(field, clauseContextUtil, ContextSetUtil.getInstance()),
                        versionClauseContextFactory).asList()),
                new VersionClauseValuesGenerator(versionManager, permissionManager, beanFactory),
                OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY,
                JiraDataTypes.VERSION);
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
        return new CustomFieldVersionStatisticsMapper(customField, versionManager, ComponentAccessor.getJiraAuthenticationContext(), customFieldInputHelper, false);
    }

    public StatisticsMapper getStatisticsMapper(CustomField customField)
    {
        return new CustomFieldVersionStatisticsMapper(customField, versionManager, ComponentAccessor.getJiraAuthenticationContext(), customFieldInputHelper, false);
    }
}
