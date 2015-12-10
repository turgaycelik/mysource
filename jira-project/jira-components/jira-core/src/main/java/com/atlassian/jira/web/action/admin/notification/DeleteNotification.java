/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class DeleteNotification extends SchemeAwareNotificationAction
{
    private Long id;
    private boolean confirmed = false;

    protected void doValidation()
    {
        if (id == null)
            addErrorMessage(getText("admin.errors.notifications.must.select.notification.to.delete"));
        if (!confirmed)
            addErrorMessage(getText("admin.errors.notifications.confirm.deletion"));
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getSchemeManager().deleteEntity(getId());
        if (getSchemeId() == null)
            return getRedirect("ViewNotificationSchemes.jspa");
        else
            return getRedirect("EditNotifications!default.jspa?schemeId=" + getSchemeId());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    private GenericValue getNotification() throws GenericEntityException
    {
        return getSchemeManager().getEntity(id);
    }

    public String getNotificationName() throws GenericEntityException
    {
        return getType(getNotification().getString("type")).getDisplayName();
    }

    public String getEventName() throws GenericEntityException
    {
        return ComponentAccessor.getEventTypeManager().getEventType(getNotification().getLong("eventTypeId")).getTranslatedName(getLoggedInUser());
    }

    public NotificationType getType(String id)
    {
        return ManagerFactory.getNotificationTypeManager().getNotificationType(id);
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getNotificationSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }
}
