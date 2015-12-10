package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface FieldConfigSchemeManager
{
    /**
     * This magic value list contains a single null element. Do not change to an empty list.
     */
    final List<GenericValue> ALL_ISSUE_TYPES = Collections.singletonList(null);

    // --------------------------------------------------------------------------------------- Bandana Interface Methods
    void init();

    Object getValue(BandanaContext context, String key);

    Object getValue(BandanaContext context, String key, boolean lookUp);

    void setValue(BandanaContext context, String key, Object value);

    // ----------------------------------------------------------------------------------------------- Retrieval Methods
    List<FieldConfigScheme> getConfigSchemesForField(ConfigurableField field);

    /**
     * Retrieves the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} associated with the
     * {@link com.atlassian.jira.issue.fields.config.FieldConfig}
     *
     * @param fieldConfig the field config to retrieve the {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme} of; cannot be null.
     * @return the config scheme for the {@link com.atlassian.jira.issue.fields.config.FieldConfig}. Null if the config scheme can not be found.
     */
    FieldConfigScheme getConfigSchemeForFieldConfig(FieldConfig fieldConfig);

    FieldConfigScheme getFieldConfigScheme(Long configSchemeId);

    FieldConfigScheme createDefaultScheme(ConfigurableField field, List<JiraContextNode> contexts, List<GenericValue> issueTypes);

    /**
     * Updates the config schemes with the new contexts
     *
     * @param newScheme
     * @param contexts
     * @param field
     * @return The updated scheme
     */
    FieldConfigScheme updateFieldConfigScheme(FieldConfigScheme newScheme, List<JiraContextNode> contexts, ConfigurableField field);

    /**
     * Only update the name & description of a field
     * @param scheme scheme with the name &amp; description to be updated
     * @return the updated scheme
     */
    FieldConfigScheme updateFieldConfigScheme(FieldConfigScheme scheme);

    /**
     * Removes a field config scheme, as well as its associated contexts and field configs
     * (which includes option sets and generic configs)
     *
     * @param fieldConfigSchemeId the id of the field config scheme to remove
     */
    void removeFieldConfigScheme(Long fieldConfigSchemeId);

    FieldConfig getRelevantConfig(IssueContext issueContext, ConfigurableField field);

    FieldConfigScheme createFieldConfigScheme(FieldConfigScheme newConfigScheme, List<JiraContextNode> contexts, List<GenericValue> issueTypes, ConfigurableField field);

    FieldConfigScheme createDefaultScheme(ConfigurableField field, List<JiraContextNode> contexts);

    void removeSchemeAssociation(List<JiraContextNode> contexts, ConfigurableField configurableField);

    // ------------------------------------------------------------------------------------------- Informational Methods

    /**
     * Returns a non-null list of Projects associated with the given field.
     *
     * @param field the Field
     * @return a non-null list of Projects associated with the given field.
     *
     * @deprecated Use {@link #getAssociatedProjectObjects(ConfigurableField)} instead. Since v5.2.
     */
    List<GenericValue> getAssociatedProjects(ConfigurableField field);

    /**
     * Returns a non-null list of Projects associated with the given field.
     *
     * @param field the Field
     * @return a non-null list of Projects associated with the given field.
     */
    List<Project> getAssociatedProjectObjects(ConfigurableField field);

    FieldConfigScheme getRelevantConfigScheme(IssueContext issueContext, ConfigurableField field);

    /**
     * Returns a collection of {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme}s for all Configuration
     * Contexts that will become invalid after the issuetype has been removed.  That is the configuration contexts that
     * will no longer be linked to ANY issue types after the issue type passed is has been deleted.
     *
     * @since v3.11
     * @param issueType The issueType to be deleted
     * @return A collection of {@link com.atlassian.jira.issue.fields.config.FieldConfigScheme}s
     */
    Collection getInvalidFieldConfigSchemesForIssueTypeRemoval(IssueType issueType);

    /**
     * Given an issueType, this method will correctly remove the fieldConfigSchemes if necessary.  In other words
     * if a FieldConfigScheme is linked to only a single issueType, and we're deleting that issuetype then that
     * FieldConfigScheme will be deleted.  If a FieldConfigScheme is associated with multiple issueTypes, then only
     * the association for the issueType we're deleting will be removed, but the FieldConfigScheme will remain.
     *
     * @since v3.11
     * @param issueType The IssueType being deleted
     */
    void removeInvalidFieldConfigSchemesForIssueType(IssueType issueType);

    /**
     * Given a CustomField, this method will correctly remove the fieldConfigSchemes if necessary.  In other words
     * if a FieldConfigScheme is linked to only a single CustomField, and we're deleting that field then that
     * FieldConfigScheme will be deleted.  If a FieldConfigScheme is associated with multiple fields, then only
     * the association for the field we're deleting will be removed, but the FieldConfigScheme will remain.
     *
     * @since v3.13
     * @param customFieldId The id of the CustomField being deleted
     */
    void removeInvalidFieldConfigSchemesForCustomField(String customFieldId);
}