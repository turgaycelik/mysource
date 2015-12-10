/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.security.JiraAuthenticationContext;

public class AbstractField implements Field
{
    private String id;
    private String name;
    protected final JiraAuthenticationContext authenticationContext;

    public AbstractField(String id, String name, JiraAuthenticationContext authenticationContext)
    {
        this.id = id;
        this.name = name;
        this.authenticationContext = authenticationContext;
    }

    public String getId()
    {
        return id;
    }

    public String getNameKey()
    {
        return name;
    }

    public String getName()
    {
        return authenticationContext.getI18nHelper().getText(getNameKey());
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Field))
            return false;

        final Field field = (Field) o;

        if (id != null ? !id.equals(field.getId()) : field.getId() != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }

    protected JiraAuthenticationContext getAuthenticationContext()
    {
        return authenticationContext;
    }

    public int compareTo(Object o)
    {
        // NOTE: If this is being chnaged, chances are the compareTo method of the CustomFieldImpl object also needs to chnage.
        if (o == null)
            return 1;
        else if (o instanceof Field)
        {
            Field field = (Field) o;
            if (getName() == null)
            {
                if (field.getName() == null)
                    return 0;
                else
                    return -1;
            }
            else
            {
                if (field.getName() == null)
                    return 1;
                else
                    return getName().compareTo(field.getName());
            }
        }
        else
        {
            throw new IllegalArgumentException("Can only compare Field objects.");
        }
    }
}
