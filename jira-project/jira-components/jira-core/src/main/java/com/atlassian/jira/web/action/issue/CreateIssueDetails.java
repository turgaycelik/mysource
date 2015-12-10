/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.atlassian.security.random.SecureTokenGenerator;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateIssueDetails extends CreateIssue
{
    private final IssueService issueService;
    protected IssueService.CreateValidationResult validationResult;
    private final SecureTokenGenerator secureTokenGenerator = DefaultSecureTokenGenerator.getInstance();
    private final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator;

    public CreateIssueDetails(final IssueFactory issueFactory, final IssueCreationHelperBean issueCreationHelperBean,
            final IssueService issueService, final TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator)
    {
        super(issueFactory, issueCreationHelperBean);
        this.issueService = issueService;
        this.temporaryAttachmentsMonitorLocator = temporaryAttachmentsMonitorLocator;
    }

    /**
     * JRA-4791 - To allow direct links to populate the create issue details page without submiting the form
     */
    public String doInit()
    {
        if (isAbleToCreateIssueInSelectedProject())
        {
            doValidation();
            if (invalidInput())
                return ERROR;
        }
        return INPUT;
    }

    protected void doValidation()
    {
        validationResult = issueService.validateCreate(getLoggedInUser(), new IssueInputParametersImpl(ActionContext.getParameters()));
        setIssueObject(validationResult.getIssue());
        // We want to be able to repopulate the fields with their input values
        this.fieldValuesHolder = validationResult.getFieldValuesHolder();

        if (!validationResult.isValid())
        {
            // This page has no fields to show field error messages against, make them error messages instead
            final ErrorCollection errorCollection = validationResult.getErrorCollection();
            transferErrorToMessages(errorCollection);
            addErrorCollection(errorCollection);
        }
    }

    protected void transferErrorToMessages(ErrorCollection errorCollection)
    {
        final Iterator<Map.Entry<String, String>> iterator = errorCollection.getErrors().entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<String, String> entry = iterator.next();
            errorCollection.addErrorMessage(entry.getKey() + ": " + entry.getValue());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            createIssue();
            if(hasAnyErrors())
            {
                return ERROR;
            }
            return doPostCreationTasks();
        }
        catch (Exception e)
        {
            log.error(e, e);
            addErrorMessage((e.getMessage() != null ? e.getMessage() : ExceptionUtils.getFullStackTrace(e)));
            return ERROR;
        }
    }

    protected void createIssue() throws Exception
    {
        if (validationResult == null)
        {
            // There are alot of actions that use this as a base action and not all of them have been converted to
            // use the issue service, we need this for those that setup the issue themselves.
            validationResult = new IssueService.CreateValidationResult(getIssueObject(), new SimpleErrorCollection(), getFieldValuesHolder());
        }

        IssueService.IssueResult issueResult = issueService.create(getLoggedInUser(), validationResult, getAuxiliarySubmitButtonValue());
        if (issueResult.isValid())
        {
            setIssueObject(issueResult.getIssue());

            final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = temporaryAttachmentsMonitorLocator.get(false);
            if(temporaryAttachmentsMonitor != null)
            {
                temporaryAttachmentsMonitor.clearEntriesForFormToken(getFormToken());
            }
        }
        else
        {
            addErrorCollection(issueResult.getErrorCollection());
        }
    }

    protected String doPostCreationTasks() throws Exception
    {
        String issueKey = JiraUrlCodec.encode(getKey());
        if (isIssueValid())
        {
            return getRedirect("/browse/" + issueKey);
        }
        else
        {
            // clear the errors since we are just going to redirect
            errorMessages.clear();
            return getRedirect("CantBrowseCreatedIssue.jspa?issueKey=" + issueKey);
        }
    }

    public GenericValue getIssue()
    {
        GenericValue issue = super.getIssue();
        if (issue == null)
            throw new IllegalStateException("No issue has been created yet so it can not be retrieved");
        else
            return issue;
    }

    /**
     * The jira-workflow.xml Create action (id 1) can contain meta attributes indicating that auxiliary (alternative) 'Create'
     * buttons must be shown, to support alternative ways of submitting an issue (eg. shortcutting a step). For example:
     * <p/>
     * <meta name="jira.button.submit">createissue.shortcut.submit</meta>
     * <p/>
     * This method checks whether a Request param matching any meta name was found
     * (eg. 'foo.jsp?jira.button.submit=jira.button.submit', and if so, returns the name ('jira.button.submit').  This can
     * then be passed back to the workflow.
     * Returns 'null' if the regular submit button was pressed
     */
    protected String getAuxiliarySubmitButtonValue() throws WorkflowException
    {
        JiraWorkflow workflow = ComponentAccessor.getWorkflowManager().getWorkflow(getPid(), getIssuetype());
        if (workflow == null) throw new WorkflowException("No workflow for pid " + getPid() + ", issuetype " + getIssuetype());
        final WorkflowDescriptor descriptor = workflow.getDescriptor();
        final List initialActions = descriptor.getInitialActions();
        if (initialActions.size() == 0)
        {
            throw new WorkflowException("There are no initial actions for workflow " + workflow + ", descriptor " + descriptor + " associated with pid " + getPid() + " and issue type " + getIssuetype());
        }
        Map buttons = ((ActionDescriptor) initialActions.get(0)).getMetaAttributes();
        final Iterator it = buttons.keySet().iterator();
        while (it.hasNext())
        {
            String key = (String) it.next();
            if (request.getParameter(key) != null)
            {
                return key;
            }
        }
        return null;
    }

    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }

}
