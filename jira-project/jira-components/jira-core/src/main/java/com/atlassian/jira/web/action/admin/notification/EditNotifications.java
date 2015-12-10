/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.notification.ProjectNotificationsSchemeHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.template.Template;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

@WebSudoRequired
public class EditNotifications extends SchemeAwareNotificationAction
{
    private final ProjectNotificationsSchemeHelper helper;
    private List<Project> projects;

    public EditNotifications(final ProjectNotificationsSchemeHelper helper)
    {
        this.helper = helper;
    }

    public Map<Long,EventType> getEvents()
    {
        return ComponentAccessor.getEventTypeManager().getEventTypesMap();
    }

    public List getNotifications(Long eventTypeId) throws GenericEntityException
    {
        return getSchemeManager().getEntities(getScheme(), eventTypeId);
    }

    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getNotificationSchemeManager();
    }

    public String getRedirectURL()
    {
        return null;
    }

    public Template getTemplate(GenericValue notificationGV)
    {
        SchemeEntity notificationSchemeEntity = new SchemeEntity(notificationGV.getLong("id"), notificationGV.getString("type"), notificationGV.getString("parameter"), notificationGV.get("eventTypeId"), notificationGV.get("templateId"), notificationGV.getLong("scheme"));
        return getTemplateManager().getTemplate(notificationSchemeEntity);
    }

    public TemplateManager getTemplateManager()
    {
        return ComponentAccessor.getComponentOfType(TemplateManager.class);
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final Scheme notificationsScheme = getSchemeObject();
            projects = helper.getSharedProjects(notificationsScheme);
        }
        return projects;
    }
}
