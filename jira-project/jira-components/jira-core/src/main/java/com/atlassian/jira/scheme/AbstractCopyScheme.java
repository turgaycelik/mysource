/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

public abstract class AbstractCopyScheme extends AbstractSchemeAwareAction
{
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            getSchemeManager().copyScheme(getSchemeObject());
        }
        catch (DataAccessException e)
        {
            addErrorMessage(e.getMessage());
        }

        return getRedirect(getRedirectURL());
    }
}
