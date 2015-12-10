package com.atlassian.jira.auditing.handlers;

import java.util.List;
import java.util.Locale;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.auditing.AssociatedItem;
import com.atlassian.jira.auditing.AuditingCategory;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.notification.NotificationAddedEvent;
import com.atlassian.jira.event.notification.NotificationDeletedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEntityEvent;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.util.I18nHelper;

/**
 * @since v6.2
 */
public class NotificationChangeHandlerImpl implements NotificationChangeHandler
{
    public static final String SCHEME_UPDATED_I18N_KEY = "jira.auditing.notification.scheme.updated";

    private final NotificationSchemeManager notificationSchemeManager;
    private final NotificationTypeManager notificationTypeManager;
    private final EventTypeManager eventTypeManager;
    private final I18nHelper.BeanFactory i18n;

    public NotificationChangeHandlerImpl(NotificationSchemeManager notificationSchemeManager,
            NotificationTypeManager notificationTypeManager,
            EventTypeManager eventTypeManager,
            I18nHelper.BeanFactory i18n)
    {
        this.notificationSchemeManager = notificationSchemeManager;
        this.notificationTypeManager = notificationTypeManager;
        this.eventTypeManager = eventTypeManager;
        this.i18n = i18n;
    }

    @Override
    public RecordRequest onNotificationAddedEvent(final NotificationAddedEvent event)
    {
        final Scheme scheme = notificationSchemeManager.getSchemeObject(event.getSchemeId());

        return new RecordRequest(AuditingCategory.NOTIFICATIONS, SCHEME_UPDATED_I18N_KEY)
                .forObject(AssociatedItem.Type.SCHEME, scheme.getName(), scheme.getId())
                .withChangedValues(computeChangedValues(event));
    }

    @Override
    public RecordRequest onNotificationDeletedEvent(final NotificationDeletedEvent event)
    {
        final Scheme scheme = notificationSchemeManager.getSchemeObject(event.getSchemeId());

        return new RecordRequest(AuditingCategory.NOTIFICATIONS, SCHEME_UPDATED_I18N_KEY)
                .forObject(AssociatedItem.Type.SCHEME, scheme.getName(), scheme.getId())
                .withChangedValues(computeChangedValues(event));
    }

    private List<ChangedValue> computeChangedValues(final AbstractSchemeEntityEvent event)
    {
        final NotificationType schemeType = notificationTypeManager.getSchemeType(event.getType());

        final Long eventTypeId = unboxNumber(event.getEntityTypeId());

        final String eventName = eventTypeManager.getEventTypesMap().get(eventTypeId).getName();
        final String receiverType = schemeType.getDisplayName();
        String receiverName = StringUtils.defaultString(event.getParameter() != null ? schemeType.getArgumentDisplay(event.getParameter()) : "", "");

        if (GroupDropdown.DESC.equals(event.getType()) && StringUtils.isEmpty(receiverName))
        {
            // special case, group Anyone
            receiverName = getI18n().getText("common.sharing.shared.description.anyone");
        }

        final ChangedValuesBuilder builder = new ChangedValuesBuilder();

        if (event instanceof NotificationAddedEvent)
        {
            // set to
            builder.addIfDifferent("admin.common.words.event", "", eventName)
                    .addIfDifferent("admin.common.words.type", "", receiverType)
                    .addIfDifferent("admin.common.words.value", "", receiverName);
        }
        else if (event instanceof NotificationDeletedEvent)
        {
            // this is NotificationDeletedEvent, so set from
            builder.addIfDifferent("admin.common.words.event", eventName, "")
                    .addIfDifferent("admin.common.words.type", receiverType, "")
                    .addIfDifferent("admin.common.words.value", receiverName, "");
        } else {
            throw new UnsupportedOperationException("Missing handler for " + event.getClass().getSimpleName());
        }

        return builder.build();
    }

    protected I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return i18n.getInstance(Locale.ENGLISH);
    }

    private Long unboxNumber(Object number) {
        return Long.valueOf(number.toString());
    }
}
