package com.atlassian.jira.web;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchContext;

import java.util.List;

/**
 * This is an interface for the old FieldVisibilityBean. In the brave new world of OSGi and Plugins-2 we
 * need an interface so it can proxied to the plugins. This allows it to be injected into Spring components.
 * @since v4.0
 */
public interface FieldVisibilityManager
{
    String ALL_ISSUE_TYPES = "all";

    /**
     * Checks if the field is hidden across all the projects
     * that the user can see (has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser user
     * @param id         id of the FieldConfiguration
     * @return true if the user cannot see the field across all visible projects, false otherwise
     */
    boolean isFieldHidden(User remoteUser, String id);

    /**
     * Checks if the field is visible in at least one of the projects
     * that the user can see (has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser user
     * @param id id of the FieldConfiguration
     * @return true if the user can see the field in at least one visible project, false otherwise
     * @since 5.0
     */
    boolean isFieldVisible(User remoteUser, String id);

    /**
     * Checks if the field is hidden for the issue's current field layout scheme.
     *
     * @param fieldId field id
     * @param issue   issue
     * @return true if the field is hidden, false otherwise
     * @since v3.10
     */
    boolean isFieldHidden(String fieldId, Issue issue);

    /**
     * Checks if the field is visible for the issue's current field layout scheme.
     *
     * @param fieldId field id
     * @param issue issue
     * @return true if the field is visible, false otherwise
     * @since v5.0
     */
    boolean isFieldVisible(String fieldId, Issue issue);

    /**
     * Checks if the field is hidden in the project with id of projectId.
     *
     * @param projectId project id
     * @param fieldId field id
     * @param issueTypeId issue type id
     * @return true if the field is hidden, false otherwise
     */
    boolean isFieldHidden(Long projectId, String fieldId, Long issueTypeId);

    /**
     * Checks if the field is visible in the project with id of projectId.
     *
     * @param projectId project id
     * @param fieldId field id
     * @param issueTypeId issue type id
     * @return true if the field is visible, false otherwise
     * @since v5.0
     */
    boolean isFieldVisible(Long projectId, String fieldId, Long issueTypeId);

    /**
     * Checks if specified field is hidden in at least one scheme associated with the specified project and issue type.
     *
     * @param projectId   project id
     * @param fieldId     field id
     * @param issueTypeId issue type id
     * @return true if the field is hidden, false otherwise
     */
    boolean isFieldHidden(Long projectId, String fieldId, String issueTypeId);

    /**
     * Checks if specified field is visible in all schemes associated with the specified project and issue type.
     *
     * @param projectId project id
     * @param fieldId field id
     * @param issueTypeId issue type id
     * @return true if the field is visible, false otherwise
     * @since v5.0
     */
    boolean isFieldVisible(Long projectId, String fieldId, String issueTypeId);

    /**
     * Checks if the custom field is hidden in the project with id of projectId.
     *
     * @param projectId project id
     * @param customFieldId the data store id of the custom field
     * @param issueTypeId issue type id
     * @return true if the custom field is hidden, false otherwise
     */
    boolean isCustomFieldHidden(Long projectId, Long customFieldId, String issueTypeId);

    /**
     * Checks if the custom field is visible in the project with id of projectId.
     *
     * @param projectId project id
     * @param customFieldId the data store id of the custom field
     * @param issueTypeId issue type id
     * @return true if the custom field is visible, false otherwise
     * @since v5.0
     */
    boolean isCustomFieldVisible(Long projectId, Long customFieldId, String issueTypeId);

    /**
     * Returns TRUE if specified field is hidden under the following scenarios:
     * <p/>
     * 1: Project specified
     * - is field hidden in all schemes associated with the specified project.
     * <p/>
     * 2: Project and Issue Type(s) specified
     * - is field hidden in all schemes associated with the specified project for the issue types specified.
     * <p/>
     * Caching is abstracted to next level - all calls to getFieldLayout(...) are cached
     *
     * @param projectId  project id
     * @param fieldId    field id
     * @param issueTypes list of issue type ids (as String objects)
     * @return true if the given field is hidden, false otherwise
     */
    boolean isFieldHiddenInAllSchemes(Long projectId, String fieldId, List<String> issueTypes);

    boolean isFieldHiddenInAllSchemes(Long projectId, String fieldId);

    boolean isFieldHiddenInAllSchemes(String fieldId, SearchContext context, User user);
}
