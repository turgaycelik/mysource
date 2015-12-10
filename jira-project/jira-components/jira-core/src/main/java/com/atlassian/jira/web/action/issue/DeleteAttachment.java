package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

/**
 */
public class DeleteAttachment extends AbstractIssueSelectAction
{
    private static final String FROM_ISSUE = "issue";

    private final AttachmentService attachmentService;

    private Long deleteAttachmentId;
    private String from;
    private Attachment attachment;

    public DeleteAttachment(AttachmentService attachmentService)
    {
        this.attachmentService = attachmentService;
    }

    private boolean validateDelete()
    {
        try
        {
            attachmentService.canDeleteAttachment(getJiraServiceContext(), deleteAttachmentId);
        }
        catch (AttachmentNotFoundException e)
        {
            addErrorMessage(getText("delete.attachment.does.not.exist"));
        }
        return !isInError();
    }

    @Override
    public String doDefault()
    {
        if (!validateDelete())
        {
            return ERROR;
        }
        else
        {
            return INPUT;
        }
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        if (!validateDelete())
        {
            return ERROR;
        }
        else
        {
            attachmentService.delete(getJiraServiceContext(), deleteAttachmentId);
            if (isInError())
            {
                return ERROR;
            }
            else
            {
                return returnComplete(getNextUrl());
            }
        }
    }

    public String getNextUrl()
    {
        if (FROM_ISSUE.equals(getFrom()))
        {
            return "/browse/" + getIssueObject().getKey();
        }
        else
        {
            return "ManageAttachments.jspa?id=" + getIssueId();
        }
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getFrom()
    {
        return this.from;
    }

    public Long getDeleteAttachmentId()
    {
        return deleteAttachmentId;
    }

    public void setDeleteAttachmentId(Long deleteAttachmentId)
    {
        this.deleteAttachmentId = deleteAttachmentId;
    }

    public Long getIssueId()
    {
        return getIssueObject().getId();
    }

    public Attachment getAttachment()
    {
        if (attachment == null && !isInError())
        {
            try
            {
                attachment = attachmentService.getAttachment(getJiraServiceContext(), deleteAttachmentId);
            }
            catch (AttachmentNotFoundException e)
            {
                addErrorMessage(getText("delete.attachment.does.not.exist"));
            }
        }
        return attachment;
    }

    public boolean isInError()
    {
        return hasAnyErrors() || !isIssueValid();
    }
}