package com.atlassian.jira.issue.link;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.RemoveException;

import java.util.Collection;

public class IssueLinkTypeDestroyerImpl implements IssueLinkTypeDestroyer
{
    private IssueLinkTypeManager issueLinkTypeManager;
    private IssueLinkManager issueLinkManager;

    public IssueLinkTypeDestroyerImpl(IssueLinkTypeManager issueLinkTypeManager, IssueLinkManager issueLinkManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkManager = issueLinkManager;
    }

    @Override
    public void removeIssueLinkType(Long issueLinkTypeId, IssueLinkType swapLinkType, User remoteUser) throws RemoveException
    {
        Collection<IssueLink> issueLinks = issueLinkManager.getIssueLinks(issueLinkTypeId);

        if (swapLinkType == null)
        {
            // We do not have a swap issue link type so just remove all the issue links
            for (final IssueLink issueLink : issueLinks)
            {
                // Remove the link of this type
                issueLinkManager.removeIssueLink(issueLink, remoteUser);
            }
        }
        else
        {
            // We were given another issue link type to move all the existing issue links to
            // So move the links before deleting the issue link type
            for (final IssueLink issueLink : issueLinks)
            {
                // Move all the link if the link type that we are about to delete to a different link type
                issueLinkManager.changeIssueLinkType(issueLink, swapLinkType, remoteUser);
            }

        }

        issueLinkTypeManager.removeIssueLinkType(issueLinkTypeId);
    }
}
