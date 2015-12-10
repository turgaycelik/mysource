package com.atlassian.jira.auditing;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.auditing.handlers.FieldLayoutSchemeChangeHandler;
import com.atlassian.jira.auditing.handlers.GroupEventHandler;
import com.atlassian.jira.auditing.handlers.NotificationChangeHandler;
import com.atlassian.jira.auditing.handlers.PermissionChangeHandler;
import com.atlassian.jira.auditing.handlers.ProjectComponentEventHandler;
import com.atlassian.jira.auditing.handlers.ProjectEventHandler;
import com.atlassian.jira.auditing.handlers.SchemeEventHandler;
import com.atlassian.jira.auditing.handlers.UserEventHandler;
import com.atlassian.jira.auditing.handlers.VersionEventHandler;
import com.atlassian.jira.auditing.handlers.WorkflowEventHandler;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since v6.2
 */
public class AuditingManagerImpl implements AuditingManager, Startable
{
    private final AuditingStore auditingStore;
    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private final FeatureManager featureManager;
    private final AuditingEventListener auditingEventListener;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final BarrierFactory barrierFactory;

    public AuditingManagerImpl(final AuditingStore auditingStore, final ApplicationProperties applicationProperties,
            final EventPublisher eventPublisher, final FeatureManager featureManager,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager,
            final PermissionChangeHandler permissionChangeHandler, final GroupEventHandler groupEventHandler,
            final SchemeEventHandler schemeEventHandler, final UserEventHandler userEventHandler,
            final WorkflowEventHandler workflowEventHandler, final NotificationChangeHandler notificationChangeHandler,
            final FieldLayoutSchemeChangeHandler fieldLayoutSchemeChangeHandler,
            final ProjectEventHandler projectEventHandler, final BarrierFactory barrierFactory,
            final ProjectComponentEventHandler projectComponentEventHandler, final VersionEventHandler versionEventHandler)
    {
        this.auditingStore = auditingStore;
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.featureManager = featureManager;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.barrierFactory = barrierFactory;
        this.auditingEventListener = new AuditingEventListener(this, permissionChangeHandler, groupEventHandler,
                schemeEventHandler, userEventHandler, workflowEventHandler, notificationChangeHandler,
                fieldLayoutSchemeChangeHandler, projectEventHandler, projectComponentEventHandler, versionEventHandler);
    }

    @Override
    public void store(RecordRequest record)
    {
        final ApplicationUser author = record.getAuthor() != null ? record.getAuthor() : getLoggedInUser();
        auditingStore.storeRecord(record.getCategory(), record.getCategoryName(), record.getSummary(), record.getEventSource(),
                author,StringUtils.defaultString(record.getRemoteAddress(), getRemoteAddress()),
                record.getObjectItem(), record.getChangedValues(), record.getAssociatedItems(),
                permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, author));
    }

    @Override
    @Nonnull
    public Records getRecords(@Nullable Long maxId, @Nullable Long sinceId, @Nullable Integer maxResults, @Nullable Integer offset, @Nullable AuditingFilter filter)
    {
        return getRecords(maxId, sinceId, maxResults, offset, filter, true);
    }

    @Override
    @Nonnull
    public Records getRecordsWithoutSysAdmin(@Nullable Long maxId, @Nullable Long sinceId, @Nullable Integer maxResults, @Nullable Integer offset, @Nullable AuditingFilter filter)
    {
        return getRecords(maxId, sinceId, maxResults, offset, filter, false);
    }

    @Override
    public long countRecords(@Nullable Long maxId, @Nullable Long sinceId)
    {
        return countRecords(maxId, sinceId, true);
    }

    @Override
    public long countRecordsWithoutSysAdmin(@Nullable Long maxId, @Nullable Long sinceId)
    {
        return countRecords(maxId, sinceId, false);
    }

    protected Records getRecords(@Nullable final Long maxId, @Nullable final Long sinceId, @Nullable final Integer maxResults,
            final Integer offset, final @Nullable AuditingFilter filter, final boolean includeSysAdminActions)
    {
        barrierFactory.getBarrier("auditingGetRecords").await();
        return auditingStore.getRecords(maxId, sinceId, maxResults, offset, filter, includeSysAdminActions);
    }

    protected long countRecords(@Nullable final Long maxId, @Nullable final Long sinceId, final boolean includeSysAdminActions)
    {
        return auditingStore.countRecords(maxId, sinceId, includeSysAdminActions);
    }

    @Nullable
    protected String getRemoteAddress()
    {
        return ExecutingHttpRequest.get() != null ? ExecutingHttpRequest.get().getRemoteAddr() : null;
    }

    protected ApplicationUser getLoggedInUser()
    {
        return authenticationContext.getUser();
    }

    static protected I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return ComponentAccessor.getI18nHelperFactory().getInstance(Locale.ENGLISH);
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(auditingEventListener);
        ensureDefaultRetentionPeriodForOnDemand();
    }

    /**
     * For OnDemand we do not offer "unlimited" option for Audit log retention so if such option is selected
     * we change it to default OnDemand value AuditingManagerImpl#DEFAULT_RETENTION_PERIOD_FOR_ONDEMAND
     */
    private void ensureDefaultRetentionPeriodForOnDemand()
    {
        if (featureManager.isOnDemand())
        {
            final AuditingRetentionPeriod retentionPeriod = AuditingRetentionPeriod.getByValue(applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_AUDITING_LOG_RETENTION_PERIOD_IN_MONTHS));
            if (retentionPeriod != null && retentionPeriod.isUnlimited())
            {
                applicationProperties.setString(APKeys.JIRA_OPTION_AUDITING_LOG_RETENTION_PERIOD_IN_MONTHS, AuditingRetentionPeriod.getDefault().getValue());
                applicationProperties.setString(APKeys.JIRA_OPTION_AUDITING_LOG_RETENTION_PERIOD_LAST_CHANGE_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
            }
        }
    }
}
