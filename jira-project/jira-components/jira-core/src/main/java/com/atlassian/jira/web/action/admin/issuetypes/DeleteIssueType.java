/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractDeleteConstant;
import com.atlassian.jira.web.action.admin.issuetypes.events.IssueTypeDeletedEventThroughUI;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class DeleteIssueType extends AbstractDeleteConstant
{
    private final IssueTypeManager issueTypeManager;
    private final EventPublisher eventPublisher;

    public DeleteIssueType(IssueTypeManager issueTypeManager, EventPublisher eventPublisher)
    {
        this.issueTypeManager = issueTypeManager;
        this.eventPublisher = eventPublisher;
    }

    protected String getConstantEntityName()
    {
        return "IssueType";
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.issuetype.lowercase");
    }

    protected String getIssueConstantField()
    {
        return "type";
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getIssueType(id);
    }

    protected IssueType getIssueTypeObject()
    {
        return getConstantsManager().getIssueTypeObject(id);
    }

    protected String getRedirectPage()
    {
        return "ViewIssueTypes.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getIssueTypes();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshIssueTypes();
        ComponentAccessor.getFieldManager().refresh();
    }

    protected void doValidation()
    {
        try
        {
            if (getMatchingIssues().isEmpty())
            {
                if (getConstant() == null)
                {
                    addErrorMessage(getText("admin.errors.no.constant.found", getNiceConstantName(), id));
                }
            }
            else
            {
                // Validate issue type is associated with one workflow and field layout scheme and that
                // suitable alternative issue types exist
                if (issueTypeManager.getAvailableIssueTypes(getIssueTypeObject()).isEmpty())
                {
                    addErrorMessage(getText("admin.errors.issuetypes.no.alternative"));
                }
                super.doValidation();
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred: " + e, e);
            addErrorMessage(getText("admin.errors.error.occurred") + " " + e);
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        issueTypeManager.removeIssueType(id, newId);

        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            eventPublisher.publish(new IssueTypeDeletedEventThroughUI());
            return getRedirect(getRedirectPage());
        }
    }

    public Collection getAvailableIssueTypes() throws GenericEntityException, WorkflowException
    {
        return issueTypeManager.getAvailableIssueTypes(getIssueTypeObject());
    }

}
