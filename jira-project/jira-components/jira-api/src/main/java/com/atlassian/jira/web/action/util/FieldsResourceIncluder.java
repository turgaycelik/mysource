package com.atlassian.jira.web.action.util;

/**
 * Used to include all the resources that may be necessary to view/edit a field. Called by pages that may show fields
 * directly or in dialogs.
 *
 * @since v4.2
 */
public interface FieldsResourceIncluder
{
    /**
     * Include the resources in the current page for the current user that may be necessary to view/edit fields.
     */
    void includeFieldResourcesForCurrentUser();
}
