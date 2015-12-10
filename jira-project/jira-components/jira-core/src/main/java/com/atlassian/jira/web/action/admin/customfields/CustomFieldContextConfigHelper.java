package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Simple helper class for doing some work around custom field configuration contexts and the reindex message manager
 * in the web action domain.
 *
 * @since v4.0
 */
public interface CustomFieldContextConfigHelper
{
    /**
     * Determines if adding the project and issue type contexts to the custom field will affect any issues.
     *
     * @param user the user
     * @param customField the custom field being altered
     * @param projectContexts the projects of the new configuration context
     * @param issueTypes the issue types of the new configuration context
     * @param isNewCustomField true if the field has just been added; false otherwise
     * @return true if issues are affected; false otherwise
     */
    boolean doesAddingContextToCustomFieldAffectIssues(final User user, final CustomField customField, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes, final boolean isNewCustomField);

    /**
     * Determines if altering an existing configuration context for a custom field will affect any issues.
     *
     * @param user the user
     * @param customField the custom field being altered
     * @param oldFieldConfigScheme the old field config scheme
     * @param isNewSchemeGlobal if modified scheme will be global or not
     * @param newProjectContexts the projects in the new context
     * @param newIssueTypes the issue types in the new context
     * @return true if issues are affected; false otherwise.
     */
    boolean doesChangingContextAffectIssues(final User user, final CustomField customField, final FieldConfigScheme oldFieldConfigScheme, final boolean isNewSchemeGlobal, final List<JiraContextNode> newProjectContexts, final List<GenericValue> newIssueTypes);

    /**
     * Determines if removing an existing configuration context for a custom field will affect any issues.
     *
     * @param user the user
     * @param customField the custom field
     * @param fieldConfigScheme the scheme to be removed
     * @return true if issues are affected; false otherwise.
     */
    boolean doesRemovingSchemeFromCustomFieldAffectIssues(final User user, final CustomField customField, final FieldConfigScheme fieldConfigScheme);
}
