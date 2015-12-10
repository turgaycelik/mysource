package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Collection;

/**
 * Manager for {@link IssueType}s.
 *
 * @since v5.0
 */
@PublicApi
public interface IssueTypeManager
{
    /**
     * Create a new (non-subtask) issue type.
     * The new issue type will be added to the default issue type scheme.
     *
     * @param name name of the issue type. Cannot be null or blank. Must be unique across issue types and subtask issue types.
     * @param description description for the issue type.
     * @param iconUrl icon URL for the issue type. Cannot be null or blank.
     *
     * @return the new {@link IssueType}
     * @deprecated Use {@link #createIssueType(String, String, Long)} instead. Since v6.3.
     */
    @Deprecated
    IssueType createIssueType(String name, String description, String iconUrl);

    /**
     * Create a new (non-subtask) issue type.
     * The new issue type will be added to the default issue type scheme.
     *
     * @param name name of the issue type. Cannot be null or blank. Must be unique across issue types and subtask issue types.
     * @param description description for the issue type.
     * @param avatarId avatarId for the issue type. Cannot by null.
     *
     * @return the new {@link IssueType}
     * @since v6.3
     */
    IssueType createIssueType(String name, String description, Long avatarId);

    /**
     * Create a new subtask issue type.
     * The new issue type will be added to the default issue type scheme.
     *
     * @param name  name of the issue type. Cannot be null or blank. Must be unique across issue types and subtask issue types.
     * @param description description for the issue type.
     * @param iconUrl  icon URL for the issue type. Cannot be null or blank.
     *
     * @return the new {@link IssueType}
     */
    IssueType createSubTaskIssueType(String name, String description, String iconUrl);


    /**
     * Create a new subtask issue type.
     * The new issue type will be added to the default issue type scheme.
     *
     * @param name  name of the issue type. Cannot be null or blank. Must be unique across issue types and subtask issue types.
     * @param description description for the issue type.
     * @param avatarId avatarId for the issue type. Cannot by null.
     *
     * @return the new {@link IssueType}
     * @since v6.3
     */
    IssueType createSubTaskIssueType(String name, String description, Long avatarId);

    /**
     * Edits an existing issue type.
     *
     * @param issueType existing issue type
     * @param name new name. Cannot be null or blank. Must be unique across issue types and subtask issue types.
     * @param description new description.
     * @param iconUrl icon URL for the issue type. Cannot be null or blank.
     *
     * @deprecated Use {@link #updateIssueType(com.atlassian.jira.issue.issuetype.IssueType, String, String, Long)} instead. Since v6.3.
     */
    @Deprecated
    void editIssueType(IssueType issueType, String name, String description, String iconUrl);


    /**
     * Edits an existing issue type.
     *
     * @param issueType existing issue type
     * @param name new name. Cannot be null or blank. Must be unique across issue types and subtask issue types.
     * @param description new description.
     * @param avatarId avatar id the issue type. Cannot be null or blank.
     *
     * @since v6.3
     */
    void updateIssueType(IssueType issueType, String name, String description, Long avatarId);

    /**
     * Returns all issue types regular and subtask issue types.
     *
     * @return a collection of {@link IssueType}s
     */
    Collection<IssueType> getIssueTypes();

    /**
     * Removes an issue type.
     * All issues which use this issue type will be migrated to a different issue type which is specified in the second argument.
     *
     * @param id  id of the issue type to remove
     * @param newIssueTypeId the id of the new issue type for all issues which are of the issue type which we are about to remove. Can be null.
     */
    void removeIssueType(String id, String newIssueTypeId);

    /**
     * Returns the {@link IssueType} for the specified id.
     *
     * @param id issue type id.
     *
     * @return an {@link IssueType}
     */
    IssueType getIssueType(String id);

    /**
     * Returns a collection of suitable alternative {@link IssueType}s to which issues with the supplied issue type can be moved to.
     * The suitable alternative {@link IssueType}s will have to use the same workflow, the same field configuration and the same screen scheme.
     *
     * @param issueType
     * @return a collection of {@link IssueType}s
     */
    Collection<IssueType> getAvailableIssueTypes(IssueType issueType);
}
