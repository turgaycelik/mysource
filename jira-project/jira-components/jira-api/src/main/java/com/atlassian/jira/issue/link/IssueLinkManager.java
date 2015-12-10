package com.atlassian.jira.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * The implementations of this class are used to manage {@link IssueLinkType issue link types} and {@link IssueLink issue links}.
 */
@PublicApi
public interface IssueLinkManager
{
    /**
     * Constructs a new issuelink from the sourceIssueId to the destinationId and persists it.  This operation will
     * cause a re-index of the associated issues.
     *
     * @param sourceIssueId      The source issue.
     * @param destinationIssueId The destination issue.
     * @param issueLinkTypeId    The type of issuelink
     * @param sequence           In which order the link will appear in the UI
     * @param remoteUser         Needed for creation of change items.
     * @throws CreateException   If there is an error when creating the "Change Item" for this operation. Note that the Link itself has most likely been created.
     */
    public void createIssueLink(Long sourceIssueId, Long destinationIssueId, Long issueLinkTypeId, Long sequence, User remoteUser) throws CreateException;

    /**
     * Removes a single issue link
     * We do not check for permission here. It should be done before this method is called. For example, in the action.
     *
     * @param issueLink  the issue link to remove
     * @param remoteUser needed for creation of change items
     * @throws RemoveException          if error occurs during creation of change items
     * @throws IllegalArgumentException if the supplied issueLink is null.
     */
    public void removeIssueLink(IssueLink issueLink, User remoteUser) throws RemoveException;

    /**
     * Removes ALL incoming and outgoing issuelinks from the issue supplied.
     *
     * @param issue
     * @param remoteUser
     * @return The total number of issuelinks deleted.
     * @throws RemoveException
     *
     * @deprecated Use {@link #removeIssueLinks(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    public int removeIssueLinks(GenericValue issue, User remoteUser) throws RemoveException;

    /**
     * Removes ALL incoming and outgoing issuelinks from the issue supplied.
     *
     * @param issue
     * @param remoteUser
     * @return The total number of issuelinks deleted.
     * @throws RemoveException
     */
    public int removeIssueLinks(Issue issue, User remoteUser) throws RemoveException;

    /**
     * Removes ALL incoming and outgoing issuelinks from the issue supplied without creating ChangeItems for the Change History.
     * <p>
     * You would normally want to use the other method which creates the ChangeItems - this method is only intended for
     * use during Issue Delete.
     *
     * @param issue
     * @return The total number of issuelinks deleted.
     * @throws RemoveException
     * @see #removeIssueLinks(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
     */
    public int removeIssueLinksNoChangeItems(Issue issue) throws RemoveException;

    /**
     * Constructs a {@link LinkCollection} for a given issue.
     *
     * @param issue
     * @param remoteUser
     * @return A {@link LinkCollection} with all the issues ingoing and outgoing issue links
     * @deprecated use {@link #getLinkCollection(com.atlassian.jira.issue.Issue, User)} instead
     */
    public LinkCollection getLinkCollection(GenericValue issue, User remoteUser);

    /**
     * @see #getLinkCollection(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
     * @param issue the issue
     * @param remoteUser the user performing the search
     * @param excludeSystemLinks whether or not to exclude system links
     * @return A {@link LinkCollection} with all the issues ingoing and outgoing issue links
     * @since v4.4.2
     */
    public LinkCollection getLinkCollection(Issue issue, User remoteUser, boolean excludeSystemLinks);

    /**
     * Constructs a {@link LinkCollection} for a given issue.
     *
     * @param issue the issue
     * @param remoteUser the user performing the search
     * @return A {@link LinkCollection} with all the issues ingoing and outgoing issue links
     * @since v4.0
     */
    public LinkCollection getLinkCollection(Issue issue, User remoteUser);

    /**
     * Constructs a {@link LinkCollection} for a given issue, ignoring security.
     *
     * @param issue the issue
     * @return A {@link LinkCollection} with all the issues ingoing and outgoing issue links
     * @since v4.0
     */
    public LinkCollection getLinkCollectionOverrideSecurity(Issue issue);

    /**
     * Returns a collection of all {@link IssueLink}s for a particular issue link type
     *
     * @param issueLinkTypeId ID of the Issue Link Type
     * @return A collection of {@link IssueLink}s
     */
    public Collection<IssueLink> getIssueLinks(Long issueLinkTypeId);

    /**
     * Returns the {@link IssueLink} with the specified id.
     *
     * @param issueLinkId the issue link id.
     * @return the {@link IssueLink}. Can be NULL, if no issue link found.
     */
    public IssueLink getIssueLink(Long issueLinkId);

    /**
     * Get links from an issue.
     *
     * @param sourceIssueId Eg. from {@link com.atlassian.jira.issue.Issue#getId()}
     * @return List of {@link IssueLink}s. This list will be immutable.
     */
    public List<IssueLink> getOutwardLinks(Long sourceIssueId);

    /**
     * Get links to an issue.
     *
     * @param destinationIssueId Eg. from {@link com.atlassian.jira.issue.Issue#getId()}
     * @return List of {@link IssueLink}s. This list will be immutable.
     */
    public List<IssueLink> getInwardLinks(Long destinationIssueId);

    /**
     * Moves an issue link to a different position in the list of issuelink.
     * <b>NOTE:</b> This is currently only used when re-ordering sub-tasks.
     *
     * @param issueLinks      The list of issueLinks
     * @param currentSequence The postion of the issuelink about to be moved
     * @param sequence        The target position of the issuelink
     * @throws IllegalArgumentException If currentSequence or sequence are null
     */
    public void moveIssueLink(List<IssueLink> issueLinks, Long currentSequence, Long sequence);

    /**
     * Sets the sequence number for each issueLink in the List of issueLinks provided
     * according to its position in the List.
     *
     * @param issueLinks A list of issue links to be recalculated
     */
    public void resetSequences(List<IssueLink> issueLinks);

    /**
     * Retrieves an issue link given a source, destination and a link type.
     *
     * @param sourceId
     * @param destinationId
     * @param issueLinkTypeId
     * @return an {@link IssueLink}
     */
    public IssueLink getIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId);

    /**
     * Changes the type of an issue link.
     * <b>NOTE:</b> It is not possible to convert a system link type to a non-system link type and vice versa.
     *
     * @param issueLink
     * @param swapLinkType
     * @param remoteUser
     * @throws RemoveException
     */
    public void changeIssueLinkType(IssueLink issueLink, IssueLinkType swapLinkType, User remoteUser) throws RemoveException;

    /**
     * Returns whether Issue Linking is currently enabled in JIRA. Issue Linking can be enabled or
     * disabled in the Admin section of JIRA.
     */
    public boolean isLinkingEnabled();

    /**
     * Clears the Issue Link cache used by the Issue Link Manager.
     */
    public void clearCache();
}
