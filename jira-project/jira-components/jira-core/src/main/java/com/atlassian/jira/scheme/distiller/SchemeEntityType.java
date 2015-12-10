package com.atlassian.jira.scheme.distiller;

/**
 * This is a silly little wrapper that allows us to abstract the entityType information (ie for the Permssion and
 * Notification types).
 */
public class SchemeEntityType
{
    private Object entityTypeId;
    private String displayName;

    public SchemeEntityType(Object entityTypeId, String displayName)
    {
        this.entityTypeId = entityTypeId;
        this.displayName = displayName;
    }

    public Object getEntityTypeId()
    {
        return entityTypeId;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
