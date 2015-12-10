package com.atlassian.jira.issue.label;

import com.atlassian.crowd.embedded.api.User;

/**
 * Utility to generate Label JQL strings.
 *
 * @since v4.2
 */
public interface LabelUtil
{
    /**
     * Given a label this utility returns a JQL string to search for this label in the system labels field
     *
     * @param user The user running the search
     * @param label The label string to search for
     * @return A jql string representing a valid query to search for this label
     */
    public String getLabelJql(final User user, final String label);

    /**
     * Given a label and custom field Id, this utility returns a JQL string to search for this label in the custom field
     * provided
     *
     * @param user The user running the search
     * @param customFieldId The CustomField Id of the labels custom field
     * @param label The label string to search for
     * @return A jql string representing a valid query to search for this label
     */
    public String getLabelJql(final User user, final Long customFieldId, final String label);

    /**
     * Given a label and project id, this utility returns a JQL string to search for this label in the label system
     * field, limited by the project provided
     *
     * @param user The user running the search
     * @param projectId The project to limit the search
     * @param label The label string to search for
     * @return A jql string representing a valid query to search for this label
     */
    public String getLabelJqlForProject(final User user, final Long projectId, final String label);

    /**
     * Given a label, project id and custom field id, this utility returns a JQL string to search for this label in the
     * label custom field, limited by the project provided
     *
     * @param user The user running the search
     * @param projectId The project to limit the search
     * @param customFieldId The CustomField Id of the labels custom field
     * @param label The label string to search for
     * @return A jql string representing a valid query to search for this label
     */
    public String getLabelJqlForProject(final User user, final Long projectId, final Long customFieldId, final String label);
}
