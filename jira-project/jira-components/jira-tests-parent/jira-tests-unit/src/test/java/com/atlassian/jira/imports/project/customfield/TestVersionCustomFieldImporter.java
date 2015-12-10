package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestVersionCustomFieldImporter
{
    @Test
    public void testCanMapImportValue() throws Exception
    {
        VersionCustomFieldImporter versionCustomFieldImporter = new VersionCustomFieldImporter();
        assertNull(versionCustomFieldImporter.canMapImportValue(null, null, null, null));
    }

    @Test
    public void testGetMappedImportValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getVersionMapper().mapValue("12", "14");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("1", "2", "3");
        externalCustomFieldValue.setStringValue("12");

        VersionCustomFieldImporter versionCustomFieldImporter = new VersionCustomFieldImporter();

        final ProjectCustomFieldImporter.MappedCustomFieldValue importValue = versionCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, null);
        assertEquals("14", importValue.getValue());
    }
    
    @Test
    public void testGetMappedImportValueNoMapping() throws Exception
    {
        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getName();
        mockCustomFieldControl.setReturnValue("Test Custom Field");
        mockCustomFieldControl.replay();

        final MockControl mockFieldConfigControl = MockControl.createStrictControl(FieldConfig.class);
        final FieldConfig mockFieldConfig = (FieldConfig) mockFieldConfigControl.getMock();
        mockFieldConfig.getCustomField();
        mockFieldConfigControl.setReturnValue(mockCustomField);
        mockFieldConfigControl.replay();
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("1", "2", "3");
        externalCustomFieldValue.setStringValue("12.0");

        VersionCustomFieldImporter versionCustomFieldImporter = new VersionCustomFieldImporter();

        final ProjectCustomFieldImporter.MappedCustomFieldValue importValue = versionCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, mockFieldConfig);
        assertNull(importValue.getValue());
        mockFieldConfigControl.verify();
        mockCustomFieldControl.verify();
    }

}
