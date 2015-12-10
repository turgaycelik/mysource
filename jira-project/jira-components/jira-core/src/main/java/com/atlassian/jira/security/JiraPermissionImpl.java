/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.jira.security.type.GroupDropdown;
import org.ofbiz.core.entity.GenericValue;

public class JiraPermissionImpl implements JiraPermission
{
    private final int type;

    //permissions are now done on a scheme rather than on a project
    private final Long scheme;
    private String permType;
    private String group;

    public JiraPermissionImpl(int type)
    {
        this.type = type;
        this.scheme = null;
        //default to a group permission if only the type is passed in
        this.permType = GroupDropdown.DESC;
    }

    public JiraPermissionImpl(int type, String group, String permType)
    {
        this.type = type;
        this.scheme = null;
        this.group = group;
        this.permType = permType;
    }

    public JiraPermissionImpl(GenericValue permission)
    {
        this.type = permission.getLong("permission").intValue();
        this.scheme = permission.getLong("scheme");
        this.group = permission.getString("parameter");
        this.permType = permission.getString("type");
    }

    public JiraPermissionImpl(int type, Long scheme, String group, String permType)
    {
        this.type = type;
        this.scheme = (scheme != null && scheme.longValue() >= 0) ? scheme : null;
        this.group = group;
        this.permType = permType;
    }

    public JiraPermissionImpl(int type, GenericValue scheme, String group, String permType)
    {
        this.type = type;
        this.scheme = (scheme == null ? null : scheme.getLong("id"));
        this.group = group;
        this.permType = permType;
    }

    public int getType()
    {
        return type;
    }

    public Long getScheme()
    {
        return scheme;
    }

    public String getGroup()
    {
        return group;
    }

    public String getPermType()
    {
        return permType;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof JiraPermissionImpl))
        {
            return false;
        }

        final JiraPermissionImpl jiraPermission = (JiraPermissionImpl) o;

        if (type != jiraPermission.type)
        {
            return false;
        }
        if (group != null ? !group.equals(jiraPermission.group) : jiraPermission.group != null)
        {
            return false;
        }
        if (permType != null ? !permType.equals(jiraPermission.permType) : jiraPermission.permType != null)
        {
            return false;
        }
        if (scheme != null ? !scheme.equals(jiraPermission.scheme) : jiraPermission.scheme != null)
        {
            return false;
        }
        return true;
    }

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = type;
        result = 29 * result + (scheme != null ? scheme.hashCode() : 0);
        result = 29 * result + (permType != null ? permType.hashCode() : 0);
        result = 29 * result + (group != null ? group.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return "Permission: [type=" + type + "][scheme=" + scheme + "][group=" + group + "][permType=" + permType + "]";
    }
}
