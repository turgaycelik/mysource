package com.atlassian.jira.issue.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.admin.RenderableProperty;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.event.issue.field.CustomFieldDeletedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.NaturallyOrderedCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.LabelsCFType;
import com.atlassian.jira.issue.customfields.impl.VersionCFType;
import com.atlassian.jira.issue.customfields.impl.rest.TextCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldClauseContextHandler;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldClauseSanitiserHandler;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.customfields.view.NullCustomFieldParams;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.IssueComparator;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.constants.DefaultClauseInformation;
import com.atlassian.jira.issue.search.parameters.lucene.sort.DefaultIssueSortComparator;
import com.atlassian.jira.issue.search.parameters.lucene.sort.DocumentSortComparatorSource;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.DefaultValuesGeneratingClauseHandler;
import com.atlassian.jira.jql.NoOpClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.ContextSetUtil;
import com.atlassian.jira.jql.context.CustomFieldClauseContextFactory;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.CustomFieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptors;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptors;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NameComparator;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.jira.web.bean.DefaultBulkMoveHelper;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.collect.ImmutableList;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;
import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default CustomField implementation backed by the database (a GenericValue object).
 * Usually managed via {@link CustomFieldManager}.
 */
public class CustomFieldImpl implements CustomField
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    private static final Logger log = Logger.getLogger(CustomFieldImpl.class);

    /**
     * Name of the parameter that stores the issue id in the current context in CustomFieldParams.
     * This is useful for getting data such as the previous value.
     *
     * @since v6.2.2
     */
    public static String getParamKeyIssueId()
    {
        return CustomFieldUtils.getParamKeyPrefixAtl() + "issue_id";
    }

    /**
     * Name of the parameter that stores the project id of the associated issue in the current context in CustomFieldParams.
     *
     * @since v6.2.2
     */
    public static String getParamKeyProjectId()
    {
        return CustomFieldUtils.getParamKeyPrefixAtl() + "project_id";
    }

    private final GenericValue gv;

    /** Property set that holds translations for the name and description. */
    private final LazyReference<PropertySet> propertySetRef = new LazyReference<PropertySet>()
    {
        @Override
        protected PropertySet create()
        {
            return OFBizPropertyUtils.getCachingPropertySet(gv);

        }
    };

    /**
     * Lazily-initialised type for this CustomFieldImpl.
     */
    private LazyReference<CustomFieldType> typeRef;

    /**
     * Lazily-initialised searcher for this CustomFieldImpl.
     */
    private ResettableLazyReference<CustomFieldSearcher> searcherRef;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final JiraAuthenticationContext authenticationContext;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final PermissionManager permissionManager;
    private final RendererManager rendererManager;
    private final FieldConfigSchemeClauseContextUtil contextUtil;
    private final CustomFieldDescription customFieldDescription;
    private final FeatureManager featureManager;
    private final TranslationManager translationManager;
    private final CustomFieldScopeFactory scopeFactory;
    private final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors;
    private final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors;

    private final String customFieldId;
    private final EventPublisher eventPublisher;

    // ---------------------------------------------------------------------------------------------------- Constructors
    CustomFieldImpl(final GenericValue customField,
            final JiraAuthenticationContext authenticationContext,
            final FieldConfigSchemeManager fieldConfigSchemeManager,
            final PermissionManager permissionManager,
            final RendererManager rendererManager,
            final FieldConfigSchemeClauseContextUtil contextUtil,
            final CustomFieldDescription customFieldDescription,
            final FeatureManager featureManager,
            final TranslationManager translationManager,
            final CustomFieldScopeFactory scopeFactory,
            final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors,
            final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors)
    {
        this.gv = customField;
        this.authenticationContext = authenticationContext;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.permissionManager = permissionManager;
        this.rendererManager = rendererManager;
        this.contextUtil = contextUtil;
        this.customFieldDescription = customFieldDescription;
        this.translationManager = translationManager;
        this.eventPublisher = getEventPublisher();
        this.typeRef = new CustomFieldTypeLazyRef();
        this.searcherRef = new CustomFieldSearcherLazyRef();
        this.featureManager = featureManager;
        this.scopeFactory = scopeFactory;
        this.customFieldTypeModuleDescriptors = customFieldTypeModuleDescriptors;
        this.customFieldSearcherModuleDescriptors = customFieldSearcherModuleDescriptors;

        customFieldId = FieldManager.CUSTOM_FIELD_PREFIX + gv.getLong(ENTITY_ID);
    }

    /**
     * Creates a new CustomFieldImpl from an existing CustomFieldImpl (aka copy constructor).
     *
     * @param customField a CustomFieldImpl
     */
    CustomFieldImpl(final CustomField customField,
            final JiraAuthenticationContext authenticationContext,
            final FieldConfigSchemeManager fieldConfigSchemeManager,
            final PermissionManager permissionManager,
            final RendererManager rendererManager,
            final FieldConfigSchemeClauseContextUtil contextUtil,
            final CustomFieldDescription customFieldDescription,
            final FeatureManager featureManager,
            final TranslationManager translationManager,
            final CustomFieldScopeFactory scopeFactory,
            final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors,
            final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors)
    {
        this(
                new GenericValue(customField.getGenericValue()),
                authenticationContext,
                fieldConfigSchemeManager,
                permissionManager,
                rendererManager,
                contextUtil,
                customFieldDescription,
                featureManager,
                translationManager,
                scopeFactory,
                customFieldTypeModuleDescriptors,
                customFieldSearcherModuleDescriptors
        );

        typeRef = new CustomFieldTypeLazyRef(customField.getCustomFieldType());
        searcherRef = new CustomFieldSearcherLazyRef(customField.getCustomFieldSearcher());
    }

    // --------------------------------------------------------------------------------------------- Persistance Methods

    /**
     * Stores the generic value of this custom field
     *
     * @throws DataAccessException if error of storing the generic value occurs
     * @deprecated Use {@link com.atlassian.jira.issue.CustomFieldManager#updateCustomField(CustomField)} instead. Since v6.2.
     */
    @Deprecated
    @Override
    public void store() throws DataAccessException
    {
        ComponentAccessor.getCustomFieldManager().updateCustomField(this);
    }

    // --------------------------------------------------------------------------------- Configuration & Schemes & Scope
    @Override
    public boolean isInScope(Project project, List<String> issueTypeIds)
    {
        List<IssueContext> issueContexts = CustomFieldUtils.convertToIssueContexts(project, issueTypeIds);
        return isInScope(issueContexts);
    }

    @Override
    public boolean isInScopeForSearch(final Project project, final List<String> issueTypeIds)
    {
        List<IssueContext> issueContexts = CustomFieldUtils.convertToIssueContexts(project, issueTypeIds);
        return isInScopeOfAtLeastOneIssueContext(issueContexts);
    }

    private boolean isInScopeOfAtLeastOneIssueContext(List<IssueContext> issueContexts)
    {
        CustomFieldScope customFieldScope = scopeFactory.createFor(this);
        for (final IssueContext issueContext : issueContexts)
        {
            if (customFieldScope.isIncludedIn(issueContext))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @Deprecated
    public final boolean isInScope(GenericValue project, List issueTypeIds)
    {
        List<IssueContext> issueContexts = CustomFieldUtils.convertToIssueContexts(project, issueTypeIds);
        return isInScope(issueContexts);
    }

    private boolean isInScope(final List<IssueContext> issueContexts)
    {
        for (final IssueContext issueContext : issueContexts)
        {
            final FieldConfig relevantConfig = getRelevantConfig(issueContext);
            if (relevantConfig != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the relevant field config for the search context specified.
     * Checks that all configs within search context are the same - i.e. all null or all the same config.
     * <p/>
     * Returns null if any two configs are different.
     * <p/>
     * Note: null config is not equal to non-null config. Previously, a non-null config was returned even if the first
     * config(s) was null.
     *
     * @param searchContext search context
     * @return null if any two configs are different
     */
    @Override
    public FieldConfig getReleventConfig(SearchContext searchContext)
    {
        List<IssueContext> issueContexts = searchContext.getAsIssueContexts();
        FieldConfig config = null;
        boolean firstRun = true;
        for (final IssueContext issueContext : issueContexts)
        {
            final FieldConfig relevantConfig = getRelevantConfig(issueContext);

            // Grab the first config - we will compare all other configs with this one.
            if (firstRun)
            {
                config = relevantConfig;
                firstRun = false;
            }
            // Compare the configurations - return the text if the configs are different
            else if (areDifferent(config, relevantConfig))
            {
                log.debug("Different configs found for search context. No configs are returned for " + getName());
                return null;
            }
        }

        return config;
    }

    @Override
    public ClauseNames getClauseNames()
    {
        return ClauseNames.forCustomField(this);
    }

    @Override
    public boolean isInScope(SearchContext searchContext)
    {
        return getReleventConfig(searchContext) != null;
    }

    /**
     * Determines whether this custom field is in scope.
     * The custom field is in scope if there is a relevant config for given search context.
     *
     * @param user          not used
     * @param searchContext search context
     * @return true if this field has a relevant config for given search context
     *
     * @deprecated The user parameter is ignored. Please call {@link #isInScope(SearchContext)}}. Since v4.3
     */
    @Override
    public final boolean isInScope(User user, SearchContext searchContext)
    {
        return getReleventConfig(searchContext) != null;
    }

    // --------------------------------------------------------------- Methods forwarded to Custom Field Type & Searcher

    /**
     * Validates relevant parameters on custom field type of this custom field. Any errors found are added to the given
     * errorCollection.
     * See {@link CustomFieldType#validateFromParams(CustomFieldParams,ErrorCollection,FieldConfig)}
     *
     * @param actionParameters action parameters
     * @param errorCollection  error collection to add errors to
     * @param config           field config
     */
    @Override
    public void validateFromActionParams(Map actionParameters, ErrorCollection errorCollection, FieldConfig config)
    {
        final CustomFieldParams relevantParams = getRelevantParams(actionParameters);
        getCustomFieldType().validateFromParams(relevantParams, errorCollection, config);
    }

    /**
     * Retrieves and returns the Object representing the this CustomField value for the given issue.
     * See {@link CustomFieldType#getValueFromIssue(CustomField,Issue)}
     *
     * @param issue issue to retrieve the value from
     * @return Object representing the this CustomField value for the given issue
     */
    @Override
    public Object getValue(Issue issue)
    {
        return getCustomFieldType().getValueFromIssue(this, issue);
    }

    /**
     * This is the conjunction point with CustomFieldTypes and this is delegated off to customField Types.
     *
     * @return true if the custom field supports interaction with the renderers, false otherwise. Text based
     *         fields will be able to interact with the renderers.
     */
    @Override
    public boolean isRenderable()
    {
        return getCustomFieldType().isRenderable();
    }

    /**
     * Returns the given value as a string. This can be a one-way transformation. That is, there is no requirement that
     * the value returned by this method can be used to reconstruct the value.
     * <p/>
     * For example, for {@link com.atlassian.jira.issue.customfields.impl.VersionCFType} it returns a comma-separated
     * list of version IDs as a string.
     *
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return change log value
     */
    @Nullable
    private String getChangelogValue(Object value)
    {
        return getCustomFieldType().getChangelogValue(this, value);
    }

    /**
     * Returns the given value as a string. This can be a one-way transformation. That is, there is no requirement that
     * the value returned by this method can be used to reconstruct the value.
     * <p/>
     * For example, for {@link com.atlassian.jira.issue.customfields.impl.VersionCFType} it returns a comma-separated
     * list of version names as a string.
     *
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return change log value
     */
    @Nullable
    private String getChangelogString(Object value)
    {
        return getCustomFieldType().getChangelogString(this, value);
    }

    @Override
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        ChangeItemBean changeItemBean = updateValue(fieldLayoutItem, issue, modifiedValue.getNewValue());
        if (changeItemBean != null)
        {
            issueChangeHolder.addChangeItem(changeItemBean);
        }
    }

    private ChangeItemBean updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, Object newValue)
    {
        Object existingValue = getValue(issue);

        ChangeItemBean cib = null;

        if (existingValue == null)
        {
            if (newValue != null)
            {
                newValue = processValueThroughRenderer(fieldLayoutItem, newValue);
                createValue(issue, newValue);
                String changelogValue = getChangelogValue(newValue);
                if (changelogValue != null)
                {
                    String changelogString = getChangelogString(newValue);
                    // If the changelogString is null then fall back to the changelogValue
                    if (changelogString == null)
                    {
                        changelogString = changelogValue;
                        changelogValue = null;
                    }

                    cib = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD, getNameKey(), null, null, changelogValue, changelogString);
                }
            }
        }
        else
        {
            if (!valuesEqual(existingValue, newValue))
            {
                newValue = processValueThroughRenderer(fieldLayoutItem, newValue);
                getCustomFieldType().updateValue(this, issue, newValue);
                String changelogValue = getChangelogValue(newValue);
                if (changelogValue != null)
                {
                    String changelogString = getChangelogString(newValue);
                    // If the changelogString is null then fall back to the changelogValue
                    if (changelogString == null)
                    {
                        changelogString = changelogValue;
                        changelogValue = null;
                    }

                    String oldChangelogString = getChangelogString(existingValue);
                    String oldChangelogValue = getChangelogValue(existingValue);
                    // If the changelogString is null then fall back to the changelogValue
                    if (oldChangelogString == null)
                    {
                        oldChangelogString = oldChangelogValue;
                        oldChangelogValue = null;
                    }
                    cib = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD, getNameKey(), oldChangelogValue, oldChangelogString, changelogValue, changelogString);
                }
            }
        }

        return cib;
    }

    private Object processValueThroughRenderer(FieldLayoutItem fieldLayoutItem, Object value)
    {
        if (isRenderable())
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            value = rendererManager.getRendererForType(rendererType).transformFromEdit(value);
        }
        return value;
    }

    /**
     * Returns the same string.
     *
     * @param changeHistory change history string
     * @return change history string
     */
    @Override
    public String prettyPrintChangeHistory(String changeHistory)
    {
        return changeHistory;
    }

    /**
     * Returns the same string.
     *
     * @param changeHistory change history string
     * @param i18nHelper    not used
     * @return change history string
     */
    @Override
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        return changeHistory;
    }

    /**
     * Returns true if this custom field has an edit template, false otherwise.
     *
     * @return true if this custom field has an edit template, false otherwise.
     */
    @Override
    public boolean isEditable()
    {
        return getCustomFieldType().getDescriptor().isEditTemplateExists();
    }


    /**
     * Returns options for this custom field if it is of {@link MultipleCustomFieldType} type. Otherwise returns null.
     *
     * @param key             not used
     * @param jiraContextNode JIRA context node
     * @return options for this custom field if it is of {@link MultipleCustomFieldType} type, null otherwise
     */
    @Override
    public Options getOptions(String key, JiraContextNode jiraContextNode)
    {
        return getOptions(key, getRelevantConfig(jiraContextNode), jiraContextNode);
    }

    /**
     * Returns options for this custom field if it is of {@link MultipleCustomFieldType} type. Otherwise returns null.
     *
     * @param key         not used
     * @param config      relevant field config
     * @param contextNode JIRA context node
     * @return options for this custom field if it is of {@link MultipleCustomFieldType} type, null otherwise
     */
    @Override
    public Options getOptions(String key, FieldConfig config, JiraContextNode contextNode)
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof MultipleCustomFieldType)
        {
            final MultipleCustomFieldType multipleCustomFieldType = (MultipleCustomFieldType) customFieldType;

            return multipleCustomFieldType.getOptions(config, contextNode);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void populateDefaults(Map<String, Object> customFieldValuesHolder, Issue issue)
    {
        FieldConfig config = getRelevantConfig(issue);
        if (config != null)
        {
            Object defaultValues = getCustomFieldType().getDefaultValue(config);

            CustomFieldParams paramsFromIssue;
            if (defaultValues != null)
            {
                paramsFromIssue = new CustomFieldParamsImpl(this, defaultValues);
                paramsFromIssue.transformObjectsToStrings();
                customFieldValuesHolder.put(getId(), paramsFromIssue);
                customFieldValuesHolder.put(getId() + ":objects", new CustomFieldParamsImpl(this, defaultValues));
            }
        }
        else
        {
            log.info("No relevant config found for " + this + " for the issue " + issue);
        }
    }

    /**
     * Returns the relevant field config of this custom field for the give issue context
     *
     * @param issueContext issue context to find the relevant field config for
     * @return the relevant field config of this custom field for the give issue context
     */
    @Override
    public FieldConfig getRelevantConfig(IssueContext issueContext)
    {
        return fieldConfigSchemeManager.getRelevantConfig(issueContext, this);
    }

    @Override
    public FieldConfig getRelevantConfig(Issue issue)
    {
        // TODO: Issue extends IssueContext. This method is redundant.
        return getRelevantConfig((IssueContext) issue);
    }

    private FieldConfig getRelevantConfig(JiraContextNode contextNode)
    {
        return getRelevantConfig(new IssueContextImpl(contextNode.getProjectObject(), contextNode.getIssueTypeObject()));
    }

    @Override
    public boolean hasParam(Map parameters)
    {
        CustomFieldParams relevantParams = getRelevantParams(parameters);
        return !relevantParams.isEmpty();
    }

    /**
     * Puts the relevant parameters from the given params map to the given customFieldValuesHolder map.
     *
     * @param customFieldValuesHolder map of custom field values
     * @param params                  map of parameters
     */
    @Override
    public void populateFromParams(Map<String, Object> customFieldValuesHolder, Map<String, String[]> params)
    {
        final CustomFieldParams relevantParams = getRelevantParams(params);
        customFieldValuesHolder.put(getId(), relevantParams);
    }

    /**
     * Puts the custom field parameters retrieved from the given issue to the given customFieldValuesHolder map.
     *
     * @param customFieldValuesHolder map of custom field values
     * @param issue                   issue to get the custom field parameters from
     */
    @Override
    public void populateFromIssue(Map<String, Object> customFieldValuesHolder, Issue issue)
    {
        CustomFieldParams paramsFromIssue = getCustomFieldParamsFromIssue(issue);

        customFieldValuesHolder.put(getId(), paramsFromIssue);
    }


    @Override
    public Object getValueFromParams(Map params) throws FieldValidationException
    {
        return getCustomFieldType().getValueFromCustomFieldParams((CustomFieldParams) params.get(getId()));
    }

    /**
     * Does nothing. Throws UnsupportedOperationException.
     *
     * @param fieldValuesHolder not used
     * @param stringValue       not used
     * @param issue             not used
     * @throws UnsupportedOperationException always
     */
    @Override
    public void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
    {
        // TODO Need to proxy to the custom field type for conversion
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List getConfigurationItemTypes()
    {
        return getCustomFieldType().getConfigurationItemTypes();
    }

    @Override
    public SearchHandler createAssociatedSearchHandler()
    {
        CustomFieldSearcher customFieldSearcher = getCustomFieldSearcher();
        final ClauseNames clauseNames = getClauseNames();
        if (customFieldSearcher == null)
        {
            // JRA-19106 - This is a special case where we can sort but not search so we will provide an no-op query and validator generators
            if (getCustomFieldType() instanceof SortableCustomField)
            {
                final ClauseHandler noOpClauseHandler = new NoOpClauseHandler(createClausePermissionHandler(null), getId(), clauseNames, "jira.jql.validation.field.not.searchable");
                final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(noOpClauseHandler);
                return new SearchHandler(Collections.<FieldIndexer>emptyList(), null, Collections.singletonList(clauseRegistration));
            }

            return null;
        }

        final CustomFieldSearcherClauseHandler searcherClauseHandler = customFieldSearcher.getCustomFieldSearcherClauseHandler();

        final ClauseContextFactory clauseContextFactory;
        if (searcherClauseHandler instanceof CustomFieldClauseContextHandler)
        {
            clauseContextFactory = ((CustomFieldClauseContextHandler) searcherClauseHandler).getClauseContextFactory();
        }
        else
        {
            final FieldConfigSchemeClauseContextUtil clauseContextUtil = ComponentAccessor.getComponentOfType(FieldConfigSchemeClauseContextUtil.class);
            clauseContextFactory = new CustomFieldClauseContextFactory(this, clauseContextUtil, ContextSetUtil.getInstance());
        }

        // if the custom field requires sanitising, the SearchClauseHandler should specify the sanitiser to be used
        ClauseSanitiser sanitiser = null;
        if (searcherClauseHandler instanceof CustomFieldClauseSanitiserHandler)
        {
            sanitiser = ((CustomFieldClauseSanitiserHandler) searcherClauseHandler).getClauseSanitiser();
        }

        ClauseInformation clauseInformation = new DefaultClauseInformation(getId(), clauseNames, getId(),
                searcherClauseHandler.getSupportedOperators(), searcherClauseHandler.getDataType());

        final ClauseHandler customFieldClauseHandler;
        if (searcherClauseHandler instanceof ValueGeneratingClauseHandler)
        {
            customFieldClauseHandler = new DefaultValuesGeneratingClauseHandler(clauseInformation, searcherClauseHandler.getClauseQueryFactory(), searcherClauseHandler.getClauseValidator(),
                    createClausePermissionHandler(sanitiser), clauseContextFactory,
                    ((ValueGeneratingClauseHandler)(searcherClauseHandler)).getClauseValuesGenerator());
        }
        else
        {
            customFieldClauseHandler = new DefaultClauseHandler(clauseInformation, searcherClauseHandler.getClauseQueryFactory(),
                searcherClauseHandler.getClauseValidator(), createClausePermissionHandler(sanitiser), clauseContextFactory);
        }
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(customFieldClauseHandler);

        return new SearchHandler(customFieldSearcher.getSearchInformation().getRelatedIndexers(),
                new SearchHandler.SearcherRegistration(customFieldSearcher, clauseRegistration));
    }

    /**
     * @param sanitiser if null, the {@link com.atlassian.jira.jql.permission.NoOpClauseSanitiser} will be used.
     * @return a clause permission handler
     */
    private ClausePermissionHandler createClausePermissionHandler(final ClauseSanitiser sanitiser)
    {
        final CustomFieldClausePermissionChecker.Factory factory = ComponentAccessor.getComponentOfType(CustomFieldClausePermissionChecker.Factory.class);

        final ClausePermissionChecker checker = factory.createPermissionChecker(this, contextUtil);
        if (sanitiser == null)
        {
            return new DefaultClausePermissionHandler(checker);
        }
        else
        {
            return new DefaultClausePermissionHandler(checker, sanitiser);
        }
    }

    @Override
    public void createValue(Issue issue, Object value)
    {
        // Do not store null values
        if (value != null)
        {
            getCustomFieldType().createValue(this, issue, value);
        }
    }

    @Override
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map<String,Object> params = operationContext.getFieldValuesHolder();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        FieldConfig config = getRelevantConfig(issue);

        if (params.containsKey(getId()))
        {
            final CustomFieldParams customFieldParams = (CustomFieldParams) params.get(getId());
            getCustomFieldType().validateFromParams(addAdditionalParametersForValidation(customFieldParams, issue), errorCollection, config);
        }

        // Only validate for 'requireness' if no errors have been found
        if (!errorCollection.hasAnyErrors())
        {
            try
            {
                // Check that if the field is 'required' that the value has been provided if it is editable
                if (isEditable() && fieldScreenRenderLayoutItem != null && fieldScreenRenderLayoutItem.isRequired())
                {
                    // If the value is not in the map or if the value is null add an error message.
                    if (!params.containsKey(getId()) || getCustomFieldType().getValueFromCustomFieldParams((CustomFieldParams) params.get(getId())) == null)
                    {
                        errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", getName()), Reason.VALIDATION_FAILED);
                    }
                }
            }
            catch (FieldValidationException e)
            {
                log.error("Error occurred while validating a custom field", e);
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        else
        {
            errorCollectionToAddTo.addErrorCollection(errorCollection);
        }
    }

    /**
     * Return a copy copy of the original params with additional parameters from the issue.
     */
    private CustomFieldParams addAdditionalParametersForValidation(final CustomFieldParams customFieldParams, final Issue issue)
    {
        if (customFieldParams.containsKey(getParamKeyIssueId()) || issue == null)
        {
            return customFieldParams;
        }
        // Add additional parameters required by {@link UserCFType} for user filter based validation.
        // This will be set for all types of custom fields, but those CustomFieldType that are not aware of
        // these parameters would just ignore them.
        // This is deemed to be less hacky than conditionally setting the parameters using instanceof,
        //  such as the "nasty hack" with VersionCFType

        // create a copy and add new params
        CustomFieldParams params = new CustomFieldParamsImpl(customFieldParams.getCustomField(), customFieldParams);

        // Adding the issue id so that CustomFieldType#validateFromParameters() could use it to retrieve data such as
        //  the existing value of the customer field
        // This is cheap because no extra computation or db operations are required.
        // We don't want to retrieve the existing value from DB as this stage, as not all custom fields need it
        if (issue.getId() != null)
        {
            params.put(getParamKeyIssueId(), ImmutableList.of(issue.getId().toString()));
        }
        // Adding the project id so that CustomFieldType#validateFromParameters() could use it to for validation related to projects
        if (issue.getProjectId() != null)
        {
            params.put(getParamKeyProjectId(), ImmutableList.of(issue.getProjectId().toString()));
        }
        // To instruct CustomFieldType#validateFromParameters() that project id's should be retrieved
        //  only meaningful to those implementations that recognize it, e.g., UserCFType
        params.put(CustomFieldUtils.getParamKeyRequireProjectIds(), ImmutableList.of(Boolean.TRUE.toString()));

        return params;
    }

    @Override
    public CustomFieldParams getCustomFieldValues(Map customFieldValuesHolder)
    {
        if (customFieldValuesHolder == null)
        {
            return new NullCustomFieldParams();
        }

        final CustomFieldParams customFieldParams = (CustomFieldParams) customFieldValuesHolder.get(getId());
        if (customFieldParams == null)
        {
            return new NullCustomFieldParams();
        }
        else
        {
            return customFieldParams;
        }
    }


    /**
     * Removes this custom field and returns a set of issue IDs of all issues that are affected by removal of this
     * custom field.
     *
     * @return a set of issue IDs of affected issues
     * @throws DataAccessException if removal of generic value fails
     * @deprecated Use {@link com.atlassian.jira.issue.CustomFieldManager#removeCustomField(CustomField)} instead. Since v5.1.
     */
    @Deprecated
    @Override
    public Set<Long> remove() throws DataAccessException
    {
        CustomFieldType<?,?> customFieldType = getCustomFieldType();

        Set<Long> issueIds;
        if (customFieldType != null)
        {
            issueIds = customFieldType.remove(this);
        }
        else
        {
            issueIds = Collections.emptySet();
        }

        try
        {
            OFBizPropertyUtils.removePropertySet(gv);
            gv.remove();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        eventPublisher.publish(new CustomFieldDeletedEvent(this));
        return issueIds;
    }

    //------------------------ HELPER METHODS ---------------------------//

    /**
     * Returns custom field parameter from the given map that are relevant to this custom field.
     *
     * @param params map of parameters
     * @return custom field parameter from the given map that are relevant to this custom field
     */
    protected CustomFieldParams getRelevantParams(Map<String, String[]> params)
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl(this);
        for (final Map.Entry<String, String[]> entry : params.entrySet())
        {
            final String key = entry.getKey();
            final String customFieldKey = CustomFieldUtils.getCustomFieldKey(key);
            if (key != null && getId().equals(customFieldKey))
            {
                String[] p = entry.getValue();
                if (p != null && p.length > 0)
                {
                    for (String param : p)
                    {
                        if (param != null && param.length() > 0)
                        {
                            customFieldParams.addValue(CustomFieldUtils.getSearchParamSuffix(key), Arrays.asList(p));
                            break; //exit for loop
                        }
                    }
                }

            }
        }
        return customFieldParams;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Object customFieldValue = getRendererCustomFieldValue(fieldLayoutItem, issue, displayParams);  // this is so that the DocumentIssueImpl can return optimised results JRA-7300
        return getCustomFieldType().getDescriptor().getColumnViewHtml(this, customFieldValue, issue, displayParams, fieldLayoutItem);
    }

    private Object getRendererCustomFieldValue(FieldLayoutItem fieldLayoutItem, Issue issue, Map displayParams)
    {
        Object customFieldValue = issue.getCustomFieldValue(this);
        if (isRenderable() && displayParams.get("excel_view") == null)
        {
            String renderedContent = rendererManager.getRenderedContent(fieldLayoutItem, issue);
            if (StringUtils.isNotBlank(renderedContent))
            {
               // JRA-12479 - probably the custom field is in value creation process
               customFieldValue = renderedContent;
            }
        }
        return customFieldValue;
    }

    private Object getRendererCustomFieldValue(FieldLayoutItem fieldLayoutItem, Issue issue, Object value)
    {
        Object customFieldValue;
        if (isRenderable() && value instanceof String)
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            customFieldValue = rendererManager.getRenderedContent(rendererType, (String) value, issue.getIssueRenderContext());
        }
        else
        {
            customFieldValue = value;
        }
        return customFieldValue;
    }

    protected I18nHelper getI18nHelper()
    {
        return getCustomFieldType().getDescriptor().getI18nBean();
    }

    @Override
    public String getHiddenFieldId()
    {
        return getId();
    }

    @Override
    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getCreateHtml(fieldLayoutItem, operationContext, action, issue, new HashMap());
    }

    @Override
    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map dispayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, dispayParameters);
    }

    @Override
    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, new HashMap());
    }

    @Override
    public String getEditHtml(FieldLayoutItem fieldLayoutItem,
                              OperationContext operationContext,
                              Action action,
                              Issue issue,
                              Map dispayParameters)
    {
        return getCustomFieldType().getDescriptor().getEditHtml(getRelevantConfig(issue),
                operationContext.getFieldValuesHolder(),
                issue,
                action,
                dispayParameters,
                fieldLayoutItem);
    }

    @Override
    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        notNull("bulkEditBean", bulkEditBean);
        notEmpty("selectedIssues", bulkEditBean.getSelectedIssues());

        FieldLayoutItem fieldLayoutItem = null;
        if (bulkEditBean.getTargetFieldLayout() != null)
        {
            // This means we are in a bulk move operation, so we will be using the first target issue to render the edit HTML.
            fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);

            // JRA-21669
            // When the Bulk Move Mapping feature was written in JIRA 4.1, we did not intend for it to work with any
            // Custom Field Type other than VersionCFType. A bug was later found that UserCFTypes were not happy with the
            // way our mapping code assumed that custom field values were always Longs. So to quickly fix this bug and
            // keep with our initial promise of only working with VersionCFType, we will do a nasty check on the fieldLayoutItem
            // to ensure that the field it refers to is actually a VersionCFType. All other custom fields will render their
            // bulk edit HTML when they are bulk moving -- as we always intended. This can be revisited in future if we
            // decide to add mapping during Bulk Move for all custom field types / other system fields.
            if (isCustomFieldTypeSupportedForDistinctValueMapping(fieldLayoutItem))
            {
                return getBulkMoveHtmlWithMapping(fieldLayoutItem, operationContext, action, bulkEditBean, displayParameters);
            }
            else
            {
                // have to use a target issue here so that we get the correct rendering of the edit control
                return getEditHtml(fieldLayoutItem, operationContext, action, bulkEditBean.getFirstTargetIssueObject(), displayParameters);
            }
        }
        else
        {
            // This means we are in a bulk edit or bulk workflow transition operation.
            // Since we do not allow bulk edit of fields that have differing renderer types we can safely
            // pick any issue to serve as our context issue for the call to editHtml
            if (!bulkEditBean.getFieldLayouts().isEmpty())
            {
                fieldLayoutItem = bulkEditBean.getFieldLayouts().iterator().next().getFieldLayoutItem(this);
            }
        }
        // Use one of the selected issues to render the edit HTML, do not use a target issue since we are not in a bulk move operation here (only above).
        return getEditHtml(fieldLayoutItem, operationContext, action, bulkEditBean.getSelectedIssues().iterator().next(), displayParameters);
    }

    private String getBulkMoveHtmlWithMapping(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        // The HTML displayed for Bulk Move of Version Custom Fields needs to allow the user to specify mappings for
        // each old version present in the currently selected issues.
        final Issue issue = bulkEditBean.getFirstTargetIssueObject();

        final BulkMoveHelper bulkMoveHelper = new DefaultBulkMoveHelper();

        // this function will retrieve the custom field's values as a collection of Strings which represent in the case
        // of VersionCFType the ids of the versions selected.
        final Function<Issue, Collection<Object>> issueValueResolver = new Function<Issue, Collection<Object>>()
        {
            public Collection<Object> get(final Issue issue)
            {
                final Map fieldValuesHolder = new LinkedHashMap();
                populateFromIssue(fieldValuesHolder, issue);
                final Object o = fieldValuesHolder.get(getId());
                final CustomFieldParams customFieldParams = (CustomFieldParams) o;
                return customFieldParams.getValuesForNullKey();
            }
        };

        // this function needs to be able to resolve the values that come back from the issueValueResolver function
        // into names that can be displayed to the user.
        final Function<Object, String> nameResolver = new Function<Object, String>()
        {
            public String get(final Object input)
            {
                // BEGIN HACK
                // At the time of writing, only Version custom fields are supported for Bulk Move. Since there is no
                // way to get the "name" of a custom field value using the CustomFieldType interface, we have to
                // hardcode what value types we are expecting and explicitly retrieve their name value.
                Object result = getCustomFieldType().getSingularObjectFromString((String) input);
                if (result == null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Could not resolve name for input '" + input + "'.");
                    }
                }
                else
                {
                    if (result instanceof Version)
                    {
                        return ((Version)result).getName();
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Type '" + result.getClass() + "' currently not supported for bulk move.");
                        }
                    }
                }
                return null;
                // END HACK
            }
        };

        final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues = bulkMoveHelper.getDistinctValuesForMove(bulkEditBean, this, issueValueResolver, nameResolver);

        return getCustomFieldType().getDescriptor().getBulkMoveHtml(getRelevantConfig(issue),
                operationContext.getFieldValuesHolder(),
                issue,
                action,
                displayParameters,
                fieldLayoutItem,
                distinctValues,
                bulkMoveHelper);
    }

    /**
     * As part of JRA-21669, we need to prevent custom fields which are not of {@link VersionCFType} from trying to do
     * the mapping-specific part of the Bulk Move rendering code.
     *
     * @param fieldLayoutItem the field layout item that represents the custom field being rendered
     * @return true if the custom field can support distinct value mapping; false otherwise.
     */
    private boolean isCustomFieldTypeSupportedForDistinctValueMapping(final FieldLayoutItem fieldLayoutItem)
    {
        final CustomField customField = (CustomField) fieldLayoutItem.getOrderableField();
        return (customField.getCustomFieldType() instanceof VersionCFType);
    }

    @Override
    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue)
    {
        return getViewHtml(fieldLayoutItem, action, issue, new HashMap());
    }

    @Override
    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        return getCustomFieldType().getDescriptor().getViewHtml(this, getRendererCustomFieldValue(fieldLayoutItem, issue, displayParameters), issue, fieldLayoutItem, displayParameters);
    }

    @Override
    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        return getCustomFieldType().getDescriptor().getViewHtml(this, getRendererCustomFieldValue(fieldLayoutItem, issue, value), issue, fieldLayoutItem, displayParameters);
    }

    @Override
    public boolean isShown(Issue issue)
    {
        return true;
    }

    @Override
    public Object getDefaultValue(Issue issue)
    {
        FieldConfig config = getRelevantConfig(issue);
        if (config == null)
        {
            return null;
        }

        return getCustomFieldType().getDefaultValue(config);
    }

    @Override
    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        try
        {
            if (fieldValueHolder.containsKey(getId()))
            {
                CustomFieldParams customFieldParams = (CustomFieldParams) fieldValueHolder.get(getId());
                issue.setCustomFieldValue(this, getCustomFieldType().getValueFromCustomFieldParams(customFieldParams));
            }
        }
        catch (FieldValidationException e)
        {
            // The exception should not be thrown here as before the issue is updated validation should have been done.
            throw new DataAccessException(e);
        }
    }

    @Override
    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (final Object originalIssue1 : originalIssues)
        {
            Issue originalIssue = (Issue) originalIssue1;

            // If the field was not originally in scope
            if (!isInScope(originalIssue.getProjectObject(), ImmutableList.of(originalIssue.getIssueTypeObject().getId())))
            {
                return new MessagedResult(true);
            }

            // Field required in target, blank in original
            if (!doesFieldHaveValue(originalIssue) && targetFieldLayoutItem != null && targetFieldLayoutItem.isRequired())
            {
                return new MessagedResult(true);
            }

            // If has value but fails error validation
            if (doesFieldHaveValue(originalIssue))
            {
                // Validates the value with the custom field
                CustomFieldParams customFieldParams = getCustomFieldParamsFromIssue(originalIssue);

                ErrorCollection errorCollection = new SimpleErrorCollection();
                FieldConfig config = getRelevantConfig(targetIssue);

                // BEGIN HACK
                // NOTE: This is a hack to temporarily fix the problem that a version custom field
                // needs to prompt a user for new values when doing a move operation and that operation
                // will end up moving the issue to a new project. This is because versions are a project
                // specific entity. The real fix is to somehow allow the CustomFieldType to determine if
                // it needs to move. At the moment the validateFromParams is being used for this but that
                // in itself is a hack and it does not get passed enough information, it needs the target
                // context so it can make context relevant decisions (like the versions changing because
                // of a project move).
                // See JRA-21726 for more info.
                if (getCustomFieldType() instanceof VersionCFType)
                {
                    if (!originalIssue.getProjectObject().getId().equals(targetIssue.getProjectObject().getId()))
                    {
                        return new MessagedResult(true);
                    }
                }
                // END HACK

                // Although we could pass in the target project id and validate the value of the user picker against user filter
                // in the new project, there is no proper validation on the Update Field screen in step 3 of Move Issue process.
                // Also there is no easy way to pre-populate the original value in the field to help user determine
                // what the new value should be.
                // Thus we are not validating the user picker against user filter during move issue.
//                if (!originalIssue.getProjectObject().getId().equals(targetIssue.getProjectObject().getId()) &&
//                    !customFieldParams.containsKey(PARAM_KEY_PROJECT_IDS))
//                {
//                    customFieldParams.put(PARAM_KEY_PROJECT_IDS, ImmutableList.of(targetIssue.getProjectId().toString()));
//                }
                getCustomFieldType().validateFromParams(customFieldParams, errorCollection, config);

                if (errorCollection.hasAnyErrors())
                {
                    log.debug("Move required. Errors occurred in automatic moving: " +
                            ToStringBuilder.reflectionToString(errorCollection.getErrorMessages()));
                    return new MessagedResult(true);
                }
            }

            // Also if the field is renderable and the render types differ prompt with an edit or a warning
            if (isRenderable() && doesFieldHaveValue(originalIssue))
            {
                FieldLayoutItem fieldLayoutItem = null;
                try
                {
                    fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(originalIssue.getProjectObject(), originalIssue.getIssueTypeObject().getId()).getFieldLayoutItem(getId());
                }
                catch (DataAccessException e)
                {
                    log.warn(getName() + " field was unable to resolve the field layout item for issue " + originalIssue.getId(), e);
                }

                String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                String targetRendererType = (targetFieldLayoutItem != null) ? targetFieldLayoutItem.getRendererType() : null;
                if (!rendererTypesEqual(rendererType, targetRendererType))
                {
                    if (originalIssues.size() > 1)
                    {
                        return new MessagedResult(false, getI18nHelper().getText("renderer.bulk.move.warning"), MessagedResult.WARNING);
                    }
                    else
                    {
                        return new MessagedResult(true);
                    }
                }
            }

        }
        return new MessagedResult(false);
    }

    @Override
    public void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // NOTE: this method should be delegated off to the CustomFieldType's so that they
        // can populate the value correctly. This is being left as is until the time can
        // be spent to implement each custom field types impl of this to-be-created-method
        // correctly.
        if (isRenderable())
        {
            // return the original value
            fieldValuesHolder.put(getId(), getCustomFieldParamsFromIssue(originalIssue));
        }
        else
        {
            // If the field needs to be updated then it should be populated with default values
            populateDefaults(fieldValuesHolder, targetIssue);
        }
    }

    @Override
    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setCustomFieldValue(this, null);
    }

    @Override
    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    @Override
    public boolean hasValue(Issue issue)
    {
        return (getValue(issue) != null);
    }

    /**
     * This is a hacked version of {@link #hasValue(com.atlassian.jira.issue.Issue)} which specifically looks at Labels
     * custom field types and determines if the value returned by them is an empty Collection. For the callers of this
     * method, the intent is that an empty Collection should not be considered as "having a value".
     * <p/>
     * This hack must exist as there is no way for custom field types to provide a way of telling if they "need moving".
     * See JRA-21726 for more info.
     *
     * @param issue the issue to look up the value for
     * @return true if the field has a value on the issue; for Labels custom field types this means it must be a non-empty
     * {@link Collection}.
     */
    private boolean doesFieldHaveValue(Issue issue)
    {
        final Object value = getValue(issue);
        if (value != null && getCustomFieldType() instanceof LabelsCFType && value instanceof Collection)
        {
            final Collection c = (Collection) value;
            return !c.isEmpty();
        }
        return value != null;
    }

    @Override
    public String getId()
    {
        // This value is cached, as this method is called thousands of time for any search / sort,
        // and generates a *lot* of garbage otherwise (approx 250k garbage per 12k issues).
        return customFieldId;
    }

    @Override
    public String getValueFromIssue(Issue issue)
    {
        Object value = getValue(issue);

        if (!(value instanceof String))
        {
            return null;
        }
        else
        {
            return (String) value;
        }
    }

    /**
     * Returns ID of this custom field.
     *
     * @return ID of this custom field
     */
    @Override
    public Long getIdAsLong()
    {
        return gv.getLong(ENTITY_ID);
    }

    /**
     * Returns a list of configuration schemes.
     *
     * @return a list of {@link FieldConfigScheme} objects
     */
    @Override
    public List<FieldConfigScheme> getConfigurationSchemes()
    {
        return fieldConfigSchemeManager.getConfigSchemesForField(this);
    }

    @Override
    public String getNameKey()
    {
        // For customfields we rely on the behaviour of the I18nBean to return the string passed to it
        // when a translation is not found. So here we pass the actually fully translated name as the key, which will
        // just have a miss when being translated and be returned as is.   Argghhhh!!!!!!!!!!
        return getName();
    }


    private CustomFieldParams getCustomFieldParamsFromIssue(Issue issue)
    {
        //JRA-16915: The CustomFieldParams returned should NEVER be null, since this may break calls to validate() methods
        //later on with a NPE.  This can cause problems for example, when deleting values in a workflow transition
        //via SOAP, if a customfield value for an issue is null.
        Object valueFromIssue = getCustomFieldType().getValueFromIssue(this, issue);

        if (valueFromIssue != null)
        {
            CustomFieldParams paramsFromIssue = new CustomFieldParamsImpl(this, valueFromIssue);
            paramsFromIssue.transformObjectsToStrings();
            return paramsFromIssue;
        }
        else
        {
            return new CustomFieldParamsImpl(this);
        }
    }

    // ------------------------------------------------------------------------------------- Convenience Context Methods

    /**
     * Returns a list of associated project categories for this custom field.
     * It returns null if {@link #getConfigurationSchemes()} returns null.
     * It returns an empty list if the {@link #getConfigurationSchemes()} returns an empty list.
     * The returned list is sorted by name using {@link OfBizComparators#NAME_COMPARATOR}.
     *
     * @return a list of {@link org.ofbiz.core.entity.GenericValue} objects that represent associated project categories
     *         as {@link com.atlassian.jira.issue.context.ProjectCategoryContext} objects
     */
    @Override
    public List<GenericValue> getAssociatedProjectCategories()
    {
        List<GenericValue> projectCategories = null;
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            projectCategories = new LinkedList<GenericValue>();
            for (final FieldConfigScheme config : configurations)
            {
                List<GenericValue> configProject = config.getAssociatedProjectCategories();
                if (configProject != null)
                {
                    projectCategories.addAll(configProject);
                }
            }

            Collections.sort(projectCategories, OfBizComparators.NAME_COMPARATOR);
        }
        return projectCategories;
    }

    @Override
    public List<ProjectCategory> getAssociatedProjectCategoryObjects()
    {
        List<ProjectCategory> projectCategories = null;
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            projectCategories = new LinkedList<ProjectCategory>();
            for (final FieldConfigScheme config : configurations)
            {
                List<ProjectCategory> configProject = config.getAssociatedProjectCategoryObjects();
                if (configProject != null)
                {
                    projectCategories.addAll(configProject);
                }
            }

            Collections.sort(projectCategories, NameComparator.COMPARATOR);
        }
        return projectCategories;
    }

    @Override
    public List<GenericValue> getAssociatedProjects()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        List<GenericValue> projects = null;
        if (configurations != null)
        {
            projects = new LinkedList<GenericValue>();
            for (final FieldConfigScheme config : configurations)
            {
                List<GenericValue> configProject = config.getAssociatedProjects();
                if (configProject != null)
                {
                    projects.addAll(configProject);
                }
            }

            Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        }

        return projects;
    }

    @Override
    public List<Project> getAssociatedProjectObjects()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        List<Project> projects = null;
        if (configurations != null)
        {
            projects = new LinkedList<Project>();
            for (final FieldConfigScheme config : configurations)
            {
                List<Project> configProject = config.getAssociatedProjectObjects();
                if (configProject != null)
                {
                    projects.addAll(configProject);
                }
            }

            Collections.sort(projects, NameComparator.COMPARATOR);
        }

        return projects;
    }

    @Override
    public List<GenericValue> getAssociatedIssueTypes()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        List<GenericValue> issueTypes = null;
        if (configurations != null)
        {
            Set<GenericValue> issueTypesSet = new HashSet<GenericValue>();
            for (final FieldConfigScheme config : configurations)
            {
                Set<GenericValue> configIssueType = config.getAssociatedIssueTypes();
                if (configIssueType != null)
                {
                    issueTypesSet.addAll(configIssueType);
                }
            }

            issueTypes = new ArrayList<GenericValue>(issueTypesSet);
            Collections.sort(issueTypes, OfBizComparators.NAME_COMPARATOR);
        }

        return issueTypes;
    }

    /**
     * Returns true if this custom field applies for all projects and all issue types.
     *
     * @return true if it is in all projects and all issue types, false otherwise.
     */
    @Override
    public boolean isGlobal()
    {
        return isAllProjects() && isAllIssueTypes();
    }

    /**
     * Checks whether this custom field applies for all projects. It returns true if it applies for all projects
     * for any field configuration scheme, false otherwise.
     *
     * @return true if it applies for all projects for any field configuration scheme, false otherwise.
     */
    @Override
    public boolean isAllProjects()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            for (FieldConfigScheme configuration : configurations)
            {
                if (configuration.isAllProjects())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if it applies for all issue types, false otherwise. The actual check test if the list returned by
     * {@link #getAssociatedIssueTypes()} contains null - all issue types.
     *
     * @return true if it applies for all issue types, false otherwise.
     */
    @Override
    public boolean isAllIssueTypes()
    {
        final List<GenericValue> issueTypes = getAssociatedIssueTypes();
        return issueTypes != null && issueTypes.contains(null);
    }

    /**
     * Returns true if all configuration schemes returned by {@link #getConfigurationSchemes()} are enabled.
     *
     * @return true if all configuration schemes are enabled, false otherwise
     */
    @Override
    public boolean isEnabled()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            for (FieldConfigScheme config : configurations)
            {
                if (config.isEnabled())
                {
                    return true;
                }
            }
        }

        return false;
    }

    // ------------------------------------------------------------------------------------------------------- Bulk Edit

    /**
     * Checks if custom field is available for bulk edit operation, whether 'shown' and if user has bulk update permission.
     * Also checks that all selected issues have the same field config for this custom field. All field configs must be
     * the same or all null.
     *
     * @param bulkEditBean bulk edit bean
     * @return null if available for bulk edit or an appropriate 'unavailable' string
     */
    @Override
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Check for custom field specific requirements
        CustomFieldType customFieldType = getCustomFieldType();
        String customFieldAvailabilityString = customFieldType.availableForBulkEdit(bulkEditBean);

        if (TextUtils.stringSet(customFieldAvailabilityString))
        {
            return customFieldAvailabilityString;
        }

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (final Object o : bulkEditBean.getFieldLayouts())
        {
            FieldLayout fieldLayout = (FieldLayout) o;
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }
        }

        FieldConfig config = null;
        boolean first = true;
        for (final Object o : bulkEditBean.getSelectedIssues())
        {
            Issue issue = (Issue) o;
            if (!(hasBulkUpdatePermission(bulkEditBean, issue)))
            {
                return "bulk.edit.unavailable.permission";
            }

            // Check if it's the same config
            FieldConfig currentConfig = getRelevantConfig(issue);
            // Grab the first config - we will compare all other configs with this one.
            if (first)
            {
                if (currentConfig == null)
                {
                    // Found an issue for which we do not have a relevant config - the field should not
                    // be available for bulk operation
                    return "bulk.edit.incompatible.customfields";
                }
                else
                {
                    config = currentConfig;
                    first = false;
                }
            }
            // Compare the configurations - return the text if the configs are different
            // Note: null config is not equal to non-null config
            else if (areDifferent(config, currentConfig))
            {
                return "bulk.edit.incompatible.customfields";
            }
        }

        // Make sure that if this field is renderable that it has no conflicting render types in any field layouts
        // that the select issues belong to.
        if (isRenderable())
        {
            String rendererType = null;
            for (final Object o : bulkEditBean.getFieldLayouts())
            {
                FieldLayout fieldLayout = (FieldLayout) o;
                String tempRendererType = fieldLayout.getRendererTypeForField(getId());
                if (rendererType == null)
                {
                    rendererType = tempRendererType;
                }
                else if (!rendererType.equals(tempRendererType))
                {
                    return "bulk.edit.unavailable.inconsistent.rendertypes";
                }
            }
        }

        return null;
    }

    /**
     * Compare two objects - return false if both are null or equal. Return true otherwise.
     *
     * @param obj1 the first object to compare
     * @param obj2 the second object to compare
     * @return false if both are null or equal. Return true otherwise.
     */
    protected static boolean areDifferent(Object obj1, Object obj2)
    {
        return (obj1 != null && !obj1.equals(obj2)) || (obj1 == null && obj2 != null);
    }

    /**
     * Checks whether the user has the permission to execute the bulk operation for the provided issue. In case of Bulk
     * Workflow Transition checks for nothing. In case of all others (e.g. Bulk Edit) checks for Edit permission.
     *
     * @param bulkEditBean bulk edit bean
     * @param issue        issue to check permission on
     * @return true if has permission, false otherwise
     */
    protected boolean hasBulkUpdatePermission(BulkEditBean bulkEditBean, Issue issue)
    {
        // Do not check the permission if we are doing a bulk workflow transition. Bulk Workflow
        // transition is only protected by the workflow conditions of the transition and should not
        // hardcode a check for a permission here.
        // For bulk edit we should check whether the user has the edit permission for the issue
        return BulkWorkflowTransitionOperation.NAME.equals(bulkEditBean.getOperationName()) ||
                permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, authenticationContext.getUser());
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators

    /**
     * Returns the name of this custom field.
     *
     * @return the name of this custom field
     */
    @Override
    public String getName()
    {
        String translatedName = translationManager.getCustomFieldNameTranslation(this);
        if (StringUtils.isNotEmpty(translatedName))
        {
            return translatedName;
        }

        return gv.getString(ENTITY_NAME);
    }

    /**
     * Returns the name of this custom field by reading {@link #ENTITY_NAME} of the underlying generic value.
     *
     * @return the name of this custom field
     */
    @Override
    public String getUntranslatedName()
    {
        return gv.getString(ENTITY_NAME);
    }

    @Override
    public String getFieldName()
    {
        //xss is allowed on BTF until 6.0 so we use wiki renderer to allow simple markup only in OD
        if (featureManager.isOnDemand())
        {
            final IssueRenderContext renderContext = new IssueRenderContext(null);
            renderContext.addParam(IssueRenderContext.INLINE_PARAM, Boolean.TRUE);
            return rendererManager.getRenderedContent(AtlassianWikiRenderer.RENDERER_TYPE, getName(), renderContext);
        } else {
            return TextUtils.htmlEncode(getName());
        }
    }

    /**
     * Sets the name of this custom field by setting the {@link #ENTITY_NAME} of the underlying generic value.
     * The name is abbreviated to a number of characters equal to {@link FieldConfigPersister#ENTITY_LONG_TEXT_LENGTH}.
     *
     * @param name name to set
     */
    @Override
    public void setName(String name)
    {
        gv.setString(ENTITY_NAME, StringUtils.abbreviate(name, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH));
    }

    /**
     * Returns the i18n'ed description of this custom field.
     *
     * @return the description of this custom field
     */
    @Override
    public String getDescription()
    {
        String translatedDesc = translationManager.getCustomFieldDescriptionTranslation(this);
        if (StringUtils.isNotEmpty(translatedDesc))
        {
            return translatedDesc;
        }
        return gv.getString(ENTITY_DESCRIPTION);
    }

    /**
     * Returns the description of this custom field by reading {@link #ENTITY_DESCRIPTION} of the underlying generic value.
     *
     * @return the description of this custom field
     */
    @Override
    public String getUntranslatedDescription()
    {
        return gv.getString(ENTITY_DESCRIPTION);
    }

    /**
     * Returns a {@code RenderableProperty} for rendering this custom field's description.
     *
     * @return a read-only RenderableProperty
     * @since v5.0.7
     */
    @Nonnull
    @Override
    public RenderableProperty getDescriptionProperty()
    {
        return customFieldDescription.createRenderablePropertyFor(this);
    }

    /**
     * Returns a {@code RenderableProperty} for rendering this custom field's description.
     *
     * @return a read-only RenderableProperty
     * @since v5.0.7
     */
    @Nonnull
    @Override
    public RenderableProperty getUntranslatedDescriptionProperty()
    {
        return customFieldDescription.createRenderablePropertyFor(getUntranslatedDescription());
    }

    /**
     * Sets the description of this custom field by setting the {@link #ENTITY_DESCRIPTION} of the underlying generic
     * value.
     *
     * @param description description to set
     */
    @Override
    public void setDescription(String description)
    {
        gv.setString(ENTITY_DESCRIPTION, description);
    }

    /**
     * Retrieves the {@link CustomFieldSearcher} for this custom field looking it up in the customFieldManager
     * by the searcher key retrieved from {@link #ENTITY_CUSTOM_FIELD_SEARCHER} underlying generic value attribute.
     * The seracher, if found is initialized with this custom field before it is returned.
     *
     * @return found custom field searcher or null, if none found
     */
    @Override
    public CustomFieldSearcher getCustomFieldSearcher()
    {
        return searcherRef.get();
    }

    /**
     * Sets the {@link CustomFieldSearcher} for this custom field by setting the {@link #ENTITY_CUSTOM_FIELD_SEARCHER}
     * underlying generic value attribute to the value of the key retrieved from the searcher.
     *
     * @param searcher custom field searcher to associate with this custom field
     */
    @Override
    public void setCustomFieldSearcher(CustomFieldSearcher searcher)
    {
        String key = null;
        if (searcher != null)
        {
            key = searcher.getDescriptor().getCompleteKey();
        }

        gv.setString(ENTITY_CUSTOM_FIELD_SEARCHER, key);
        //need to reset the lazy reference,as it is no longer valid
        searcherRef.reset();
    }

    /**
     * Looks up the {@link com.atlassian.jira.issue.customfields.CustomFieldType} in the {@link #customFieldTypeModuleDescriptors} by
     * the key retrieved from the {@link #ENTITY_CF_TYPE_KEY} attribute of the underlying generic value.
     * This only happens once if {@link #typeRef} is null, then the custom field type is set and returned each time.
     * It can return null if the custom field type cannot be found by that key.
     *
     * @return custom field type
     */
    @Override
    public CustomFieldType getCustomFieldType()
    {
        return typeRef.get();
    }


    @Override
    public String getColumnHeadingKey()
    {
        return getName();
    }

    @Override
    public String getColumnCssClass()
    {
        return getId();
    }

    /**
     * Returns {@link #ORDER_ASCENDING}.
     *
     * @return ascending order as {@link #ORDER_ASCENDING} value
     */
    @Override
    public String getDefaultSortOrder()
    {
        return ORDER_ASCENDING;
    }

    // ---------------------------------------------------------------------------------------------- Compare & Equality
    private boolean isSortable()
    {
        return (!getSortFields(false).isEmpty());
    }

    boolean valuesEqual(Object v1, Object v2)
    {
        return getCustomFieldType().valuesEqual(v1, v2);
    }

    /**
     * This method compares the values of this custom field in two given issues.
     * <p/>
     * Returns a negative integer, zero, or a positive integer as the value of first issue is less than, equal to,
     * or greater than the value of the second issue.
     * <p/>
     * This method returns 0 if this custom field is not sortable, or its customFieldType is not an instance
     * of {@link SortableCustomField}
     * <p/>
     * If either of given issues is null a IllegalArgumentException is thrown.
     *
     * @param issue1 issue to compare
     * @param issue2 issue to compare
     * @return a negative integer, zero, or a positive integer as the value of first issue is less than, equal to, or
     *         greater than the value of the second issue
     * @throws IllegalArgumentException if any of given issues is null
     */
    @Override
    public int compare(Issue issue1, Issue issue2) throws IllegalArgumentException
    {
        if (!isSortable())
        {
            log.error("Called compare method, even though not comparable");
            return 0;
        }

        if (issue1 == null && issue2 == null)
        {
            throw new IllegalArgumentException("issue1 and issue2 are null");
        }

        if (issue1 == null)
        {
            throw new IllegalArgumentException("issue1 is null");
        }

        if (issue2 == null)
        {
            throw new IllegalArgumentException("issue2 is null");
        }

        Object v1 = getValue(issue1);
        Object v2 = getValue(issue2);

        if (v1 == v2)
        {
            return 0;
        }

        if (v1 == null)
        {
            return 1; // null values at the end?
        }

        if (v2 == null)
        {
            return -1;
        }

        // Ensure that both of the contexts are the same and then compare
        final CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof SortableCustomField)
        {
            SortableCustomField sortable = (SortableCustomField) customFieldType;
            FieldConfig c1 = getRelevantConfig(issue1);
            FieldConfig c2 = getRelevantConfig(issue2);
            if (c1 == null || !c1.equals(c2))
            {
                log.info("Sort order for custom field " + this + " for issues " + issue1 + " and " + issue2 + " " +
                        "contexts did not match. Sort order may be incorrect");
            }
            return sortable.compare(v1, v2, c1);
        }

        return 0;
    }

    /**
     * If this field has a searcher, and this searcher implements {@link SortableCustomFieldSearcher} then return
     * {@link SortableCustomFieldSearcher#getSorter(CustomField)}.  Else return null.
     */
    @Override
    public LuceneFieldSorter getSorter()
    {
        if (getCustomFieldSearcher() instanceof SortableCustomFieldSearcher)
        {
            return ((SortableCustomFieldSearcher) getCustomFieldSearcher()).getSorter(this);
        }
        else
        {
            return null;
        }
    }

    /**
     * Return a SortComparatorSource that uses either a custom field searcher that implements
     * {@link SortableCustomFieldSearcher} or a custom field that implements {@link SortableCustomField}.
     * If neither are found, this method returns null.
     */
    @Override
    public FieldComparatorSource getSortComparatorSource()
    {
        LuceneFieldSorter sorter = getSorter();
        if (sorter != null)
        {
            return new MappedSortComparator(sorter);
        }
        else if (getCustomFieldType() instanceof SortableCustomField)
        {
            return new DocumentSortComparatorSource(new DefaultIssueSortComparator(new CustomFieldIssueSortComparator(this)));
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<SortField> getSortFields(boolean sortOrder)
    {
        List<SortField> sortFields = new ArrayList<SortField>();

        CustomFieldSearcher customFieldSearcher = getCustomFieldSearcher();
        if (customFieldSearcher instanceof NaturallyOrderedCustomFieldSearcher)
        {
            String fieldName = ((NaturallyOrderedCustomFieldSearcher) customFieldSearcher).getSortField(this);
            SortField sortField = new SortField(fieldName, new StringSortComparator(), sortOrder);
            sortFields.add(sortField);
        }
        else
        {
            final FieldComparatorSource sorter = getSortComparatorSource();
            if (sorter != null)
            {
                String fieldName = customFieldId;
                SortField sortField = new SortField(fieldName, sorter, sortOrder);
                sortFields.add(sortField);
            }
        }
        return sortFields;
    }

    static class CustomFieldIssueSortComparator implements IssueComparator
    {
        private final CustomField customField;

        public CustomFieldIssueSortComparator(CustomField customField)
        {
            if (customField == null)
            {
                throw new NullPointerException("Custom field cannot be null.");
            }

            this.customField = customField;
        }

        public int compare(Issue issue1, Issue issue2)
        {
            // Use the custom field to sort issues.
            return this.customField.compare(issue1, issue2);
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final CustomFieldIssueSortComparator that = (CustomFieldIssueSortComparator) o;

            return customField.getId().equals(that.customField.getId());
        }

        public int hashCode()
        {
            return customField.getId().hashCode();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Field))
        {
            return false;
        }

        final Field field = (Field) o;

        String id = getId();
        String fieldId = field.getId();
        return !(id != null ? !id.equals(fieldId) : fieldId != null);
    }

    @Override
    public int hashCode()
    {
        return (getId() != null ? getId().hashCode() : 0);
    }

    /**
     * Constant lazy reference to the CustomFieldSearcher for this CustomFieldType.
     */
    class CustomFieldSearcherLazyRef extends ResettableLazyReference<CustomFieldSearcher>
    {
        private CustomFieldSearcher initialValue;

        CustomFieldSearcherLazyRef(CustomFieldSearcher customFieldSearcher)
        {
            this.initialValue = customFieldSearcher;
        }

        public CustomFieldSearcherLazyRef()
        {

        }

        @Override
        protected CustomFieldSearcher create() throws Exception
        {
            if (initialValue != null)
            {
                CustomFieldSearcher v = initialValue;
                initialValue = null;
                return v;
            }
            else
            {
                final String customFieldSearcherKey = gv.getString(ENTITY_CUSTOM_FIELD_SEARCHER);
                Option<CustomFieldSearcher> customFieldSearcher = customFieldSearcherModuleDescriptors.getCustomFieldSearcher(customFieldSearcherKey);

                if (customFieldSearcher.isDefined())
                {
                    try
                    {
                        customFieldSearcher.get().init(CustomFieldImpl.this);
                    }
                    catch (Exception exception)
                    {
                        // JRA-18412
                        // we don't want to prevent JIRA from continuing to operate so we swallow exceptions during initialization.
                        // returning null from here will result in the custom field not being searchable which is acceptable
                        // when something catastrophic happens.
                        log.error(String.format("Exception during searcher initialization of the custom field %s:", customFieldSearcherKey), exception);
                        return null;
                    }
                }
                return customFieldSearcher.getOrNull();
            }
        }
    }

    /**
     * Constant lazy reference for this CustomFieldImpl's CustomFieldType.
     */
    class CustomFieldTypeLazyRef extends LazyReference<CustomFieldType>
    {
        private CustomFieldType customFieldType;

        CustomFieldTypeLazyRef(CustomFieldType customFieldType)
        {
            this.customFieldType = customFieldType;
        }

        public CustomFieldTypeLazyRef()
        {

        }

        @Override
        protected CustomFieldType create() throws Exception
        {
            if (customFieldType == null)
            {
                final String customFieldKey = gv.getString(ENTITY_CF_TYPE_KEY);
                if (customFieldKey != null)
                {
                    customFieldType = customFieldTypeModuleDescriptors.getCustomFieldType(customFieldKey).getOrNull();
                }

            }
            return customFieldType;
        }
    }

   /**
     * Returns a generic value that represents this custom field. (only for internal use, it is deprecated on interface)
     *
     * @return generic value of this custom field
     */
    @Override
    public GenericValue getGenericValue()
    {
        return gv;
    }

    /**
     * Non-public method that returns a copy of the underlying GenericValue. This is essentially only used in the above
     * copy constructor.
     * <p/>
     * Prefer this class's get* method instead.
     *
     * @return the underlying GenericValue
     * @since 5.1
     */
    protected GenericValue copyGenericValue()
    {
        return new GenericValue(gv);
    }

    @Override
    public int compareTo(Object o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (o instanceof Field)
        {
            Field field = (Field) o;
            if (getName() == null)
            {
                if (field.getName() == null)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (field.getName() == null)
                {
                    return 1;
                }
                else
                {
                    return getName().compareTo(field.getName());
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Can only compare Field objects.");
        }

    }

    /**
     * Null-safe comparison of renderer type strings.
     *
     * @param oldRendererType old renderer type to compare
     * @param newRendererType new renderer type to compare
     * @return true if both are null or equal, false otherwise
     */
    private boolean rendererTypesEqual(String oldRendererType, String newRendererType)
    {
        return (oldRendererType == null && newRendererType == null)
                || ((oldRendererType != null) && oldRendererType.equals(newRendererType));
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof RestAwareCustomFieldType)
        {
            return ((RestAwareCustomFieldType) customFieldType).getFieldTypeInfo(fieldTypeInfoContext);
        }
        else
        {
            return new FieldTypeInfo(null, null);
        }
    }

    @Override
    public JsonType getJsonSchema()
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof RestAwareCustomFieldType)
        {
            return ((RestAwareCustomFieldType) customFieldType).getJsonSchema(this);
        }
        else
        {
            return JsonTypeBuilder.customArray(JsonType.STRING_TYPE, customFieldType.getKey(), getIdAsLong());
        }
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequested, FieldLayoutItem fieldLayoutItem)
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof RestAwareCustomFieldType)
        {
            return ((RestAwareCustomFieldType) customFieldType).getJsonFromIssue(this, issue, renderedVersionRequested, fieldLayoutItem);
        }
        else
        {
            Object value = customFieldType.getValueFromIssue(this, issue);
            if (value instanceof Collection)
            {
                // Convert to an array of strings as that is all we support generically
                final List<String> list = new ArrayList<String>();
                for (Object v : (Collection) value)
                {
                    list.add(v.toString());
                }
                return new FieldJsonRepresentation(new JsonData(list));
            }
            else
            {
                if (value != null)
                {
                    return new FieldJsonRepresentation(new JsonData(value.toString()));
                }
                else
                {
                    return new FieldJsonRepresentation(new JsonData(null));
                }
            }
        }
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof RestCustomFieldTypeOperations)
        {
            return ((RestCustomFieldTypeOperations) customFieldType).getRestFieldOperation(this);
        }
        return new TextCustomFieldOperationsHandler(this, getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof RestCustomFieldTypeOperations)
        {
            return ((RestCustomFieldTypeOperations) customFieldType).getJsonDefaultValue(issueCtx, this);
        }

        FieldConfig config = getRelevantConfig(issueCtx);
        if (config == null)
        {
            return null;
        }

        Object defaultValue = getCustomFieldType().getDefaultValue(config);
        return defaultValue == null ? null : new JsonData(defaultValue);
    }

    /**
     * Overridable location to retrieve an {@link EventPublisher} to publish events through.
     * It's not injected via the constructor because it has too many adverse impacts on APIs and existing usages.
     *
     * @return an {@link EventPublisher}.
     */
    protected EventPublisher getEventPublisher()
    {
        return ComponentAccessor.getComponent(EventPublisher.class);
    }

    @Override
    public PropertySet getPropertySet()
    {
        return propertySetRef.get();
    }
}
