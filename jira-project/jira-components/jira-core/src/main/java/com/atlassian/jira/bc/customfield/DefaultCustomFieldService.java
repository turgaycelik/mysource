package com.atlassian.jira.bc.customfield;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupCF;
import com.atlassian.jira.security.type.UserCF;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldContextConfigHelper;
import com.atlassian.jira.web.action.admin.customfields.CustomFieldValidator;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since v3.13
 */
public class DefaultCustomFieldService implements CustomFieldService
{
    private static final String CUSTOMFIELD_PREFIX = "customfield_";
    private static final String NONE_VALUE = "-1";

    private final GlobalPermissionManager permissionManager;
    private final CustomFieldManager customFieldManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final CustomFieldValidator customFieldValidator;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraContextTreeManager treeManager;
    private final CustomFieldContextConfigHelper customFieldContextConfigHelper;
    private final ReindexMessageManager reindexMessageManager;
    private final ConstantsManager constantManager;
    private final FieldScreenManager fieldScreenManager;
    private final LocaleManager localeManager;
    private final TranslationManager translationManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    public DefaultCustomFieldService(final GlobalPermissionManager permissionManager,
            final CustomFieldManager customFieldManager,
            final PermissionSchemeManager permissionSchemeManager,
            final IssueSecuritySchemeManager issueSecuritySchemeManager,
            final CustomFieldValidator customFieldValidator,
            final I18nHelper.BeanFactory i18nFactory,
            final JiraContextTreeManager treeManager,
            final CustomFieldContextConfigHelper customFieldContextConfigHelper,
            final ReindexMessageManager reindexMessageManager,
            final ConstantsManager constantManager,
            final FieldScreenManager fieldScreenManager,
            final LocaleManager localeManager,
            final TranslationManager translationManager,
            final ManagedConfigurationItemService managedConfigurationItemService)
    {
        this.permissionManager = permissionManager;
        this.customFieldManager = customFieldManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.customFieldValidator = customFieldValidator;
        this.i18nFactory = i18nFactory;
        this.treeManager = treeManager;
        this.customFieldContextConfigHelper = customFieldContextConfigHelper;
        this.reindexMessageManager = reindexMessageManager;
        this.constantManager = constantManager;
        this.fieldScreenManager = fieldScreenManager;
        this.localeManager = localeManager;
        this.translationManager = translationManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    @Override
    public ServiceOutcome<CustomField> getCustomFieldForEditConfig(@Nullable final ApplicationUser user, final String fieldId)
    {
        Preconditions.checkArgument(fieldId != null, "fieldId is null.");

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return ServiceOutcomeImpl.error(i18n(user).getText("admin.customfields.service.no.admin.permission"), ErrorCollection.Reason.FORBIDDEN);
        }
        final CustomField customFieldObject = customFieldManager.getCustomFieldObject(fieldId);
        if (customFieldObject != null)
        {
            final ManagedConfigurationItem managedCustomField = managedConfigurationItemService.getManagedCustomField(customFieldObject);
            if (managedConfigurationItemService.doesUserHavePermission(ApplicationUsers.toDirectoryUser(user), managedCustomField))
            {
                return ServiceOutcomeImpl.ok(customFieldObject);
            }
            else
            {
                final String message = i18n(user).getText("admin.managed.configuration.items.customfield.error.cannot.alter.configuration.locked", fieldId);
                return ServiceOutcomeImpl.error(message, ErrorCollection.Reason.FORBIDDEN);
            }
        }
        else
        {
            return ServiceOutcomeImpl.error(i18n(user).getText("admin.errors.customfields.invalid.custom.field"), ErrorCollection.Reason.NOT_FOUND);
        }
    }

    @Nonnull
    @Override
    public Iterable<CustomFieldType<?, ?>> getCustomFieldTypesForUser(final ApplicationUser user)
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return Collections.emptyList();
        }

        @SuppressWarnings ("unchecked")
        final List<CustomFieldType<?,?>> customFieldTypes = customFieldManager.getCustomFieldTypes();
        return Iterables.unmodifiableIterable(Iterables.filter(customFieldTypes, new Predicate<CustomFieldType<?, ?>>()
        {
            @Override
            public boolean apply(final CustomFieldType<?, ?> input)
            {
                final ConfigurationItemAccessLevel managedAccessLevel = input.getDescriptor().getManagedAccessLevel();
                return managedAccessLevel == null || managedConfigurationItemService.doesUserHavePermission(ApplicationUsers.toDirectoryUser(user), managedAccessLevel);
            }
        }));
    }

    @Nullable
    @Override
    public CustomFieldSearcher getDefaultSearcher(@Nonnull final CustomFieldType<?, ?> type)
    {
        return customFieldManager.getDefaultSearcher(type);
    }

    public void validateDelete(final JiraServiceContext jiraServiceContext, final Long customFieldId)
    {
        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return;
        }

        final CustomField customField = customFieldManager.getCustomFieldObject(customFieldId);
        if (customField == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.custom.field"));
            return;
        }
        validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldId, false);
    }

    public void validateUpdate(final JiraServiceContext jiraServiceContext, final Long customFieldId, final String name, final String description, final String searcherKey)
    {
        if (customFieldId == null)
        {
            throw new IllegalArgumentException("customFieldId can not be null.");
        }

        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return;
        }

        //ensure we're not updating a custom field that doesn't exist.
        final CustomField originalCustomField = customFieldManager.getCustomFieldObject(customFieldId);
        if (originalCustomField == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.custom.field"));
            return;
        }

        if (StringUtils.isEmpty(name))
        {
            jiraServiceContext.getErrorCollection().addError("name", i18nBean.getText("admin.errors.customfields.no.name"));
            return;
        }

        if (StringUtils.isNotEmpty(searcherKey) && !NONE_VALUE.equals(searcherKey) && (customFieldManager.getCustomFieldSearcher(searcherKey) == null))
        {
            jiraServiceContext.getErrorCollection().addError("searcher", i18nBean.getText("admin.errors.customfields.invalid.searcher"));
            return;
        }

        //setting the searcher to none...we need to check if it is used anywhere in a permission or issuelevel scheme.
        //see JRA-13808 for more info.
        if (StringUtils.isEmpty(searcherKey) || NONE_VALUE.equals(searcherKey))
        {
            validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldId, true);
        }
    }

    @Override
    public void validateTranslation(final JiraServiceContext jiraServiceContext, final Long customFieldId, final String name, final String description, final String locale)
    {
        if (customFieldId == null)
        {
            throw new IllegalArgumentException("customFieldId can not be null.");
        }

        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getLoggedInUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return;
        }

        //ensure we're not updating a custom field that doesn't exist.
        final CustomField originalCustomField = customFieldManager.getCustomFieldObject(customFieldId);
        if (originalCustomField == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.custom.field"));
            return;
        }

        // make sure the locale is valid
        if (StringUtils.isEmpty(locale) || localeManager.getLocale(locale) == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.locale"));
        }
    }

    @Override
    public void updateTranslation(final JiraServiceContext jiraServiceContext, final Long customFieldId, final String name, final String description, final String localeString)
    {
        final CustomField customField = customFieldManager.getCustomFieldObject(customFieldId);
        // make sure the locale is valid
        if (StringUtils.isEmpty(localeString) || localeManager.getLocale(localeString) == null)
        {
            final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.locale"));
        }
        Locale locale = localeManager.getLocale(localeString);
        translationManager.setCustomFieldTranslation(customField, locale, name, description);
    }

    /**
     * This method checks that the custom field provided is not used in any permission schemes or issue level security
     * schemes.  If it is used, an appropriate error message with a complete list of scheme names will be added to the
     * JiraServiceContext.
     *
     * @param jiraServiceContext JiraServiceContext
     * @param customFieldId ID of the CustomField
     * @param forUpdate used to determine the correct error message
     */
    void validateNotUsedInPermissionSchemes(final JiraServiceContext jiraServiceContext, final Long customFieldId, final boolean forUpdate)
    {
        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        // We need to find if this is used in any security schemes.
        final Set<GenericValue> usedPermissionSchemes = getUsedPermissionSchemes(customFieldId);
        // if we found any permissionSchemes that use this custom field, show an error.
        if (!usedPermissionSchemes.isEmpty())
        {
            String messageKey;
            if (forUpdate)
            {
                messageKey = "admin.errors.customfields.used.in.permission.scheme.update";
            }
            else
            {
                messageKey = "admin.errors.customfields.used.in.permission.scheme.delete";
            }
            jiraServiceContext.getErrorCollection().addErrorMessage(
                    i18nBean.getText(messageKey, GenericValueUtils.getCommaSeparatedList(usedPermissionSchemes, "name")));
        }
        //issuelevel security checks should only be done in enterprise edition!
        final Set<GenericValue> usedIssueLevelSecuritySchemes = getUsedIssueSecuritySchemes(customFieldId);
        // if we found any IssueLevelSecuritySchemes that use this Custom Field - then show error.
        if (!usedIssueLevelSecuritySchemes.isEmpty())
        {
            String messageKey;
            if (forUpdate)
            {
                messageKey = "admin.errors.customfields.used.in.issuelevelschemes.update";
            }
            else
            {
                messageKey = "admin.errors.customfields.used.in.issuelevelschemes.delete";
            }
            jiraServiceContext.getErrorCollection().addErrorMessage(
                    i18nBean.getText(messageKey, GenericValueUtils.getCommaSeparatedList(usedIssueLevelSecuritySchemes, "name")));
        }
    }

    @Override
    public ServiceOutcome<CreateValidationResult> validateCreate(final User user, final CustomFieldDefinition customFieldDefinition)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = i18nFactory.getInstance(user);
        if (user == null || !permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return new ServiceOutcomeImpl<CreateValidationResult>(errorCollection, null);
        }

        if (StringUtils.isBlank(customFieldDefinition.getCfType()))
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.errors.customfields.no.field.type.specified"));
            return new ServiceOutcomeImpl<CreateValidationResult>(errorCollection, null);
        }

        errorCollection.addErrorCollection(customFieldValidator.validateType(customFieldDefinition.getCfType()));
        if (errorCollection.hasAnyErrors())
        {
            return new ServiceOutcomeImpl<CreateValidationResult>(errorCollection, null);
        }

        //We have already validated that the type exists.
        final CustomFieldType customFieldType = customFieldManager.getCustomFieldType(customFieldDefinition.getCfType());
        final String searcherKey;
        if (customFieldDefinition.isUseDefaultSearcher())
        {
            final CustomFieldSearcher searcher = customFieldManager.getDefaultSearcher(customFieldType);
            searcherKey = searcher == null ? null : searcher.getDescriptor().getCompleteKey();
        }
        else
        {
            searcherKey = customFieldDefinition.getSearcherKey();
        }

        errorCollection.addErrorCollection(customFieldValidator.validateDetails(customFieldDefinition.getName(), customFieldDefinition.getCfType(), searcherKey));
        if (errorCollection.hasAnyErrors())
        {
            return new ServiceOutcomeImpl<CreateValidationResult>(errorCollection, null);
        }

        if (!customFieldDefinition.isGlobal() && customFieldDefinition.getProjectIds().isEmpty())
        {
            errorCollection.addError("projects", i18nBean.getText("admin.errors.must.select.project.for.non.global.contexts"));
            return new ServiceOutcomeImpl<CreateValidationResult>(errorCollection, null);//CreateValidationResult(errorCollection);
        }

        final List<Long> projects = customFieldDefinition.getProjectIds();
        final List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(customFieldDefinition.isGlobal(), new Long[0], projects.toArray(new Long[projects.size()]), treeManager);

        final List<GenericValue> returnIssueTypes;
        if (customFieldDefinition.isAllIssueTypes())
        {
            //watch out for a customField manager expecting a null in a list
            returnIssueTypes = Lists.newArrayList();
            returnIssueTypes.add(null);
        }
        else
        {
            final List<String> issueTypesIds = customFieldDefinition.getIssueTypeIds();
            returnIssueTypes = CustomFieldUtils.buildIssueTypes(constantManager, issueTypesIds.toArray(new String[issueTypesIds.size()]));
        }

        //we have validated searcher in validateDetails
        final CustomFieldSearcher customFieldSearcher = customFieldManager.getCustomFieldSearcher(searcherKey);
        final CreateValidationResult createValidationResult = CreateValidationResult.builder()
                .user(user)
                .customFieldType(customFieldType)
                .name(customFieldDefinition.getName())
                .description(customFieldDefinition.getDescription())
                .customFieldSearcher(customFieldSearcher)
                .contextNodes(contexts)
                .issueTypes(returnIssueTypes)
                .build();

        return new ServiceOutcomeImpl<CreateValidationResult>(errorCollection, createValidationResult);
    }

    @Override
    public ServiceOutcome<CustomField> create(final CreateValidationResult createValidationResult)
    {

        final CustomField customField;
        try
        {
            customField = customFieldManager.createCustomField(
                    createValidationResult.getName(),
                    createValidationResult.getDescription(),
                    createValidationResult.getCustomFieldType(),
                    createValidationResult.getCustomFieldSearcher(),
                    createValidationResult.getContextNodes(),
                    createValidationResult.getIssueTypes());

        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // if the resultant context contains issues, then we must also add a reindex message
        if (customFieldContextConfigHelper.doesAddingContextToCustomFieldAffectIssues(
                createValidationResult.getUser(),
                customField,
                createValidationResult.getContextNodes(),
                createValidationResult.getIssueTypes(),
                true))
        {
            reindexMessageManager.pushMessage(createValidationResult.getUser(), "admin.notifications.task.custom.fields");
        }

        return ServiceOutcomeImpl.ok(customField);
    }

    public ServiceOutcomeImpl<List<Long>> addToScreenTabs(final User user, final Long customFieldId, final List<Long> tabIds)
    {
        Preconditions.checkArgument(customFieldId != null, "customFieldId can not be null.");
        Preconditions.checkArgument(tabIds != null && !tabIds.isEmpty(), "List of tabs can not be null or empty.");

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = i18nFactory.getInstance(user);
        if (user == null || !permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return new ServiceOutcomeImpl<List<Long>>(errorCollection, Collections.EMPTY_LIST);
        }

        for (final Long screenId : tabIds)
        {
            final FieldScreenTab fieldScreenTab = fieldScreenManager.getFieldScreenTab(screenId);
            final String customFieldKey = "customfield_" + customFieldId;
            if (!fieldScreenTab.isContainsField(customFieldKey))
            {
                fieldScreenTab.addFieldScreenLayoutItem(customFieldKey);
            }
            else
            {
                errorCollection.addErrorMessage(i18nBean.getText("admin.errors.field.with.id.already.exists", customFieldKey));
                return new ServiceOutcomeImpl<List<Long>>(errorCollection, getListOfCurrentTabs(customFieldId));
            }
        }

        return new ServiceOutcomeImpl<List<Long>>(errorCollection, getListOfCurrentTabs(customFieldId));
    }


    public ServiceOutcomeImpl<List<Long>> removeFromScreenTabs(final User user, final Long customFieldId, final List<Long> tabIds)
    {
        Preconditions.checkArgument(customFieldId != null, "customFieldId can not be null.");
        Preconditions.checkArgument(tabIds != null && !tabIds.isEmpty(), "List of tabs can not be null or empty.");

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nBean = i18nFactory.getInstance(user);
        if (user == null || !permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return new ServiceOutcomeImpl<List<Long>>(errorCollection, Collections.EMPTY_LIST);
        }

        for (final Long screenId : tabIds)
        {
            final FieldScreenTab fieldScreenTab = fieldScreenManager.getFieldScreenTab(screenId);
            fieldScreenTab.getFieldScreen().removeFieldScreenLayoutItem("customfield_" + customFieldId);
        }

        return new ServiceOutcomeImpl<List<Long>>(errorCollection, getListOfCurrentTabs(customFieldId));
    }

    /**
     * Finds all the permission schemes where this customfield is used in a particular permission
     *
     * @param customFieldId ID of the CustomField
     * @return Set of {@link org.ofbiz.core.entity.GenericValue}s
     */
    @VisibleForTesting
    Set<GenericValue> getUsedPermissionSchemes(final Long customFieldId)
    {
        final Set<GenericValue> ret = new HashSet<GenericValue>();
        //Note: This is a bit of a hack, since there may be other custom field types than user and group, however
        // currently there's no better way of doing this, as there's no way to figure out the type from a customFieldId
        ret.addAll(permissionSchemeManager.getSchemesContainingEntity(UserCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        ret.addAll(permissionSchemeManager.getSchemesContainingEntity(GroupCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        return ret;
    }

    /**
     * Finds all the issue level security schemes where this customfield is used in a particular issue level.
     *
     * @param customFieldId ID of the CustomField
     * @return Set of {@link org.ofbiz.core.entity.GenericValue}s
     */
    @VisibleForTesting
    Set<GenericValue> getUsedIssueSecuritySchemes(final Long customFieldId)
    {
        final Set<GenericValue> ret = new HashSet<GenericValue>();
        //Note: This is a bit of a hack, since there may be other custom field types than user and group, however
        // currently there's no better way of doing this, as there's no way to figure out the type from a customFieldId
        ret.addAll(issueSecuritySchemeManager.getSchemesContainingEntity(UserCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        ret.addAll(issueSecuritySchemeManager.getSchemesContainingEntity(GroupCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        return ret;
    }

    private List<Long> getListOfCurrentTabs(final Long customFieldId)
    {
        return ImmutableList.copyOf(Iterables.transform(fieldScreenManager.getFieldScreenTabs("customfield_" + customFieldId), new Function<FieldScreenTab, Long>()
        {
            @Override
            public Long apply(final FieldScreenTab input)
            {
                return input.getId();
            }
        }));
    }

    private I18nHelper i18n(ApplicationUser user)
    {
        return i18nFactory.getInstance(user);
    }
}
