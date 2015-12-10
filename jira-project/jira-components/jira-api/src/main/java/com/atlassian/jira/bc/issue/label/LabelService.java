package com.atlassian.jira.bc.issue.label;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Set;

/**
 * The label service is responsible for setting and getting labels for issue and custom field combinations.  It can also
 * be used to add and remove individual labels from an isssue.
 *
 * @since v4.2
 */
@PublicApi
public interface LabelService
{
    /**
     * Returns all the labels for the given issue.
     *
     * @param user The user performing the operation
     * @param issueId The issue id that the label is linked against
     * @return A set of alphabetically ordered labels for the issue.
     */
    LabelsResult getLabels(final User user, final Long issueId);

    /**
     * Validates that the user provided can set the labels provided for a particular issue.  Validation will ensure that
     * the user has the EDIT_ISSUE permission for the issue in question.  The labels will also be validated to ensure
     * that they don't contain spaces and that they don't exceed the max length of 255
     * characters each.
     *
     * @param user The user performing the operation
     * @param issueId The issue id of the issue that labels will be set on
     * @param labels The actual labels as strings to set on the issue
     * @return A validation result, that can be used to set the labels or to display errors.
     */
    SetLabelValidationResult validateSetLabels(final User user, final Long issueId, final Set<String> labels);

    /**
     * Validates that the user provided can set the labels provided for a particular issue.  Validation will ensure that
     * the user has the EDIT_ISSUE permission for the issue in question.  The labels will also be validated to ensure
     * that they don't contain spaces and that they don't exceed the max length of 255
     * characters each.  Validation will also ensure that the custom field with the id provided exists.
     *
     * @param user The user performing the operation
     * @param issueId The issue id of the issue that labels will be set on
     * @param customFieldId The custom field id against which to set the labels
     * @param labels The actual labels as strings to set on the issue
     * @return A validation result, that can be used to set the labels or to display errors.
     */
    SetLabelValidationResult validateSetLabels(final User user, final Long issueId, final Long customFieldId, final Set<String> labels);

    /**
     * Returns all the labels for the given issue and custom field.  The custom field may also be null, in which case
     * the labels for the system field will be returned.
     *
     * @param user The user performing the operation
     * @param issueId The issue id that the label is linked against
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @return A set of alphabetically ordered labels for the issue and custom field.
     */
    LabelsResult getLabels(final User user, final Long issueId, final Long customFieldId);

    /**
     * Sets the labels for a particular issue to the set specified as a parameter.  The set may be an empty set in order
     * to clear all labels for an issue.
     *
     * @param user The user performing the operation
     * @param result The validation result obtained by calling {@link #validateSetLabels(User,
     * Long, java.util.Set)}
     * @param sendNotification true if a notification e-mail should be sent, false otherwise
     * @param causeChangeNotification true if a change history should be created, false otherwise
     * @return a set of stored label objects in alphabetical order
     */
    LabelsResult setLabels(final User user, final SetLabelValidationResult result, final boolean sendNotification, final boolean causeChangeNotification);

    /**
     * Validates that the user provided can add the label provided for a particular issue.  Validation will ensure that
     * the user has the EDIT_ISSUE permission for the issue in question.  The label will also be validated to ensure
     * that it doesn't contain spaces and that it doesn't exceed the max length of 255
     * characters.
     *
     * @param user The user performing the operation
     * @param issueId The issue id of the issue that labels will be set on
     * @param label The actual labels as strings to set on the issue
     * @return A validation result, that can be used to set the labels or to display errors.
     */
    AddLabelValidationResult validateAddLabel(final User user, final Long issueId, final String label);

    /**
     * Validates that the user provided can add the label provided for a particular issue.  Validation will ensure that
     * the user has the EDIT_ISSUE permission for the issue in question.  The label will also be validated to ensure
     * that it doesn't contain spaces and that it doesn't exceed the max length of 255
     * characters. Validation will also ensure that the custom field with the id provided exists.
     *
     * @param user The user performing the operation
     * @param issueId The issue id of the issue that labels will be set on
     * @param customFieldId Custom field id for the labels CF or null if it's the system field.
     * @param label The actual labels as strings to set on the issue
     * @return A validation result, that can be used to set the labels or to display errors.
     */
    AddLabelValidationResult validateAddLabel(final User user, final Long issueId, final Long customFieldId, final String label);

    /**
     * Adds the label to the issue specified by the validation result.
     *
     * @param user The user performing the operation
     * @param result The validation result obtained via {@link #validateAddLabel(User, Long, String)}
     * @param sendNotification true if a notification e-mail should be sent, false otherwise
     * @return A result containing the new label.
     */
    LabelsResult addLabel(final User user, final AddLabelValidationResult result, final boolean sendNotification);

    /**
     * Given a token to search for, this method returns a number of suggestions for the label.  The token may also be
     * null or empty in which case a list of suggestions will be returned sorted by most popular labels for the labels
     * system field.  If a token was provided, then a list of labels sorted alphabetically starting with the token will
     * be returned.  If provided, any labels that the issue already has will be removed from the list of suggestions.
     * The token needs to be at least 2 characters to generate suggestions starting with that token, otherwise an empty
     * collection is returned.
     *
     * @param user The user trying to get label suggestions
     * @param issueId The issue for which suggestions are being fetched or {@code null}
     * @param token The prefix for the labels to be suggested. May be null for popular label suggestions
     * @return suggestion result containing a set of suggestions either sorted alphabetically or by popularity depending
     *         on the token
     */
    LabelSuggestionResult getSuggestedLabels(final User user, final Long issueId, String token);

    /**
     * Given a token to search for, this method returns a number of suggestions for the label.  The token may also be
     * null or empty in which case a list of suggestions will be returned sorted by most popular labels for the labels
     * custom field provided.  If a token was provided, then a list of labels sorted alphabetically starting with the
     * token will be returned.  If provided, any labels that the issue already has will be removed from the list of
     * suggestions. The token needs to be at least 2 characters long to generate suggestions starting with that token,
     * otherwise an empty collection is returned.
     *
     * @param user The user trying to get label suggestions
     * @param issueId The issue for which suggestions are being fetched or {@code null}
     * @param customFieldId The labels custom field for which to provide suggestions
     * @param token The prefix for the labels to be suggested. May be null for popular label suggestions
     * @return suggestion result containing a set of suggestions either sorted alphabetically or by popularity depending
     *         on the token
     */
    LabelSuggestionResult getSuggestedLabels(final User user, final Long issueId, final Long customFieldId, String token);

    @PublicApi
    public static class LabelSuggestionResult extends ServiceResultImpl
    {
        private final Set<String> suggestions;

        public LabelSuggestionResult(final Set<String> suggestions, final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.suggestions = suggestions;
        }

        public Set<String> getSuggestions()
        {
            return suggestions;
        }
    }

    @PublicApi
    public static class LabelsResult extends ServiceResultImpl
    {
        private final Set<Label> labels;

        public LabelsResult(final Set<Label> labels, final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.labels = labels;
        }

        public Set<Label> getLabels()
        {
            return labels;
        }
    }

    public static abstract class LabelValidationResult extends ServiceResultImpl
    {
        private final Long issueId;
        private final Long customFieldId;

        public LabelValidationResult(final Long issueId, final Long customFieldId,
                final ErrorCollection errorCollection)
        {
            super(errorCollection);
            this.issueId = issueId;
            this.customFieldId = customFieldId;
        }

        public Long getCustomFieldId()
        {
            return customFieldId;
        }

        public Long getIssueId()
        {
            return issueId;
        }
    }

    @PublicApi
    public static class SetLabelValidationResult extends LabelValidationResult
    {
        private final Set<String> labels;

        public SetLabelValidationResult(final Long issueId, final Long customFieldId, final ErrorCollection errorCollection,
                final Set<String> labels)
        {
            super(issueId, customFieldId, errorCollection);
            this.labels = labels;
        }

        public Set<String> getLabels()
        {
            return labels;
        }
    }

    @PublicApi
    public static class AddLabelValidationResult extends LabelValidationResult
    {
        private final String label;

        public AddLabelValidationResult(final Long issueId, final Long customFieldId, final ErrorCollection errorCollection,
                final String label)
        {
            super(issueId, customFieldId, errorCollection);
            this.label = label;
        }

        public String getLabel()
        {
            return label;
        }
    }
}
