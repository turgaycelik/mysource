package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Default implementation of service for managing auditing records
 *
 * @since v6.3
 */
public class DefaultAuditingService implements AuditingService
{
    public static final Integer MAX_LEN = 255;

    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final AuditingManager auditingManager;
    private final FeatureManager featureManager;
    private final JiraAuthenticationContext authenticationContext;
    private final PluginAccessor pluginAccessor;

    public DefaultAuditingService(final PermissionManager permissionManager, final I18nHelper.BeanFactory i18nFactory, final AuditingManager auditingManager, final FeatureManager featureManager, final JiraAuthenticationContext jiraAuthenticationContext, final PluginAccessor pluginAccessor)
    {
        this.permissionManager = permissionManager;
        this.i18nFactory = i18nFactory;
        this.auditingManager = auditingManager;
        this.featureManager = featureManager;
        this.authenticationContext = jiraAuthenticationContext;
        this.pluginAccessor = pluginAccessor;
    }

    @Nonnull
    @Override
    public ServiceOutcome<Records> getRecords(@Nullable final Integer offset, @Nullable final Integer maxResults, @Nullable final AuditingFilter filter)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final I18nHelper i18nFactory = this.i18nFactory.getInstance(user);
        if (user == null || !permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nFactory.getText("jira.auditing.service.no.admin.permission"), ErrorCollection.Reason.FORBIDDEN);
            return new ServiceOutcomeImpl<Records>(errorCollection, null);
        }

        final Records records;

        if (featureManager.isOnDemand())
        {
            records = auditingManager.getRecordsWithoutSysAdmin(null, null, maxResults, offset, filter);
        }
        else
        {
            records = auditingManager.getRecords(null, null, maxResults, offset, filter);
        }

        return new ServiceOutcomeImpl<Records>(errorCollection, records);
    }

    @Nonnull
    @Override
    public ErrorCollection storeRecord(@Nullable final String category, @Nullable final String summary,
            @Nonnull final String eventSourceKey, @Nullable final AssociatedItem objectItem,
            @Nullable final Iterable<ChangedValue> changedValues,
            @Nullable final Iterable<AssociatedItem> associatedItems)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final I18nHelper i18nBean = i18nFactory.getInstance(user);

        if (StringUtils.isEmpty(eventSourceKey))
        {
            final ErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage(i18nBean.getText("jira.auditing.service.event.source.not.empty"));
            return errorCollection;
        }

        final Plugin plugin = pluginAccessor.getPlugin(eventSourceKey);
        if (plugin == null)
        {
            final ErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addErrorMessage(i18nBean.getText("jira.auditing.service.event.plugin.not.found", eventSourceKey));
            return errorCollection;
        }

        return storeRecordData(category, summary, plugin.getName(), objectItem, changedValues, associatedItems);
    }

    @Nonnull
    @Override
    public ErrorCollection storeRecord(@Nonnull final String category, @Nonnull final String summary, @Nullable final AssociatedItem objectItem, @Nullable final Iterable<ChangedValue> changedValues, @Nullable final Iterable<AssociatedItem> associatedItems)
    {
        return storeRecordData(category, summary, "", objectItem, changedValues, associatedItems);
    }

    @Nonnull
    @Override
    public Long getTotalNumberOfRecords()
    {
        if (featureManager.isOnDemand())
        {
            return auditingManager.countRecordsWithoutSysAdmin(null, null);
        }
        else
        {
            return auditingManager.countRecords(null, null);
        }
    }

    protected void validateCategoryAndSummary(final String category, final String summary, final ErrorCollection errorCollection, final I18nHelper i18nBean)
    {
        if (isBlank(summary))
        {
            errorCollection.addError("summary", i18nBean.getText("jira.auditing.service.field.must.be.set", "summary"));
        } else if (summary.length() > MAX_LEN) {
            errorCollection.addError("summary", i18nBean.getText("createissue.error.summary.less.than", MAX_LEN.toString()));
        }

        if (isBlank(category))
        {
            errorCollection.addError("category", i18nBean.getText("jira.auditing.service.field.must.be.set", "category"));
        }
        else if (AuditingCategory.getCategoryById(category) == null)
        {
            errorCollection.addError("category", i18nBean.getText("jira.auditing.service.invalid.category", category));
        }
    }

    protected void validateChangedValues(@Nonnull final String fieldId, @Nonnull final Iterable<ChangedValue> values, @Nonnull final ErrorCollection errorCollection, @Nonnull final I18nHelper i18nBean)
    {
        int idx = 0;
        for (ChangedValue value : values)
        {
            validateChangedValue(fieldId + '[' + (idx++) + ']', value, errorCollection, i18nBean);
        }
    }

    protected void validateChangedValue(@Nonnull final String fieldId, @Nonnull final ChangedValue value,
            @Nonnull final ErrorCollection errorCollection, @Nonnull final I18nHelper i18nBean)
    {
        if (isBlank(value.getName()))
        {
            errorCollection.addError(fieldId, i18nBean.getText("jira.auditing.service.field.must.be.set", "name"));
        }
    }

    protected void validateAssociatedItems(@Nonnull final String fieldId, @Nonnull final Iterable<AssociatedItem> associatedItems,
            @Nonnull final ErrorCollection errorCollection, @Nonnull final I18nHelper i18nBean)
    {
        int idx = 0;
        for (AssociatedItem associatedItem : associatedItems)
        {
            validateAssociatedItem(fieldId + '[' + (idx++) + ']', associatedItem, errorCollection, i18nBean);
        }
    }

    protected void validateAssociatedItem(@Nonnull final String fieldId, @Nonnull final AssociatedItem objectItem,
            @Nonnull final ErrorCollection errorCollection, @Nonnull final I18nHelper i18nBean)
    {
        if (isBlank(objectItem.getObjectName()))
        {
            errorCollection.addError(fieldId, i18nBean.getText("jira.auditing.service.field.must.be.set", "name"));
        }
        if (AssociatedItem.Type.PROJECT.equals(objectItem.getObjectType()) && isBlank(objectItem.getObjectId()))
        {
            errorCollection.addError(fieldId, i18nBean.getText("jira.auditing.service.field.must.be.set", "id"));
        }
    }

    private ErrorCollection storeRecordData(final String category, final String summary, final String eventSourceName, final AssociatedItem objectItem, final Iterable<ChangedValue> changedValues, final Iterable<AssociatedItem> associatedItems)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final I18nHelper i18nBean = i18nFactory.getInstance(user);

        final ErrorCollection errorCollection = new SimpleErrorCollection();

        if (user == null || !permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errorCollection.addErrorMessage(i18nBean.getText("jira.auditing.service.no.admin.permission"));
            return errorCollection;
        }

        final String trimmedSummary = StringUtils.trimToEmpty(summary);
        validateCategoryAndSummary(category, trimmedSummary, errorCollection, i18nBean);
        if (errorCollection.hasAnyErrors())
        {
            return errorCollection;
        }

        final String trimmedEventSource = StringUtils.trimToEmpty(eventSourceName);
        final RecordRequest record = new RecordRequest(AuditingCategory.getCategoryById(category), trimmedSummary, trimmedEventSource);
        if (objectItem != null)
        {
            validateAssociatedItem("objectItem", objectItem, errorCollection, i18nBean);
            record.forObject(objectItem);
        }
        if (changedValues != null)
        {
            validateChangedValues("changedValues", changedValues, errorCollection, i18nBean);
            record.withChangedValues(changedValues);
        }
        if (associatedItems != null)
        {
            validateAssociatedItems("associatedItems", associatedItems, errorCollection, i18nBean);
            record.withAssociatedItems(associatedItems);
        }
        if (!errorCollection.hasAnyErrors())
        {
            auditingManager.store(record);
        }
        return errorCollection;
    }
}
