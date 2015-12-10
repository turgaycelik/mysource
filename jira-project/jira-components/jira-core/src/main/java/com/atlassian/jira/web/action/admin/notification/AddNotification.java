/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import java.util.Map;

@WebSudoRequired
public class AddNotification extends SchemeAwareNotificationAction
{
    private String type;
    private Long[] eventTypeIds;

    private final EventTypeManager eventTypeManager;
    private final NotificationTypeManager notificationTypeManager;
    private final NotificationSchemeManager notificationSchemeManager;

    public AddNotification(final EventTypeManager eventTypeManager, final NotificationTypeManager notificationTypeManager,
            final NotificationSchemeManager notificationSchemeManager)
    {
        this.eventTypeManager = eventTypeManager;
        this.notificationTypeManager = notificationTypeManager;
        this.notificationSchemeManager = notificationSchemeManager;
    }

    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getNotificationSchemeManager();
    }

    public String getRedirectURL()
    {
        return "EditNotifications!default.jspa?schemeId=";
    }

    protected void doValidation()
    {
        try
        {
            if (getSchemeId() == null || getScheme() == null)
            {
                addErrorMessage(getText("admin.errors.notifications.must.select.scheme"));
            }
            if (getEventTypeIds() == null || getEventTypeIds().length == 0)
            {
                addError("eventTypeIds", getText("admin.errors.notifications.must.select.notification.to.add"));
            }
            if (!TextUtils.stringSet(getType()))
            {
                addErrorMessage(getText("admin.errors.notifications.must.select.type"));
            }
            else if (!notificationTypeManager.getNotificationType(getType()).doValidation(getType(), getParameters()))
            {
                addErrorMessage(getText("admin.errors.notifications.fill.out.box"));
            }
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.notifications.error.occured", "\n") + e.getMessage());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        for (Long eventTypeId : eventTypeIds)
        {
            final String displayValue = getParameter(getType());
            final String rawValue = notificationTypeManager.getNotificationType(getType()).getArgumentValue(displayValue);
            SchemeEntity schemeEntity = new SchemeEntity(getType(), rawValue, eventTypeId, null);

            //prevent adding the same event multiple times
            if (!notificationSchemeManager.hasEntities(getScheme(), eventTypeId, type, rawValue, null))
            {
                notificationSchemeManager.createSchemeEntity(getScheme(), schemeEntity);
            }
        }

        return getRedirect(getRedirectURL() + getSchemeId());
    }

    public Map<String, NotificationType> getTypes()
    {
        return notificationTypeManager.getTypes();
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getParameters()
    {
        return ActionContext.getSingleValueParameters();
    }

    public String getParameter(String key)
    {
        return (String) getParameters().get(key);
    }

    public Map<Long,EventType> getEvents()
    {
        return eventTypeManager.getEventTypesMap();
    }

    public Long[] getEventTypeIds()
    {
        return eventTypeIds;
    }

    public void setEventTypeIds(Long[] eventTypeIds)
    {
        this.eventTypeIds = eventTypeIds;
    }

}
