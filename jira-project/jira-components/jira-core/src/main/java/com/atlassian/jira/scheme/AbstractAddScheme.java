/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractAddScheme extends AbstractSchemeAwareAction
{
    private String name;
    private String description;

    protected void doValidation()
    {
        doNameValidation(name, "add");

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Scheme newScheme = getSchemeManager().createSchemeObject(getName(), getDescription());

        return returnCompleteWithInlineRedirect(getRedirectURL() + newScheme.getId());
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
