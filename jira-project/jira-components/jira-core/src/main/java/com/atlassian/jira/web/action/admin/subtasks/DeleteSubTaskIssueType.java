/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.issuetypes.DeleteIssueType;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

@WebSudoRequired
public class DeleteSubTaskIssueType extends DeleteIssueType
{
    private final IssueTypeManager issueTypeManager;

    public DeleteSubTaskIssueType(IssueTypeManager issueTypeManager, EventPublisher eventPublisher)
    {
        super(issueTypeManager, eventPublisher);
        this.issueTypeManager = issueTypeManager;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        return super.doExecute();
    }

    protected String getNiceConstantName()
    {
        return getText("admin.issue.constant.subtask.issuetype.lowercase");
    }

    protected String getRedirectPage()
    {
        return "ManageSubTasks.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getSubTaskIssueTypes();
    }
}
