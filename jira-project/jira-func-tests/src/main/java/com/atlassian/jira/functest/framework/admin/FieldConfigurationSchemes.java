package com.atlassian.jira.functest.framework.admin;

/**
 * Actions to be performed on the Field Configuration Schemes in JIRA's administration interface.
 *
 * @since v4.0
 */
public interface FieldConfigurationSchemes
{
    /**
     * @param name the name of the new scheme
     * @param description the description of the new scheme; optional (use null to ignore)
     * @return the id of the new field configuration scheme
     */
    String addFieldConfigurationScheme(String name, String description);

    /**
     * @param name the name of the field configuration scheme
     * @return a reference to a {@link com.atlassian.jira.functest.framework.admin.FieldConfigurationSchemes.FieldConfigurationScheme} instance
     * that can be used to perform actions on the scheme. Note that only one field configuration scheme can be accessed
     * at a time; if this method is called again, any prior references will now refer to the new scheme.
     */
    FieldConfigurationScheme fieldConfigurationScheme(String name);

    /**
     * Represents a field configuration scheme that actions can be carried out on
     */
    interface FieldConfigurationScheme
    {
        /**
         * @param issueTypeId the issue type id (e.g. Bug = "1"); must not be null
         * @param fieldConfigurationName the name of the field configuration to associate to the issue type e.g. <code>Default Field Configuration</code>
         */
        void addAssociation(String issueTypeId, String fieldConfigurationName);

        /**
         * @param issueTypeId the issue type id (e.g. Bug = "1"); null represents the "unmapped" association
         * @param newFieldConfigurationName the name of the new field configuration to associate to the issue type e.g. <code>Default Field Configuration</code>
         */
        void editAssociation(String issueTypeId, String newFieldConfigurationName);

        /**
         * @param issueTypeId the issue type id (e.g. Bug = "1"); must not be null
         */
        void removeAssociation(String issueTypeId);

        /**
         * Navigates to the configuration page for this field configuration scheme.
         * @return this field configuration scheme.
         */
        FieldConfigurationScheme goTo();
    }
}
