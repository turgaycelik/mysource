package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.RemoveException;

@PublicApi
public interface IssueLinkTypeDestroyer
{
    /**
     * Removes an issueLinkType from the datastore. If a swapLinkType is passed (not null) the
     * exsting issue links of the issueLinkType are changed to the swapLinkType. If swapLinkType
     * is null, all issue links are removed.
     *
     * @param issueLinkTypeId the id of the issue link type top delete
     * @param swapLinkType If null issue links are removed, if not null the issue links are changed to this
     * issue link type
     * @param remoteUser the remote user performing the delete operation (needed for change item creating)
     * @throws RemoveException if the problem occurs during change item creation or persistence
     */
    public void removeIssueLinkType(Long issueLinkTypeId, IssueLinkType swapLinkType, User remoteUser) throws RemoveException;
}
