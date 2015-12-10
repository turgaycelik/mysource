package com.atlassian.jira.notification.type;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;

/**
 * {@link com.atlassian.jira.issue.customfields.CustomFieldType CustomFieldType's} should implement this interface if it wants to be
 * selectable from the user custom field drop down list when adding new notifications. The implementing class must
 * make sure that the {@link Object} returned from {@link com.atlassian.jira.issue.customfields.CustomFieldType#getValueFromIssue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue) CustomFieldType#getValueFromIssue(CustomField, Issue)}
 * is a {@link com.atlassian.crowd.embedded.api.User User} or a {@link java.util.Collection collection} of {@link com.atlassian.crowd.embedded.api.User User}
 * to function properly.
 *
 * @since v3.12
 */
public interface UserCFNotificationTypeAware
{
    /**
     * Retrieves the {@link com.atlassian.crowd.embedded.api.User User} or a {@link java.util.Collection collection} of {@link com.atlassian.crowd.embedded.api.User Users}
     * representing the <strong>current</strong> CustomField value for the given issue.
     *
     * @param customField Custom field for which to retrieve user(s) from
     * @param issue Issue from which to retrieve the user(s)
     * @return {@link com.atlassian.crowd.embedded.api.User User} or a {@link java.util.Collection collection} of {@link com.atlassian.crowd.embedded.api.User Users}
     * @see com.atlassian.jira.issue.customfields.CustomFieldType#getValueFromIssue(com.atlassian.jira.issue.fields.CustomField, com.atlassian.jira.issue.Issue)
     */
    public Object getValueFromIssue(CustomField customField, Issue issue);
}
