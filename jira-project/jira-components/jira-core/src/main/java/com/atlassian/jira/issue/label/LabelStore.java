package com.atlassian.jira.issue.label;

import java.util.Set;

/**
 * Store
 *
 * @since v4.2
 */
public interface LabelStore
{
    /**
     * Returns all the labels for the given issue and custom field.  The custom field may also be null, in which case
     * the labels for the system field will be returned.
     *
     * @param issueId The issue id that the label is linked against
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @return A set of alphabetically ordered labels for the issue and custom field.
     */
    Set<Label> getLabels(final Long issueId, final Long customFieldId);

    /**
     * Sets the labels for a particular issue and field combo to the set specified as a parameter.  The set may be an
     * empty set in order to clear all labels for an issue.
     *
     * @param issueId The issue id that the label is linked against
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @param labels the new labels for this issue and custom field combo
     * @return a set of stored label objects in alphabetical order
     */
    Set<Label> setLabels(final Long issueId, final Long customFieldId, final Set<String> labels);

    /**
     * Adds a new label to the issue for the custom field specified. If the customFieldId is null, the label will be
     * added for the system field.  Adding an existing label again, will simply return the existing label.
     *
     * @param issueId The issue id that the label is linked against
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @param label The new label to add
     * @return The Label object that was added
     */
    Label addLabel(final Long issueId, final Long customFieldId, final String label);

    /**
     * Removes the label identified by id
     *
     * @param issueId The issue id that the label is linked against
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @param labelId The id of the label to delete
     */
    void removeLabel(final Long labelId, final Long issueId, final Long customFieldId);

    /**
     * Deletes all labels for a given custom field.  This is useful when deleting a custom field.
     *
     * @param customFieldId The custom field id for which to delete labels
     * @return A set of issueids affected by this operation
     */
    Set<Long> removeLabelsForCustomField(Long customFieldId);
}