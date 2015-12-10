/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.priorities;

import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class DeletePriority extends AbstractDeleteConstant
{
    private final PriorityManager priorityManager;

    public DeletePriority(PriorityManager priorityManager)
    {
        this.priorityManager = priorityManager;
    }

    protected String getConstantEntityName()
    {
        return "Priority";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.priority.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "priority";
    }

    protected GenericValue getConstant(String id)
    {
        Priority priorityObject = getConstantsManager().getPriorityObject(id);
        return (priorityObject != null) ? priorityObject.getGenericValue() : null;
    }

    protected String getRedirectPage()
    {
        return "ViewPriorities.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getPriorities();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshPriorities();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            priorityManager.removePriority(id, newId);
        }
        if (getHasErrorMessages())
            return ERROR;
        else
            return getRedirect(getRedirectPage());
    }
}
