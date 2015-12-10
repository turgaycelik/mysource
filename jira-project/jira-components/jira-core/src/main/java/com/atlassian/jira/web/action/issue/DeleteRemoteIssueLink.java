package com.atlassian.jira.web.action.issue;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkUIDeleteEvent;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;

public class DeleteRemoteIssueLink extends AbstractIssueSelectAction
{
    private final RemoteIssueLinkService remoteIssueLinkService;
    private final EventPublisher eventPublisher;
    private RemoteIssueLink remoteIssueLink;
    private RemoteIssueLinkService.DeleteValidationResult deleteValidationResult;
    private boolean confirm;
    private Long remoteIssueLinkId;

    public DeleteRemoteIssueLink(RemoteIssueLinkService remoteIssueLinkService, EventPublisher eventPublisher)
    {
        this.remoteIssueLinkService = remoteIssueLinkService;
        this.eventPublisher = eventPublisher;
    }

    protected void doValidation()
    {
        try
        {
            // To delete issue links - you need to have the "edit" issue link permission.
            if (!hasIssuePermission(Permissions.LINK_ISSUE, getIssueObject()))
            {
                addErrorMessage(getText("admin.errors.issues.no.permission.to.delete.links"));
                return;
            }

            deleteValidationResult = remoteIssueLinkService.validateDelete(getLoggedInUser(), remoteIssueLinkId);
            if (!deleteValidationResult.isValid())
            {
                addErrorMessages(deleteValidationResult.getErrorCollection().getErrorMessages());
                for (String error : deleteValidationResult.getErrorCollection().getErrors().values())
                {
                    addErrorMessage(error);
                }
            }
            else
            {
                RemoteIssueLinkService.RemoteIssueLinkResult remoteIssueLinkResult = remoteIssueLinkService.getRemoteIssueLink(getLoggedInUser(),remoteIssueLinkId);
                if (remoteIssueLinkResult.getErrorCollection().hasAnyErrors())
                {
                    addErrorMessages(remoteIssueLinkResult.getErrorCollection().getErrorMessages());
                }
                else
                {
                    remoteIssueLink = remoteIssueLinkResult.getRemoteIssueLink();
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
            addErrorMessage(getText("admin.errors.issues.exception.occured.validating", e));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            remoteIssueLinkService.delete(getLoggedInUser(), deleteValidationResult);

            eventPublisher.publish(new RemoteIssueLinkUIDeleteEvent(deleteValidationResult.getRemoteIssueLinkId()));

            boolean hasRemoteIssueLinks = remoteIssueLinkService.getRemoteIssueLinksForIssue(getLoggedInUser(), getIssueObject()).getRemoteIssueLinks().iterator().hasNext();
            String targetID = hasRemoteIssueLinks ? "#linkingmodule" : "";

            final String redirectUrl = "/browse/" + getIssueObject().getKey() + targetID;
            if (isInlineDialogMode())
            {
                return returnCompleteWithInlineRedirect(redirectUrl);
            }

            return returnComplete(redirectUrl);

        }
        else
        {
            // No confirmation supplied - ask for one
            return INPUT;
        }
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public Long getRemoteIssueLinkId()
    {
        return remoteIssueLinkId;
    }

    public void setRemoteIssueLinkId(Long remoteIssueLinkId)
    {
        this.remoteIssueLinkId = remoteIssueLinkId;
    }

    public String getRelationship()
    {
        return (remoteIssueLink == null ? null : remoteIssueLink.getRelationship());
    }

    public String getLinkTitle()
    {
        return (remoteIssueLink == null ? null : remoteIssueLink.getTitle());
    }

    public boolean isRemoteIssueLink()
    {
        return true;
    }
}
