package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;

/**
 * Provides help with some business logic around the configuration of Field Configuration Schemes (FieldLayoutSchemes in
 * code) and the requirement for reindex.
 *
 * @since v4.0
 */
public interface FieldLayoutSchemeHelper
{
    /**
     * Determines whether or not a reindex message is required after changing the association of the specified issue type
     * in the given scheme from the old field layout (Field Configuration) to the new field layout.
     * 
     * @param user the user
     * @param fieldLayoutScheme the scheme in which the association change is occuring
     * @param oldFieldLayoutId the id of the Field Configuration that used to be associated
     * @param newFieldLayoutId the id of the new Field Configuration
     * @return true if the change will require a reindex message; false otherwise.
     */
    boolean doesChangingFieldLayoutAssociationRequireMessage(User user, FieldLayoutScheme fieldLayoutScheme, Long oldFieldLayoutId, Long newFieldLayoutId);

    /**
     * Determines whether or not a reindex message is required after modifying visibility of a field in the specified
     * field layout.
     *
     * @param user the user
     * @param fieldLayout the field layout being changed
     * @return true if the change will require a reindex message; false otherwise.
     */
    boolean doesChangingFieldLayoutRequireMessage(User user, EditableFieldLayout fieldLayout);

    /**
     * Determines whether or not a reindex message is required after changing the associated {@link FieldLayoutScheme}
     * (field configuration scheme) for a project.
     *
     * @param user the user
     * @param projectId the project which is being changed
     * @param oldFieldLayoutSchemeId the old scheme
     * @param newFieldLayoutSchemeId the new scheme
     * @return true if the change will require a reindex message; false otherwise.
     */
    boolean doesChangingFieldLayoutSchemeForProjectRequireMessage(final User user, final Long projectId, final Long oldFieldLayoutSchemeId, final Long newFieldLayoutSchemeId);
}
