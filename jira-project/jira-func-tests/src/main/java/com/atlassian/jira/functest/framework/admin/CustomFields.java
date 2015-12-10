package com.atlassian.jira.functest.framework.admin;

/**
 * Actions for administering the custom fields in JIRA.
 *
 * @since v4.0
 */
public interface CustomFields
{
    /**
     * Adds a custom field of the specified type to JIRA.
     *
     * The default searcher is used, and the custom field is added to the global project/issue type context.
     *
     * The field is added to the Default Screen.
     *
     * @param fieldType the full name of the custom field type to add e.g. <code>com.atlassian.jira.plugin.system.customfieldtypes:userpicker</code>
     * @param fieldName the name of the custom field e.g. <code>UserCF</code>
     * @return <p>A formatted String containing the id of the custom field, the format used is:
     * <tt>customfield_<strong>customfield-id</strong></tt></p>
     *
     * <p>For example, <code>customfield_10000</code></p>
     */
    String addCustomField(String fieldType, String fieldName);

    /**
     * Adds a custom field of the specified type to JIRA.
     *
     * The default searcher is used. The context is created for the specified issue types and projects.
     *
     * The field is added to the Default Screen.
     *
     * @param fieldType the full name of the custom field type to add e.g. <code>com.atlassian.jira.plugin.system.customfieldtypes:userpicker</code>
     * @param fieldName the name of the custom field e.g. <code>UserCF</code>
     * @param issueTypeIds the ids of the issue types; must not be null
     * @param projectIds the ids of the projects; must not be null
     * @return the custom field id e.g. <code>customfield_10000</code>
     */
    String addCustomField(String fieldType, String fieldName, String[] issueTypeIds, String[] projectIds);

    /**
     * Sets the searcher for the specified custom field. Use the return value to reset the searcher to its previous value
     * later.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param searcherKey the full key of the searcher to use e.g. <code>com.atlassian.jira.plugin.system.customfieldtypes:numberrange</code>.
     * Use <code>null</code> to remove the searcher.
     * @return the previously used searcher key
     */
    String setCustomFieldSearcher(String numericCustomFieldId, String searcherKey);

    /**
     * Renames the custom field.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param newCustomFieldName the new name for the custom field e.g. <code>My CF</code>
     * @return the previously used name
     */
    String renameCustomField(String numericCustomFieldId, String newCustomFieldName);

    /**
     * Adds a custom field configuration scheme context.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param label the new name
     * @param issueTypeIds the ids of the issue types; must not be null
     * @param projectIds the ids of the projects; must not be null
     * @return the numeric id of the field config scheme created e.g. <code>10013</code>
     */
    String addConfigurationSchemeContext(final String numericCustomFieldId, String label, String[] issueTypeIds, String[] projectIds);

    /**
     * Alters an existing custom field configuration scheme context.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param fieldConfigSchemeId the id of the scheme
     * @param label the new name; use null if no update required
     * @param issueTypeIds the ids of the issue types; use null if no update required
     * @param projectIds the ids of the projects; use null if no update required
     */
    void editConfigurationSchemeContextById(final String numericCustomFieldId, String fieldConfigSchemeId, String label, String[] issueTypeIds, String[] projectIds);

    /**
     * Alters an existing custom field configuration scheme context.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param label the label of the scheme
     * @param newLabel the new name; use null if no update required
     * @param issueTypeIds the ids of the issue types; use null if no update required
     * @param projectIds the ids of the projects; use null if no update required
     */
    void editConfigurationSchemeContextByLabel(final String numericCustomFieldId, String label, String newLabel, String[] issueTypeIds, String[] projectIds);

    /**
     * Removes the global context from the specified custom field's configuration schemes.
     *
     * @param numericCustomFieldId the numeric custom field id
     */
    void removeGlobalContext(String numericCustomFieldId);

    /**
     * Removes the context from the specified custom field with the specified config scheme id.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param fieldConfigSchemeId the numeric field config scheme id
     */
    void removeConfigurationSchemeContextById(String numericCustomFieldId, String fieldConfigSchemeId);

    /**
     * Removes the context from the specified custom field with the specified config scheme id.
     *
     * @param numericCustomFieldId the numeric custom field id
     * @param fieldConfigSchemeLabel the label of the field config scheme
     */
    void removeConfigurationSchemeContextByLabel(String numericCustomFieldId, String fieldConfigSchemeLabel);

    /**
     * Removes the custom field
     *
     * @param customFieldId the full custom field id e.g. <code>customfield_10000</code>
     */
    void removeCustomField(String customFieldId);

    /**
     * Add options to a customfield that supports it (i.e. selectlist)
     * @param numericCustomFieldId the numeric customfield id
     * @param options the options to add
     */
    void addOptions(String numericCustomFieldId, String... options);


    /**
     * Set default value for any custom field that supports default value.
     *
     * @param numericCustomFieldId the numeric customfield id
     * @param defValue default value
     */
    void setDefaultValue(String numericCustomFieldId, String defValue);

    /**
     * disable options from a customfield that supports them (i.e. selectlist)
     * @param numericCustomFieldId the numeric custom field id
     * @param options to disable
     */
    void disableOptions(String numericCustomFieldId, String... options);

    /**
     * enable options from a customfield that supports them (i.e. selectlist)
     * @param numericCustomFieldId the numeric custom field id
     * @param options to enable
     */
    void enableOptions(String numericCustomFieldId, String... options);

    /**
     * Remove options from a customfield that supports them (i.e. selectlist)
     * @param numericCustomFieldId the numeric custom field id
     * @param options to remove
     */
    void removeOptions(String numericCustomFieldId, String... options);

    /**
     * Change teh display value of an option from a customfield that supports them (i.e. selectlist)
     * @param numericCustomFieldId the numeric custom field id
     * @param option id to change
     */
    void editOptionValue(String numericCustomFieldId, String option, String newValue);
}
