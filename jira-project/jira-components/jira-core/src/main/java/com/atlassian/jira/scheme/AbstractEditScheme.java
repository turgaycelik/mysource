/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractEditScheme extends AbstractSchemeAwareAction
{
    private String name;
    private String description;

    protected void doValidation()
    {
        doNameValidation(name, "edit");

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        GenericValue updatedScheme = getScheme();
        updatedScheme.setString("name", name);
        updatedScheme.setString("description", description);

        getSchemeManager().updateScheme(updatedScheme);

        return returnCompleteWithInlineRedirect(getRedirectURL());
    }

    public String doDefault() throws Exception
    {
        setName(getScheme().getString("name"));
        setDescription(getScheme().getString("description"));

        return super.doDefault();
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
