package com.atlassian.jira.issue.fields.config.persistence;

import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FieldConfigSchemePersister
{
    FieldConfigScheme create(FieldConfigScheme configScheme, ConfigurableField field);

    FieldConfigScheme update(FieldConfigScheme configScheme);

    void remove(Long fieldConfigSchemeId);

    FieldConfigScheme getFieldConfigScheme(Long configSchemeId);

    List<FieldConfigScheme> getConfigSchemesForCustomField(ConfigurableField field);

    /**
     * Finds the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} that contains
     * the {@link com.atlassian.jira.issue.fields.config.FieldConfig}
     *
     * @param fieldConfig the config to find the containing {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} of; cannot be null.
     * @return the first scheme that contains the {@link com.atlassian.jira.issue.fields.config.FieldConfig}. Not Null.
     * @throws {@link com.atlassian.jira.exception.DataAccessException} if the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} is not found.
     */
    FieldConfigScheme getConfigSchemeForFieldConfig(FieldConfig fieldConfig);

    /**
     * Retrieves the ids of the field config schemes for the specified customfield id. This method should be used when
     * full {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} domain objects are not required, for
     * example when removing them from the system.
     *
     * @param customFieldId the id of the customfield
     * @return a list of ids of the {@link FieldConfigScheme} objects associated with this customfield. Should not be
     *         null.
     */
    List<Long> getConfigSchemeIdsForCustomFieldId(String customFieldId);

    FieldConfigScheme createWithDefaultValues(ConfigurableField field, Map<String, FieldConfig> configs);

    /**
     * Returns a collection of the {@link FieldConfigScheme}s that will be made invalid if the issueType is
     * removed (that is the FieldConfigScheme would no longer be bound to ANY issue types).
     * If there are still any other issue types that a FieldConfigScheme will be bound to after
     * the removal then it will not be included in the collection returned.
     *
     * @since 3.11
     * @param issueType to be removed
     * @return A collection of {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme}s or an empty collection.
     */
    Collection<FieldConfigScheme> getInvalidFieldConfigSchemeAfterIssueTypeRemoval(IssueType issueType);

    /**
     * Removes {@link FieldConfigScheme} associations for the given issue type. Please note that this method call
     * on it's own is NOT safe.  it removes rows from a many-to-many table without considering other entities in
     * the many-to-many relationship.  What this means is that if there's only one row left in the table, this
     * method may remove it, and you'll end up with orphaned values.  Only use this in conjunction with
     * {@link #getInvalidFieldConfigSchemeAfterIssueTypeRemoval(com.atlassian.jira.issue.issuetype.IssueType)}.
     *
     * Also see {@link com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManagerImpl#removeInvalidFieldConfigSchemesForIssueType(com.atlassian.jira.issue.issuetype.IssueType)}
     * for correct usage.
     *
     * @since 3.11
     * @param issueType the issue type to remove the associations for
     */
    void removeByIssueType(IssueType issueType);

    void init();
}
