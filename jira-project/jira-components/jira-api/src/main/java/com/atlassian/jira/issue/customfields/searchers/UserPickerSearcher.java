package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.UserCustomFieldSearchRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.UserPickerCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.UserCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.statistics.CustomFieldUserStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.UserCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.validator.UserCustomFieldValidator;
import com.atlassian.jira.jql.values.UserClauseValuesGenerator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@PublicSpi
@PublicApi
public class UserPickerSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private final JqlOperandResolver operandResolver;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JiraAuthenticationContext context;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    private final UserConverter userConverter;
    private final UserResolver userResolver;
    private final UserPickerSearchService userPickerSearchService;
    private UserManager userManager;
    private CustomFieldInputHelper customFieldInputHelper;
    private I18nHelper.BeanFactory beanFactory;

    public UserPickerSearcher(final UserResolver userResolver,
            final JqlOperandResolver operandResolver,
            final JiraAuthenticationContext context,
            final UserConverter userConverter,
            final UserPickerSearchService userPickerSearchService,
            final CustomFieldInputHelper customFieldInputHelper,
            final UserManager userManager,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        this.beanFactory = ComponentAccessor.getI18nHelperFactory();
        this.userPickerSearchService = userPickerSearchService;
        this.userManager = userManager;
        this.userResolver = notNull("userResolver", userResolver);
        this.userConverter = notNull("userConverter", userConverter);
        this.context = notNull("context", context);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.fieldVisibilityManager = notNull("fieldVisibilityManager", fieldVisibilityManager);
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(final CustomField field)
    {
        final UserCustomFieldIndexer indexer = new UserCustomFieldIndexer(fieldVisibilityManager, field, userConverter);
        final UserFitsNavigatorHelper userFitsNavigatorHelper = new UserFitsNavigatorHelper(userPickerSearchService);
        final ClauseNames names = field.getClauseNames();

        final CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new UserCustomFieldSearchRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new UserPickerCustomFieldSearchInputTransformer(searcherInformation.getId(), names, field, userConverter, userFitsNavigatorHelper, customFieldInputHelper);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(new UserCustomFieldValidator(userResolver, operandResolver, beanFactory),
                        new UserCustomFieldClauseQueryFactory(field.getId(), userResolver, operandResolver),
                        new UserClauseValuesGenerator(userPickerSearchService),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
                JiraDataTypes.USER);
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
        return new CustomFieldUserStatisticsMapper(customField, userManager, context, customFieldInputHelper);
    }

    public StatisticsMapper getStatisticsMapper(CustomField customField)
    {
        return new CustomFieldUserStatisticsMapper(customField, userManager, context, customFieldInputHelper);
    }
}
