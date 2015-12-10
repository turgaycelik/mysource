/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.ProjectActionSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract action useful for creating and editing projects
 */
public abstract class AbstractProjectAction extends ProjectActionSupport
{
    private static final Map<String,String> ERROR_FIELD_MAPPINGS;

    static
    {
        ERROR_FIELD_MAPPINGS = new HashMap<String,String>();
        ERROR_FIELD_MAPPINGS.put(ProjectService.PROJECT_DESCRIPTION, "description");
        ERROR_FIELD_MAPPINGS.put(ProjectService.PROJECT_KEY, "key");
        ERROR_FIELD_MAPPINGS.put(ProjectService.PROJECT_LEAD, "lead");
        ERROR_FIELD_MAPPINGS.put(ProjectService.PROJECT_NAME, "name");
        ERROR_FIELD_MAPPINGS.put(ProjectService.PROJECT_URL, "url");
    }

    private String name;
    private String key;
    private String lead;
    private Long avatarId;
    private Long assigneeType;
    private String url;
    private String description;
    private Long notificationScheme;
    private Long permissionScheme;
    private Long issueSecurityScheme;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLead()
    {
        return lead;
    }

    public void setLead(String lead)
    {
        this.lead = lead;
    }

    public Long getAssigneeType()
    {
        return assigneeType;
    }

    public void setAssigneeType(Long assigneeType)
    {
        this.assigneeType = assigneeType;
    }

    public String getPrettyAssigneeType(Long assigneeType)
    {
        return ProjectAssigneeTypes.getPrettyAssigneeType(assigneeType);
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key.toUpperCase();
    }

    public Long getNotificationScheme()
    {
        return notificationScheme;
    }

    public void setNotificationScheme(Long notificationScheme)
    {
        this.notificationScheme = notificationScheme;
    }

    public Long getPermissionScheme()
    {
        return permissionScheme;
    }

    public void setPermissionScheme(Long permissionScheme)
    {
        this.permissionScheme = permissionScheme;
    }

    public Long getIssueSecurityScheme()
    {
        return issueSecurityScheme;
    }

    public void setIssueSecurityScheme(Long issueSecurityScheme)
    {
        this.issueSecurityScheme = issueSecurityScheme;
    }

    public Map getAssigneeTypes()
    {
        return ProjectAssigneeTypes.getAssigneeTypes();
    }

    protected void mapErrorCollection(final ErrorCollection errorCollection)
    {
        final Map<String,String> validationErrors = errorCollection.getErrors();
        for (String key : validationErrors.keySet())
        {
            String originalErrorMessage = validationErrors.get(key);
            String newKey = ERROR_FIELD_MAPPINGS.get(key);

            if (newKey != null)
            {
                addError(newKey, originalErrorMessage);
            }
            else
            {
                addErrorMessage(originalErrorMessage);
            }
        }

        //and add any unkeyed error messages
        addErrorMessages(errorCollection.getErrorMessages());
    }

    public Long getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(final Long avatarId)
    {
        this.avatarId = avatarId;
    }
}
