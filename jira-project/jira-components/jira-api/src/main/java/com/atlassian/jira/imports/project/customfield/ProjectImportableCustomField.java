package com.atlassian.jira.imports.project.customfield;

import com.atlassian.annotations.PublicSpi;

/**
 * This is an interface that will need to be implemented if a custom field is able to be imported by the
 * project importer. When importing a project, if the {@link com.atlassian.jira.issue.customfields.CustomFieldType}
 * is an instance of this interface then we will attempt to perform mappings and import the custom field
 * data. If it is not an instance of this interface then we will alert the user in the importer that the
 * custom field is not importable.
 *
 * @since v3.13
 */
@PublicSpi
public interface ProjectImportableCustomField
{
    /**
     * Returns the object that will perform the actual project import functions for the custom field type.
     * @return the object that will perform the actual project import functions for the custom field type.
     */
    ProjectCustomFieldImporter getProjectImporter();
}