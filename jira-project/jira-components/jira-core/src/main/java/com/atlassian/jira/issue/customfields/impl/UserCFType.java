package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.UserCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.ApplicationUserBestNameComparator;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.UserFilterConfigItem;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.impl.rest.UserCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.notification.type.UserCFNotificationTypeAware;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom Field allow selection of a single {@link ApplicationUser}. For multi-user see {@link MultiUserCFType}
 *
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link ApplicationUser}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link String} of user name</dd>
 * </dl>
 */
public class UserCFType extends AbstractSingleFieldType<ApplicationUser> implements SortableCustomField<ApplicationUser>, UserCFNotificationTypeAware, ProjectImportableCustomField, UserField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    public static final SharedEntity.TypeDescriptor<SharedEntity.Identifier> ENTITY_TYPE = SharedEntity.TypeDescriptor.Factory.get().create("UserPicker");

    private final ProjectCustomFieldImporter userCustomFieldImporter = new UserCustomFieldImporter();
    private final UserConverter userConverter;
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final ProjectManager projectManager;
    private final GroupManager groupManager;
    private final ProjectRoleManager projectRoleManager;
    private final UserPickerSearchService searchService;
    private final JiraBaseUrls jiraBaseUrls;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final UserHistoryManager userHistoryManager;
    private final UserFilterManager userFilterManager;
    private final UserPickerSearchService userPickerSearchService;
    private final I18nHelper i18nHelper;
    private final EmailFormatter emailFormatter;

    public UserCFType(final CustomFieldValuePersister customFieldValuePersister, final UserConverter userConverter,
            final GenericConfigManager genericConfigManager, final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext authenticationContext,
            final FieldConfigSchemeManager fieldConfigSchemeManager, final ProjectManager projectManager,
            final SoyTemplateRendererProvider soyTemplateRendererProvider,
            final GroupManager groupManager, final ProjectRoleManager projectRoleManager,
            final UserPickerSearchService searchService, JiraBaseUrls jiraBaseUrls,
            final UserHistoryManager userHistoryManager, final UserFilterManager userFilterManager,
            final UserPickerSearchService userPickerSearchService, I18nHelper i18nHelper, final EmailFormatter emailFormatter)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.userConverter = userConverter;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.projectManager = projectManager;
        this.groupManager = groupManager;
        this.projectRoleManager = projectRoleManager;
        this.searchService = searchService;
        this.jiraBaseUrls = jiraBaseUrls;
        this.emailFormatter = emailFormatter;
        this.soyTemplateRenderer = soyTemplateRendererProvider.getRenderer();
        this.userHistoryManager = userHistoryManager;
        this.userFilterManager = userFilterManager;
        this.userPickerSearchService = userPickerSearchService;
        this.i18nHelper = i18nHelper;
    }

    @Override
    public void updateValue(CustomField customField, Issue issue, ApplicationUser user)
    {
        super.updateValue(customField, issue, user);

        //JRADEV-14962 Don't add defaults to user history; we don't want to clobber genuine choices with defaults.
        final ApplicationUser loggedInUser = authenticationContext.getUser();
        final ApplicationUser defaultValue = getDefaultValue(customField.getRelevantConfig(issue));
        if (user != null && !user.equals(loggedInUser) && !user.equals(defaultValue))
        {
            userHistoryManager.addUserToHistory(UserHistoryItem.USED_USER, loggedInUser, user);
        }
    }

    @Override
    public String getChangelogValue(final CustomField field, final ApplicationUser value)
    {
        if (value == null)
        {
            return "";
        }
        return value.getKey();
    }

    @Override
    public String getChangelogString(final CustomField field, final ApplicationUser value)
    {
        if (value == null)
        {
            return "";
        }
        return value.getDisplayName();
    }

    @Override
    protected Object getDbValueFromObject(final ApplicationUser customFieldObject)
    {
        return userConverter.getDbString(customFieldObject);
    }

    @Override
    protected ApplicationUser getObjectFromDbValue(@Nonnull final Object databaseValue) throws FieldValidationException
    {
        return userConverter.getUserFromDbString((String) databaseValue);
    }

    public String getStringFromSingularObject(final ApplicationUser value)
    {
        return userConverter.getHttpParameterValue(value);
    }

    public ApplicationUser getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return userConverter.getUserFromHttpParameterWithValidation(string);
    }

    /**
     * This method validate the parameters based on the following criteria:
     * <ol>
     *     <li>If the specified new value is NOT a valid user, it fails.</li>
     *     <li>If the previous value is specified (obtained from the issue object in the "issue" key),
     *     and the specified new value is the same as the previous value,
     *     it succeeds and the rest of the validations are skipped.
     *     This is to be consistent with the Assignee field where even if the user might not pass
     *     the validation in the current context, as long as it does not change, we will let it go.
     *     This step relies on the caller to set the issue object into a parameter named "issue" in the {@code relevantParams} map.</li>
     *     <li>If a "requireProjectIds" parameter is specified and its value is "true", then the specified new value is
     *     checked against the user filtering criteria. Failing the check will immediately fail the whole validation.
     *     The "requireProjectIds" parameter indicates that the list of project id's is to be obtained from issue object or field config.
     *     The list of project id's might be required for validating project roles in the user filtering.</li>
     *     <li>Otherwise, the validation succeeds.</li>
     * </ol>
     * @param relevantParams the custom field parameters, containing the new value under the null key, and
     * optionally the previous value (username) as a single String item list under the key "oldValue", and also
     * optionally the list of projectIds in the current context, as a String list under the key "projectIds", and also
     * optionally a value to indicate whether to load project id's from field config with a single boolean item list
     * under the key "requireProjectIds".
     * @param errorCollectionToAddTo any validation errors will be added into this collection
     * @param config the field configuration
     */
    @Override
    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        try
        {
            final ApplicationUser newUser = super.getValueFromCustomFieldParams(relevantParams);
            // newUser would never be null here,
            // otherwise an exception would have been thrown from UserConverter.getUserFromHttpParameterWithValidation
            if (newUser == null)
            {
                return;
            }

            // if the issue id is passed in, we use it to check previous username
            final Long issueId = getLongFromParamsByKey(relevantParams, CustomFieldImpl.getParamKeyIssueId());
            if (issueId != null)
            {
                final ApplicationUser oldValue = getValueFromIssue(config.getCustomField(), issueId, null);
                // If same as previous value, skip the validation.
                // This is to keep the behaviour consistent with assignee, i.e., when updating an issue with other fields,
                // even though the value of the user picker became invalid due to some reason, as long as it's not
                // explicitly changed, we will leave it as it is.
                // However, different from assignee, the user assigned to the custom field could be deleted, thus
                // the next validation will fail at the previous step when we could not obtain a valid user from the username.
                if (newUser.equals(oldValue))
                {
                    return;
                }
            }

            // also validate against the user filters
            final UserFilter filter = userFilterManager.getFilter(config);
            if (filter == null)
            {
                throw new FieldValidationException(i18nHelper.getText("user.picker.errors.notconfigured"));
            }
            final Object requireProjectIds = relevantParams.getFirstValueForKey(CustomFieldUtils.getParamKeyRequireProjectIds());
            if (requireProjectIds == null || !Boolean.parseBoolean(requireProjectIds.toString()))
            {
                // skip validation based on user filter if project Id's are not required in the parameters,
                //  so that the behaviour of needMove and search input transformer remain unchanged.
                return;
            }

            final Long projectId = getLongFromParamsByKey(relevantParams, CustomFieldImpl.getParamKeyProjectId());
            final Collection<Long> projectIds = CustomFieldUtils.getProjectIdsFromProjectOrFieldConfig(projectId, config, fieldConfigSchemeManager, projectManager);
            final UserSearchParams userSearchParams = UserSearchParams.builder()
                                                        .filter(filter).filterByProjectIds(projectIds)
                                                        .build();
            if (!userPickerSearchService.userMatches(newUser, userSearchParams))
            {
                throw new FieldValidationException(i18nHelper.getText("user.picker.errors.notvaliduser", newUser.getUsername()));
            }
        }
        catch (final FieldValidationException e)
        {
            errorCollectionToAddTo.addError(config.getCustomField().getId(), e.getMessage(), Reason.VALIDATION_FAILED);
        }
    }

    /**
     * Retrieve the first value of the given param key and convert it to a Long object if it's a valid long in String format.
     * @return the parsed Long object, or null if invalid
     */
    private Long getLongFromParamsByKey(final CustomFieldParams relevantParams, final String key)
    {
        final Object obj = relevantParams.getFirstValueForKey(key);
        if (obj != null && obj instanceof String)
        {
            final long parsedLong = NumberUtils.toLong(obj.toString(), -1);
            if (parsedLong != -1)
            {
                return parsedLong;
            }
        }
        return null;
    }

    @Override
    public ApplicationUser getValueFromCustomFieldParams(final CustomFieldParams relevantParams) throws FieldValidationException
    {
        ApplicationUser value = null;
        try
        {
            value = super.getValueFromCustomFieldParams(relevantParams);
        }
        catch (final FieldValidationException e)
        {
            //ignore
        }

        return value;

    }

    public int compare(@Nonnull final ApplicationUser customFieldObjectValue1, @Nonnull final ApplicationUser customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return new ApplicationUserBestNameComparator(authenticationContext.getLocale()).compare(customFieldObjectValue1, customFieldObjectValue2);
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);

        final JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());

        final boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
        }
        final WebResourceManager webResourceManager = ComponentAccessor.getComponent(WebResourceManager.class);
        webResourceManager.requireResource("jira.webresources:autocomplete");
        velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        // add a parameter to signal CustomFieldUtils.buildParams() that this type requires the projectIdList parameter
        //  for edit html only
        velocityParams.put(CustomFieldUtils.getParamKeyRequireProjectIds(), Boolean.TRUE);
        return velocityParams;
    }

    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        List<FieldConfigItemType> types = super.getConfigurationItemTypes();
        types.add(new UserFilterConfigItem(groupManager, projectRoleManager, soyTemplateRenderer, userFilterManager));
        return types;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return userCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor) {
            return ((Visitor) visitor).visitUser(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitUser(UserCFType userCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        final String userPickerAutoCompleteUrl = String.format("%s/rest/api/1.0/users/picker?fieldName=%s&query=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getOderableField().getId());
        return new FieldTypeInfo(null, userPickerAutoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.custom(JsonType.USER_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(UserJsonBean.shortBean(getValueFromIssue(field, issue), jiraBaseUrls, authenticationContext.getUser(), emailFormatter)));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new UserCustomFieldOperationsHandler(field, getI18nBean());
    }

    @Override
    public Set<Long> remove(CustomField field)
    {
        userFilterManager.removeFilter(field.getIdAsLong());
        return super.remove(field);
    }
}
