package com.atlassian.jira.issue.label;


import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

import java.util.Set;

/**
 * Manager responsible for adding/removing and getting labels for a particular issue and custom field combination.
 *
 * @since v4.2
 */
@PublicApi
public interface LabelManager
{
    /**
     * Returns all the labels for the given issue.
     *
     * @param issueId The issue id that the label is linked against
     * @return A set of alphabetically ordered labels for the issue.
     */
    Set<Label> getLabels(final Long issueId);

    /**
     * Sets the labels for a particular issue to the set specified as a parameter.  The set may be an empty set in order
     * to clear all labels for an issue.
     *
     * @param remoteUser The user setting labels
     * @param issueId The issue id that the label is linked against
     * @param labels the new labels for this issue
     * @param sendNotification true if an e-mail should be sent to users notifying them of the issue update
     * @param causeChangeNotification true if a change history should be created, false otherwise
     * @return a set of stored label objects in alphabetical order
     */
    Set<Label> setLabels(final User remoteUser, final Long issueId, final Set<String> labels, final boolean sendNotification, final boolean causeChangeNotification);

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
     * @param remoteUser The user setting labels
     * @param issueId The issue id that the label is linked against
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @param labels the new labels for this issue and custom field combo
     * @param sendNotification true if an e-mail should be sent to users notifying them of the issue update
     * @param causeChangeNotification true if a change history should be created, false otherwise
     * @return a set of stored label objects in alphabetical order
     */
    Set<Label> setLabels(final User remoteUser, final Long issueId, final Long customFieldId, final Set<String> labels, final boolean sendNotification, final boolean causeChangeNotification);

    /**
     * Adds a label to the issue provided.
     *
     * @param remoteUser The user setting labels
     * @param issueId The issue id that the label is linked against
     * @param label The new label to add to the issue
     * @param sendNotification true if an e-mail should be sent to users notifying them of the issue update
     * @return The Label domain object which was created
     */
    Label addLabel(final User remoteUser, final Long issueId, final String label, final boolean sendNotification);

    /**
     * Adds a label to the issue and customFieldId provided.
     *
     * @param remoteUser The user setting labels
     * @param issueId The issue id that the label is linked against
     * @param customFieldId The id of the custom field to add the label to
     * @param label The new label to add to the issue
     * @param sendNotification true if an e-mail should be sent to users notifying them of the issue update
     * @return The Label domain object which was created
     */
    Label addLabel(final User remoteUser, final Long issueId, final Long customFieldId, final String label, final boolean sendNotification);

    /**
     * This method deletes all label entries for the custom field provided.  This is useful when deleting a customfield
     *
     * @param customFieldId the custom field for which to delete labels
     * @return a set of issue ids affected
     */
    Set<Long> removeLabelsForCustomField(final Long customFieldId);

    /**
     * Returns a set of label suggestions sorted alphabetically for the labels system field. Suggestions will be
     * narrowed down to the ones starting with the token provided and, if the issue is non-null, any labels the issue
     * already has will be removed. If the token provided is null or empty, a set of labels sorted by popularity of the
     * label will be returned.
     *
     * @param user The user retrieving suggestions for the labels system field
     * @param issueId The issue for which suggestions should be generated or {@code null}
     * @param token The search token entered by the user
     * @return A sorted set of labels in alphabetical order
     */
    Set<String> getSuggestedLabels(final User user, final Long issueId, final String token);

    /**
     * Returns a set of label suggestions sorted alphabetically for the label custom field provided. Suggestions will be
     * narrowed down to the ones starting with the token provided and, if the issue is non-null, any labels the issue
     * already has will be removed. If the token provided is null or empty, a set of labels sorted by popularity of the
     * label will be returned.
     *
     * @param user The user retrieving suggestions for the labels custom field
     * @param issueId The issue for which suggestions should be generated or {@code null}
     * @param customFieldId The label custom field to generate suggestions for
     * @param token The search token entered by the user
     * @return A sorted set of labels in alphabetical order
     */
    Set<String> getSuggestedLabels(final User user, final Long issueId, final Long customFieldId, final String token);
}
