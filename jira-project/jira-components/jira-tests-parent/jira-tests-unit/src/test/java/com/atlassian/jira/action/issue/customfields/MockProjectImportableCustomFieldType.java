package com.atlassian.jira.action.issue.customfields;

import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;

/**
 * A mock, do nothing implementation of a CustomFieldType that is ProjectImportable
 *
 * @since v3.13
 */
public class MockProjectImportableCustomFieldType extends MockCustomFieldType implements ProjectImportableCustomField
{
    private ProjectCustomFieldImporter projectCustomFieldImporter;

    public MockProjectImportableCustomFieldType(ProjectCustomFieldImporter projectCustomFieldImporter)
    {
        this.projectCustomFieldImporter = projectCustomFieldImporter;
    }

    public MockProjectImportableCustomFieldType(String key, String name)
    {
        super(key, name);
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }
}
