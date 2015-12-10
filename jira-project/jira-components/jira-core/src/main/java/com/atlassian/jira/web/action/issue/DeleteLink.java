package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;
import org.ofbiz.core.entity.GenericEntityException;

public class DeleteLink extends AbstractIssueSelectAction
{
    private final IssueLinkService issueLinkService;

    private Long destId;
    private Long linkType;
    private Long sourceId;
    private IssueLink issueLink;
    private IssueLinkService.DeleteIssueLinkValidationResult validationResult;
    private boolean confirm;

    public DeleteLink(final IssueLinkService issueLinkService)
    {
        this.issueLinkService = issueLinkService;
    }

    protected void doValidation()
    {
        validationResult = issueLinkService.validateDelete(getLoggedInUser(), getIssueObject(), getLink());

        if (!validationResult.isValid())
        {
            addErrorCollection(validationResult.getErrorCollection());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            issueLinkService.delete(validationResult);

            String targetID = hasIssueLinks(getLoggedInUser(), getIssueObject()) ? "#linkingmodule" : "";
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

    public String getDirectionName() throws GenericEntityException
    {
        if (destId != null)
        { return getLink().getIssueLinkType().getOutward(); }
        else if (sourceId != null)
        { return getLink().getIssueLinkType().getInward(); }

        return null;
    }

    public String getTargetIssueKey()
    {
        Issue issue = getIssueManager().getIssueObject(destId != null ? destId : sourceId);
        if (issue != null)
        { return issue.getKey(); }

        return null;
    }

    /**
     * Returns true if there were no errors and issue has local links.
     *
     * @param user current user
     * @param issue current issue
     * @return true if issue has local links
     */
    private boolean hasIssueLinks(User user, Issue issue)
    {
        final IssueLinkService.IssueLinkResult result = issueLinkService.getIssueLinks(getLoggedInUser(), getIssueObject());
        return result.isValid() && !result.getLinkCollection().getAllIssues().isEmpty();
    }

    public Long getDestId()
    {
        return destId;
    }

    public void setDestId(Long destId)
    {
        this.destId = destId;
    }

    public Long getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(Long sourceId)
    {
        this.sourceId = sourceId;
    }

    public Long getLinkType()
    {
        return linkType;
    }

    public void setLinkType(Long linkType)
    {
        this.linkType = linkType;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public boolean isRemoteIssueLink()
    {
        return false;
    }

    private IssueLink getLink()
    {
        if (issueLink == null)
        {
            if (destId != null)
            {
                issueLink = issueLinkService.getIssueLink(getId(), destId, linkType);
            }
            else if (sourceId != null)
            {
                issueLink = issueLinkService.getIssueLink(sourceId, getId(), linkType);
            }
        }

        return issueLink;
    }
}
