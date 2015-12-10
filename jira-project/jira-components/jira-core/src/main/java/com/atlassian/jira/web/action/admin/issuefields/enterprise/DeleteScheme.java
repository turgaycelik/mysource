/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

public class DeleteScheme extends AbstractSchemeAware
{
    private static final String INVALID_SCHEME_ID = "Please specify a valid scheme to delete.";

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldLayoutScheme fieldLayoutScheme = getFieldLayoutScheme();
        getFieldLayoutManager().deleteFieldLayoutScheme(fieldLayoutScheme);

        return getRedirect("ViewFieldLayoutSchemes.jspa");
    }

    public String getInvalidSchemeId()
    {
        return INVALID_SCHEME_ID;
    }
}
