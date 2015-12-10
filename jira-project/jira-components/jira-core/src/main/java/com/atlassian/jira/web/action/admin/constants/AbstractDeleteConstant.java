/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.constants;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDeleteConstant extends AbstractConstantAction
{
    public String id;
    protected String newId;
    protected boolean confirm;
    private GenericValue constant;
    private List matchingIssues;

    protected void doValidation()
    {
        try
        {
            if (getConstant() == null)
            {
                addErrorMessage(getText("admin.errors.no.constant.found", getNiceConstantName(), id));
            }

            if (newId == null || getConstant(newId) == null)
            {
                addError("newId", getText("admin.errors.specify.valid.constant.name", getNiceConstantName()));
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error occurred: " + e, e);
            addErrorMessage(getText("admin.errors.general.error.occurred", e));
        }
    }

    @RequiresXsrfCheck
    protected abstract String doExecute() throws Exception;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getNewId()
    {
        return newId;
    }

    public void setNewId(String newId)
    {
        this.newId = newId;
    }

    public GenericValue getConstant() throws GenericEntityException
    {
        if (constant == null)
        {
            constant = getConstant(id);
        }
        return constant;
    }

    public List getMatchingIssues() throws GenericEntityException
    {
        if (matchingIssues == null)
        {
            matchingIssues = getConstant().getRelated("ChildIssue");

            if (matchingIssues == null)
            {
                matchingIssues = Collections.EMPTY_LIST;
            }
        }
        return matchingIssues;
    }

    public Collection getNewConstants()
    {
        List newConstants = new ArrayList();
        for (GenericValue constant : getConstants())
        {
            if (!id.equals(constant.getString("id")))
            {
                newConstants.add(constant);
            }
        }
        return newConstants;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }
}
