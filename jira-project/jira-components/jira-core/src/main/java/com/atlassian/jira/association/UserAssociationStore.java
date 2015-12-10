package com.atlassian.jira.association;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * This Store is used to work with relationships between Users and other objects.
 *
 * @since v4.3
 */
public interface UserAssociationStore
{
    /**
     * Tests if the given association exists.
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @param sinkNodeId The id of the sink node.
     * @return true if the given association exists.
     *
     * @deprecated Use {@link #associationExists(String, com.atlassian.jira.user.ApplicationUser, String, Long)} instead. Since v6.0.
     */
    public boolean associationExists(final String associationType, final User user, final String sinkNodeEntity, final Long sinkNodeId);

    /**
     * Tests if the given association exists.
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @param sinkNodeId The id of the sink node.
     * @return true if the given association exists.
     */
    public boolean associationExists(String associationType, ApplicationUser user, String sinkNodeEntity, Long sinkNodeId);

    /**
     * Finds and returns a list of usernames associated with a given sink.
     *
     * @param associationType the Association type
     * @param sink the sink node
     * @return a list of associated usernames (never null)
     */
    public List<String> getUsernamesFromSink(String associationType, GenericValue sink);

    /**
     * Finds and returns a list of userkeys associated with a given sink.
     *
     * @param associationType the Association type
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @param sinkNodeId The id of the sink node.
     * @return a list of associated usernames (never null)
     */
    Collection<String> getUserkeysFromSink(String associationType, String sinkNodeEntity, Long sinkNodeId);

    Collection<String> getUserkeysFromIssue(String associationType, Long issueId);

    /**
     * Returns all the sinks that are associated with the given User.
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @return all the sinks that are associated with the given User.
     */
    public List<GenericValue> getSinksFromUser(String associationType, ApplicationUser user, String sinkNodeEntity);

    /**
     * Finds and returns a list of Users associated with a given sink.
     *
     * @param associationType the Association type
     * @param sink the sink node
     * @return a list of associated Users (never null)
     */
    public List<ApplicationUser> getUsersFromSink(String associationType, GenericValue sink);

    /**
     * Creates an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param user the user to associate with the sink node.
     * @param sink the sink node
     */
    public void createAssociation(String associationType, ApplicationUser user, GenericValue sink);

    /**
     * Creates an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param user the user to associate with the sink node.
     * @param sink the sink node
     */
    public void createAssociation(String associationType, ApplicationUser user, Issue sink);

    /**
     * Creates an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param userkey the user name to associate with the sink node.
     * @param sinkNodeEntity the entity name of the sink node
     * @param sinkNodeId the id of the sink node entity
     */
    public void createAssociation(String associationType, String userkey, String sinkNodeEntity, Long sinkNodeId);

    /**
     * Removes an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param userkey the user to associate with the sink node.
     * @param sinkNodeEntity the entity name of the sink node
     * @param sinkNodeId the id of the sink node entity
     */
    public void removeAssociation(String associationType, String userkey, String sinkNodeEntity, Long sinkNodeId);

    /**
     * Removes an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param user the user to associate with the sink node.
     * @param sink the sink node
     */
    public void removeAssociation(String associationType, ApplicationUser user, Issue sink);

    /**
     * Removes all User Associations for this User of the given associationType
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     */
    public void removeUserAssociationsFromUser(final String associationType, final ApplicationUser user, final String sinkNodeEntity);

    /**
     * Removes all User Associations for this Sink
     *
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @param sinkNodeId the id of the sink node entity
     */
    public void removeUserAssociationsFromSink(String sinkNodeEntity, Long sinkNodeId);
}
