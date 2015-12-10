/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractSchemeAware extends JiraWebActionSupport implements SchemeAware
{
    private Long schemeId;

    protected void doValidation()
    {
        // Check the scheme with specified id exists
        if (getSchemeId() != null)
        {
            try
            {
                FieldLayoutScheme fieldLayoutScheme = getFieldLayoutScheme();
                if (fieldLayoutScheme == null)
                {
                    addErrorMessage(getInvalidSchemeId());
                }
            }
            catch (DataAccessException e)
            {
                log.error(e, e);
                addErrorMessage(STORAGE_EXCEPTION);
            }
        }
        else
        {
            addErrorMessage(getInvalidSchemeId());
        }
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public void setSchemeId(Long schemeId)
    {
        this.schemeId = schemeId;
    }

    public GenericValue getScheme()
    {
        throw new UnsupportedOperationException("Use getFieldLayoutScheme() instead.");
    }

    public FieldLayoutScheme getFieldLayoutScheme()
    {
        return getFieldLayoutManager().getMutableFieldLayoutScheme(getSchemeId());
    }

    public FieldLayoutManager getFieldLayoutManager()
    {
        return ComponentAccessor.getFieldLayoutManager();
    }


}
