package com.atlassian.jira.bc.issue.link;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.link.IssueLinkType;

import java.util.Collection;

/**
 * Service for creation, deletion, and general management of {@link IssueLinkType}s.
 * If Issue Linking is disabled, none of these will work.
 * @since v6.0
 */
@PublicApi
public interface IssueLinkTypeService
{
    /**
     * Create a new issue link type.
     * @param user the user who will create it
     * @param name the name of the link to create
     * @param outward the description to use for outbound links of this type
     * @param inward the description to use for inbound links of this type
     * @return a ServiceOutcome that contains a description of the failure or the created IssueLinkType
     */
    ServiceOutcome<IssueLinkType> createIssueLinkType(User user, String name, String outward, String inward);

    /**
     * Delete a given IssueLinkType, checking for permissions.
     * @param user the user who is performing the action
     * @param linkType the IssueLinkType to delete
     * @return a ServiceOutcome indicating success or failure
     */
    ServiceOutcome<IssueLinkType> deleteIssueLinkType(User user, IssueLinkType linkType);

    /**
     * Get a list of all issue link types in the system.
     * @param user the user who wants to know
     * @return a list of all issue link types
     */
    ServiceOutcome<Collection<IssueLinkType>> getIssueLinkTypes(User user);

    /**
     * Update an existing issue link type
     * @param user the user performing the modification
     * @param linkType the link you want to update
     * @param name the new name to use
     * @param outward the new outbound description to use
     * @param inward the new inbound description to use
     * @return the updated IssueLinkType
     */
    ServiceOutcome<IssueLinkType> updateIssueLinkType(User user, IssueLinkType linkType, String name, String outward, String inward);
}
