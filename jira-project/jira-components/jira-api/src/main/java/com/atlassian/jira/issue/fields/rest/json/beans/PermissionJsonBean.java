package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.security.Permissions;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionJsonBean
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    public String id()
    {
        return id;
    }

    public PermissionJsonBean id(String id)
    {
        this.id = id;
        return this;
    }

    public String key()
    {
        return key;
    }

    public PermissionJsonBean key(String key)
    {
        this.key = key;
        return this;
    }

    public Permissions.Permission asPermission()
    {
        Permissions.Permission permission = getPermissionFromId();
        if (permission == null)
        {
            permission = getPermissionFromKey();
        }
        return permission;
    }

    @Override
    public String toString()
    {
        return "key: " + key + ", id: " + id;
    }

    private Permissions.Permission getPermissionFromKey()
    {
        if (key == null)
        {
            return null;
        }
        try
        {
            return Permissions.Permission.valueOf(key);
        }
        catch (IllegalArgumentException i)
        {
            return null;
        }
    }

    private Permissions.Permission getPermissionFromId() throws NumberFormatException
    {
        final Integer permissionId;
        try
        {
            permissionId = Integer.valueOf(id());
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        for (Permissions.Permission permission : Permissions.Permission.values())
        {
            if (permissionId.equals(permission.getId()))
            {
                return permission;
            }
        }
        return null;
    }

    public static PermissionJsonBean fullBean(Permissions.Permission permission)
    {
        return new PermissionJsonBean().id(String.valueOf(permission.getId())).key(permission.name());
    }
}
