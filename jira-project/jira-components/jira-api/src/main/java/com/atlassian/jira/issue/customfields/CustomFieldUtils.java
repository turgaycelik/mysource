/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.customfields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectCategoryContext;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItem;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.render.Encoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.CommonVelocityKeys;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomFieldUtils
{
    public static final String CUSTOM_FIELD_PREFIX = "customfield_";

    /**
     * The prefix to be used by Atlassian defined custom field parameter names, so that we could avoid naming conflict
     * between Atlassian parameters and parameters used by third party plugin.
     *
     * @since v6.2.2
     */
    public static String getParamKeyPrefixAtl()
    {
        return "com.atlassian.jira.internal.";
    }

    /**
     * Name of the parameter that indicates whether the list of project id's associated with the current context
     *  should be loaded, either from the current issue (when editing value of an issue) or
     *  from FieldConfig and its FieldConfigScheme (when setting default value).
     * <p/>
     * This parameter could be used in two places now:
     * <ul>
     *     <li>In {@link com.atlassian.jira.issue.customfields.CustomFieldType#getVelocityParameters(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem)}
     *     to inform {@link #buildParams(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem, Object, java.util.Map, webwork.action.Action, java.util.Map)}
     *     that project id's need to be loaded</li>
     *     <li>In {@link com.atlassian.jira.issue.customfields.view.CustomFieldParams} to inform {@link com.atlassian.jira.issue.customfields.CustomFieldType#validateFromParams(com.atlassian.jira.issue.customfields.view.CustomFieldParams, com.atlassian.jira.util.ErrorCollection, com.atlassian.jira.issue.fields.config.FieldConfig)}
     *     that project id's need to be loaded from field config scheme.</li>
     * </ul>
     *
     * @since v6.2.2
     */
    public static String getParamKeyRequireProjectIds()
    {
        return getParamKeyPrefixAtl() + "requireProjectIds";
    }

    /**
     * @see {@link #getParamKeyRequireProjectIds()}
     * @since v6.2
     * @deprecated since v6.2.3 use {@link #getParamKeyRequireProjectIds()} instead.
     */
    @Deprecated
    public static final String PARAM_REQUIRE_PROJECT_IDS = "requireProjectIds";

    private static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/fields/view/";
    private static final char SEARCH_PARM_KEY_SEPARATOR = ':';

    public static String getSearchParamSuffix(final String searchParamKey)
    {
        if (searchParamKey == null)
        {
            return null;
        }

        final int indexOfSeparator = searchParamKey.indexOf(SEARCH_PARM_KEY_SEPARATOR);
        if ((indexOfSeparator != -1) && (indexOfSeparator != searchParamKey.length()))
        {
            return searchParamKey.substring(indexOfSeparator + 1);
        }
        else
        {
            return null;
        }
    }

    public static String getCustomFieldKey(final String searchParamKey)
    {
        final int indexOfSeparator = searchParamKey.indexOf(SEARCH_PARM_KEY_SEPARATOR);
        return indexOfSeparator == -1 ? searchParamKey : searchParamKey.substring(0, indexOfSeparator);
    }

    public static String createSearchParam(final String customFieldId, final String searchParam)
    {
        return customFieldId + SEARCH_PARM_KEY_SEPARATOR + searchParam;
    }

    /**
     * Given a custom field key, return its id or null.
     *
     * @param key eg. "customfield_10000"
     * @return Id, eg. 10000
     */
    public static Long getCustomFieldId(final String key)
    {
        if ((key == null) || !key.startsWith(CUSTOM_FIELD_PREFIX))
        {
            return null;
        }
        final int indexOfSeparator = key.indexOf(SEARCH_PARM_KEY_SEPARATOR);
        Long id;
        if (indexOfSeparator != -1)
        {
            id = new Long(key.substring(CUSTOM_FIELD_PREFIX.length(), indexOfSeparator));
        }
        else
        {
            String substring = key.substring(CUSTOM_FIELD_PREFIX.length());
            if (substring.endsWith(DateSearcherConfig.AFTER_SUFFIX))
            {
                substring = substring.substring(0, substring.length() - DateSearcherConfig.AFTER_SUFFIX.length());
            }
            else if (substring.endsWith(DateSearcherConfig.BEFORE_SUFFIX))
            {
                substring = substring.substring(0, substring.length() - DateSearcherConfig.BEFORE_SUFFIX.length());
            }
            id = new Long(substring);
        }

        return id;
    }

    public static boolean isCollectionNotEmpty(final Collection stringCollection)
    {
        if ((stringCollection != null) && !stringCollection.isEmpty())
        {
            for (final Object o : stringCollection)
            {
                // Check to see if object is != -1
                final boolean hasValue = ObjectUtils.isValueSelected(o);
                if (hasValue)
                {
                    return hasValue;
                }
            }
        }

        return false;
    }

    /**
     * Parses action parameters (Map of Collections of Strings with <customfield_<id>[:<key>]> as key)
     *
     * @param actionParameters map of action parameters
     * @param customFields     a list of custom fields
     * @return a map of custom field to its value, never null
     * @since 3.1-DEV
     */
    // @todo Badly needs a rewrite, currently very inefficient
    public static Map<CustomField, Object> parseCustomFieldValuesFromActionParams(final Map actionParameters, final List<CustomField> /* <CustomField> */customFields)
    {
        final Map<CustomField, Object> customFieldValues = new HashMap<CustomField, Object>();
        for (final CustomField customField : customFields)
        {
            customFieldValues.put(customField, customField.getValueFromParams(actionParameters));
        }
        return customFieldValues;
    }

    public static List<JiraContextNode> buildJiraIssueContexts(final boolean global, final Long[] projectCategories,
            final Long[] projects, final JiraContextTreeManager treeManager)
    {
        final ArrayList<JiraContextNode> contexts = new ArrayList<JiraContextNode>();

        if (global)
        {
            contexts.add(treeManager.getRootNode());
        }
        else
        {
            if (projectCategories != null)
            {
                for (final Long projectCategoryId : projectCategories)
                {
                    final ProjectCategory projectCategory = treeManager.getProjectManager().getProjectCategoryObject(projectCategoryId);
                    contexts.add(new ProjectCategoryContext(projectCategory, treeManager));
                }
            }

            if (projects != null)
            {
                for (final Long projectId : projects)
                {
                    final Project project = treeManager.getProjectManager().getProjectObj(projectId);
                    contexts.add(new ProjectContext(project, treeManager));
                }
            }
        }

        return contexts;
    }

    public static List<GenericValue> buildIssueTypes(final ConstantsManager constantsManager, final String[] issueTypes)
    {
        List<GenericValue> returnIssueTypes = null;
        if (issueTypes != null)
        {
            returnIssueTypes = new ArrayList<GenericValue>(issueTypes.length);
            for (final String issuetype : issueTypes)
            {
                if ("-1".equals(issuetype))
                {
                    returnIssueTypes.add(null);
                }
                else
                {
                    returnIssueTypes.add(constantsManager.getIssueType(issuetype));
                }
            }
        }
        return returnIssueTypes;
    }

    /**
     * Converts list of issue type ids to {@link com.atlassian.jira.issue.context.IssueContext IssueContexts}.
     *
     * @param project          project generic value
     * @param issueTypeIds     Type ids as strings, eg. ["3", "4"]
     * @return a list of issue contexts, never null
     * @deprecated Please use {@link #convertToIssueContexts(Project, List)}. Since v4.0
     */
    public static List<IssueContext> convertToIssueContexts(final GenericValue project, final List<String> issueTypeIds)
    {
        final Long projectId = project == null ? null : project.getLong("id");
        return convertToIssueContexts(projectId, issueTypeIds);
    }

    /**
     * Converts list of issue type ids to {@link com.atlassian.jira.issue.context.IssueContext IssueContexts}.
     *
     * @param project          The project (may be null).
     * @param issueTypeIds     Type ids as strings, eg. ["3", "4"]. May be null or empty.
     * @return a list of issue contexts, never null
     */
    public static List<IssueContext> convertToIssueContexts(final Project project, final List<String> issueTypeIds)
    {
        final Long projectId = project == null ? null : project.getId();
        return convertToIssueContexts(projectId, issueTypeIds);
    }

    // TODO: Would it be useful to make this public?
    private static List<IssueContext> convertToIssueContexts(final Long projectId, final List<String> issueTypeIds)
    {
        final List<IssueContext> issueContexts = new ArrayList<IssueContext>();
        if ((issueTypeIds != null) && !issueTypeIds.isEmpty())
        {
            for (final String issueTypeId : issueTypeIds)
            {
                issueContexts.add(new IssueContextImpl(projectId, issueTypeId));
            }
        }
        else
        // Project *can* be null. For issue navigator. This will match global scope
        {
            // Null IssueTypes means "any" are allowed.
            // TODO: Document why empty issueTypeIds List is treated the same as null.
            issueContexts.add(new IssueContextImpl(projectId, null));
        }
        return issueContexts;
    }

    /**
     * Does the user have permission to at least one project that falls under this custom field. If the custom field
     * has not been configured for anything, it won't return true either
     *
     * @param customField custom field
     * @param user        user
     * @return true if user has permission to at least one project that falls under this custom field, false otherwise
     */
    public static boolean isUserHasPermissionToProjects(final CustomField customField, final User user)
    {
        if (customField.isAllProjects())
        {
            return true;
        }
        else
        {
            final List<Project> projects = customField.getAssociatedProjectObjects();
            if ((projects != null) && !projects.isEmpty())
            {
                for (final Project project : projects)
                {
                    if (ComponentAccessor.getComponent(PermissionManager.class).hasPermission(Permissions.BROWSE, project, user))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // @TODO WMC pull this out to velocity
    public static String prettyPrintOptions(final Options options)
    {
        Encoder encoder = ComponentAccessor.getComponent(Encoder.class);
        if (encoder == null)
        {
            encoder = new NullEncoder();
        }

        final StringBuilder sb = new StringBuilder();
        if ((options != null) && !options.isEmpty())
        {
            sb.append("<ul class=\"optionslist\">");
            for (final Option option : options)
            {
                sb.append("<li>");
                sb.append(encoder.encodeForHtml(option.getValue())).append(option.getDisabled() ? " (" + getI18nBean().getText("admin.common.words.disabled") + ")" : "");
                final List childOptions = option.getChildOptions();
                if ((option.getChildOptions() != null) && !childOptions.isEmpty())
                {
                    sb.append("<ul>");
                    for (Object childOption : childOptions)
                    {
                        sb.append("<li>").append(encoder.encodeForHtml(((Option) childOption).getValue())).append("</li>");
                    }
                    sb.append("</ul>");
                }
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        else
        {
            sb.append("No options configured.");
        }

        return sb.toString();
    }

    // http://confluence.atlassian.com/display/JIRA/Custom+field+Velocity+context+unwrapped

    public static Map<String, Object> buildParams(final CustomField customField, final FieldConfig config,
            final Issue issue, final FieldLayoutItem fieldLayoutItem, final Object value,
            final Map customFieldValuesHolder, final Action action, final Map displayParameters)
    {
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("customField", customField)
                .add("issue", issue)
                .add("fieldLayoutItem", fieldLayoutItem)
                .add("action", action)
                .add(CommonVelocityKeys.DISPLAY_PARAMETERS, displayParameters)
                .add(CommonVelocityKeys.DISPLAY_PARAMS, displayParameters)
                .add("fieldValuesHolder", customFieldValuesHolder)
                .toMutableMap();

        if (displayParameters != null)
        {
            params.put(CommonVelocityKeys.READ_ONLY, (displayParameters.get("readonly") == null ? Boolean.FALSE: displayParameters.get("readonly")));
            params.put(CommonVelocityKeys.TEXT_ONLY, (displayParameters.get("textOnly") == null ? Boolean.FALSE: displayParameters.get("textOnly")));
            params.put(CommonVelocityKeys.EXCEL_VIEW, (displayParameters.get("excel_view") == null ? Boolean.FALSE: displayParameters.get("excel_view")));
            params.put(CommonVelocityKeys.NO_LINK, (displayParameters.get("nolink") == null ? Boolean.FALSE: displayParameters.get("nolink")));
            params.put(CommonVelocityKeys.PREFIX, (displayParameters.get("prefix") == null ? "": displayParameters.get("prefix")));

        }
        else
        {
            params.put(CommonVelocityKeys.READ_ONLY, Boolean.FALSE);
            params.put(CommonVelocityKeys.TEXT_ONLY, Boolean.FALSE);
            params.put(CommonVelocityKeys.EXCEL_VIEW, Boolean.FALSE);
            params.put(CommonVelocityKeys.NO_LINK, Boolean.FALSE);
            params.put(CommonVelocityKeys.PREFIX, "");
        }

        // express if there is a calendar translation file for the current language
        final String language = ComponentAccessor.getComponent(JiraAuthenticationContext.class).getLocale().getLanguage();
        params.put("hasCalendarTranslation", ComponentAccessor.getComponent(CalendarLanguageUtil.class).hasTranslationForLanguage(language));
        params.put("calendarIncluder", new CalendarResourceIncluder());
        if (customField != null && customField.isRenderable() && (fieldLayoutItem != null))
        {
            params.put("rendererDescriptor", ComponentAccessor.getComponent(RendererManager.class).getRendererForType(fieldLayoutItem.getRendererType()).getDescriptor());
            params.put("rendererParams", new HashMap());
        }
        params.put("auiparams", new HashMap<String, Object>());

        // Add the values iff value != null && customFieldValuesHolder == null
        if (customField == null || ((customFieldValuesHolder == null) && (value != null)))
        {
            params.put("value", value);
        }
        else
        {
            final CustomFieldParams customFieldParams = customField.getCustomFieldValues(customFieldValuesHolder);
            params.put("value", customField.getCustomFieldType().getStringValueFromCustomFieldParams(customFieldParams));
            params.put("customFieldParams", customFieldParams);
        }

        // Add the static params to be added from custom field types
        if (customField != null)
        {
            final Map<String, Object> velocityParameters = customField.getCustomFieldType().getVelocityParameters(issue, customField, fieldLayoutItem);
            if (velocityParameters != null)
            {
                // Hacky way to load projectIdList for only those types that need it.
                // Those CustomFieldType that need this should set this key to true in their getVelocityParameters() implementation.
                // Currently only UserCFType requires it due to user filtering.
                // Hopefully this is less hacky than using instanceof on customField.getCustomFieldType() to check for UserCFType
                //  and more efficient than always setting this parameters for all custom field types.
                if (velocityParameters.containsKey(getParamKeyRequireProjectIds()))
                {
                    params.put("projectIdList", getProjectIdsFromIssueOrFieldConfig(issue, config,
                            ComponentAccessor.getFieldConfigSchemeManager(),
                            ComponentAccessor.getProjectManager()));
                }
                params.putAll(velocityParameters);
            }
        }

        // Add javascript date time format params
        params.put("dateFormat", getDateFormat());
        params.put("dateTimeFormat", getDateTimeFormat());
        params.put("timeFormat", getTimeFormat());

        // Add config & config items
        if (config != null)
        {
            params.put("config", config);
            final List<FieldConfigItem> configItems = config.getConfigItems();
            if ((configItems != null) && !configItems.isEmpty())
            {
                final Map<String, Object> configs = new HashMap<String, Object>(configItems.size());
                for (final FieldConfigItem configItem : configItems)
                {
                    configs.put(configItem.getObjectKey(), configItem.getConfigurationObject(issue));
                }
                params.put("configs", configs);
            }
        }

        return params;
    }

    /**
     * Returns the configured Javascript date picker format.
     * <p>
     * ie the format stored in the "jira.date.picker.javascript.format" application property.
     *
     * @return the configured Javascript date picker format.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatUtils#getDateFormat()} instead. Since v5.0.
     */
    public static String getDateFormat()
    {
        return ComponentAccessor.getComponent(ApplicationProperties.class).getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVASCRIPT_FORMAT);
    }

    /**
     * Returns the configured Javascript date-time picker format.
     * <p>
     * ie the format stored in the "jira.date.time.picker.javascript.format" application property.
     *
     * @return the configured Javascript date-time picker format.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatUtils#getDateTimeFormat()} instead. Since v5.0.
     */
    public static String getDateTimeFormat()
    {
        return ComponentAccessor.getComponent(ApplicationProperties.class).getDefaultBackedString(APKeys.JIRA_DATE_TIME_PICKER_JAVASCRIPT_FORMAT);
    }

    /**
     * Returns "12" or "24" from the Javascript date-time picker format.
     *
     * @return "12" or "24" from the Javascript date-time picker format.
     *
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatUtils#getTimeFormat()} instead. Since v5.0.
     */
    public static String getTimeFormat()
    {
        final String dateTimeFormat = getDateTimeFormat();
        if (dateTimeFormat != null)
        {
            if ((dateTimeFormat.indexOf("%H") > -1) || (dateTimeFormat.indexOf("%R") > -1) || (dateTimeFormat.indexOf("%k") > -1))
            {
                return "24";
            }
        }

        return "12";
    }

    protected static I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
     }

    /**
     * Checks if the custom field is in the search context scope (using {@link com.atlassian.jira.issue.fields.CustomField#isShown(com.atlassian.jira.issue.Issue)})
     * and visible in all field schemes (using {@link com.atlassian.jira.web.FieldVisibilityManager#isFieldHiddenInAllSchemes(String,SearchContext,User)})
     *
     * @param customField         customfield to check visibility of
     * @param user                current user
     * @param searchContext       search context
     * @param fieldVisibilityManager field visibility bean
     * @return true if the customfield is in scope and visibile in all schemes in the specified search context
     * @see com.atlassian.jira.issue.fields.CustomField#isShown(com.atlassian.jira.issue.Issue)
     * @see com.atlassian.jira.web.FieldVisibilityManager#isFieldHiddenInAllSchemes(String,com.atlassian.jira.issue.search.SearchContext,User)
     */
    public static boolean isShownAndVisible(final CustomField customField, final User user,
            final SearchContext searchContext, final FieldVisibilityManager fieldVisibilityManager)
    {
        return customField.isInScope(searchContext) && !fieldVisibilityManager.isFieldHiddenInAllSchemes(customField.getId(), searchContext, user);
    }

    /**
     * Retrieve the list of project id's from issue or field config, in the following order:
     * <ol>
     *     <li>If {@code issue} is not null and issue.getProjectId() is not null, return a single list with issue.getProjectId()</li>
     *     <li>If {@code config} is not null, use it to retrieve the list of project id's associated with the current scheme</li>
     *     <li>Otherwise, return null</li>
     * </ol>
     *
     * @since v6.2
     */
    public static Collection<Long> getProjectIdsFromIssueOrFieldConfig(
            final Issue issue, final FieldConfig config,
            final FieldConfigSchemeManager fieldConfigSchemeManager, final ProjectManager projectManager)
    {
        return getProjectIdsFromProjectOrFieldConfig(issue == null ? null : issue.getProjectId(), config, fieldConfigSchemeManager, projectManager);
    }

    /**
     * Retrieve the list of project id's from issue or field config, in the following order:
     * <ol>
     *     <li>If {@code issue} is not null and issue.getProjectId() is not null, return a single list with issue.getProjectId()</li>
     *     <li>If {@code config} is not null, use it to retrieve the list of project id's associated with the current scheme</li>
     *     <li>Otherwise, return null</li>
     * </ol>
     *
     * @since v6.2.2
     */
    public static Collection<Long> getProjectIdsFromProjectOrFieldConfig(
            final Long projectIdFromIssue, final FieldConfig config,
            final FieldConfigSchemeManager fieldConfigSchemeManager, final ProjectManager projectManager)
    {
        // JRA-7659 get projectIds and add them into the context for user picker so that
        //          it could use it to check against project roles for user filtering
        if (projectIdFromIssue != null)
        {
            // If project id could be obtained from issue, we use it.
            //  This is when creating/editing an issue
            return ImmutableList.of(projectIdFromIssue);
        }
        if (config != null)
        {
            // If this is from EditDefaultValue page in custom field config, projectIds could be obtained from configFieldScheme.
            return getProjectIdsFromFieldConfig(config, fieldConfigSchemeManager, projectManager);
        }
        return null;
    }

    /**
     * Retrieve the list of project id's from the given {@code fieldConfigScheme}.
     * If the scheme is global, return all project id's in the system.
     */
    private static Collection<Long> getProjectIdsFromFieldConfig(FieldConfig fieldConfig, FieldConfigSchemeManager fieldConfigSchemeManager, ProjectManager projectManager)
    {
        final FieldConfigScheme fieldConfigScheme = fieldConfigSchemeManager.getConfigSchemeForFieldConfig(fieldConfig);
        if (fieldConfigScheme != null && fieldConfigScheme.isEnabled())
        {
            // We include all project id's if the config is global
            if (fieldConfigScheme.isGlobal())
            {
                // following a logic similar to that of ManageConfigurationScheme.getAllProjects()
                final Collection<Long> allProjectIds = Collections2.transform(projectManager.getProjectObjects(), getProjectIdFunction());
                final Collection<Long> associatedProjectIds = Collections2.transform(fieldConfig.getCustomField().getAssociatedProjectObjects(), getProjectIdFunction());
                // subtract those that are associated with other schemes
                return Sets.difference(ImmutableSet.copyOf(allProjectIds), ImmutableSet.copyOf(associatedProjectIds));
            }
            else
            {
                return fieldConfigScheme.getAssociatedProjectIds();
            }
        }
        return null;
    }

    /**
     * If {@code projectIds} is not empty, return it.
     * Otherwise, return the list of project id's that the {@code user} could browse.
     * <p/>
     * If {@code projectIds} is not provided and {@code userFilter} contains no roleIds, the list of browsable project
     *  ids will not be computed. An empty list will be returned instead.
     *
     * @since v6.2
     */
    public static Collection<Long> getProjectIdsForUser(ApplicationUser user, Collection<Long> projectIds, PermissionManager permissionManager, final UserFilter userFilter)
    {
        if (CollectionUtils.isEmpty(projectIds))
        {
            if (userFilter != null && CollectionUtils.isNotEmpty(userFilter.getRoleIds()))
            {
                // if no project is configured, take all projects browsable by the user
                return Collections2.transform(permissionManager.getProjects(Permissions.BROWSE, user), getProjectIdFunction());
            }
            else
            {
                // if no roles id, it means project ids is not required at all
                return ImmutableList.of();
            }
        }
        else
        {
            return projectIds;
        }
    }

    private static Function<Project, Long> getProjectIdFunction()
    {
        return new Function<Project, Long>()
        {
            @Override
            public Long apply(final Project project)
            {
                return project.getId();
            }
        };
    }

    private static class NullEncoder implements Encoder
    {
        @Override
        @Nonnull
        public String encodeForHtml(@Nullable Object input)
        {
            return input != null ? input.toString() : "";
        }
    }
}
