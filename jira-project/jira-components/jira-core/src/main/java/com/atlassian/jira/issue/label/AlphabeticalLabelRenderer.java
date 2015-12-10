package com.atlassian.jira.issue.label;

import com.atlassian.crowd.embedded.api.User;

/**
 * Responsible for rendering an alphabetically grouped list of labels
 *
 * @since v4.3
 */
public interface AlphabeticalLabelRenderer
{
    /**
     * Provides the rendered HTML for all labels in a project & a particular field (System or custom field)
     *
     * @param remoteUser The user performing this request
     * @param projectId The project for which to render labels
     * @param fieldId The field for which to render labels
     * @param isOtherFieldsExist A flag to indicate whether the given field is the only labels field in the project
     * @return the rendered HTML for all labels in a project & a particular field (System or custom field)
     */
    String getHtml(final User remoteUser, final Long projectId, final String fieldId, final boolean isOtherFieldsExist);
}
