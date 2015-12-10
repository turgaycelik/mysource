package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestProjectPickerCustomFieldImporter
{
    @Test
    public void testCanMap() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().registerOldValue("123", "Test Project");
        projectImportMapper.getProjectMapper().mapValue("123", "456");

        ProjectPickerCustomFieldImporter projectPickerCustomFieldImporter = new ProjectPickerCustomFieldImporter();

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("333", "444", "555");
        externalCustomFieldValue.setStringValue("123");

        final MessageSet messageSet = projectPickerCustomFieldImporter.canMapImportValue(projectImportMapper, externalCustomFieldValue, null, new MockI18nBean());
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testCantMapGeneratesWarnings() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().registerOldValue("123", "Test Project");

        ProjectPickerCustomFieldImporter projectPickerCustomFieldImporter = new ProjectPickerCustomFieldImporter();

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("333", "444", "555");
        externalCustomFieldValue.setStringValue("123");

        final MessageSet messageSet = projectPickerCustomFieldImporter.canMapImportValue(projectImportMapper, externalCustomFieldValue, null, new MockI18nBean());
        assertTrue(messageSet.hasAnyMessages());
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals("The project 'Test Project' does not exist in the current JIRA system. This custom field value will not be imported.", messageSet.getWarningMessages().iterator().next());
    }

    @Test
    public void testGetMappedImportValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().registerOldValue("123", "Test Project");
        projectImportMapper.getProjectMapper().mapValue("123", "456");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("333", "444", "555");
        externalCustomFieldValue.setStringValue("123");

        ProjectPickerCustomFieldImporter projectPickerCustomFieldImporter = new ProjectPickerCustomFieldImporter();

        final ProjectCustomFieldImporter.MappedCustomFieldValue importValue = projectPickerCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, null);
        assertEquals("456", importValue.getValue());
        assertNull(importValue.getParentKey());
    }

    @Test
    public void testGetMappedImportValueNoMapping() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().registerOldValue("123", "Test Project");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("333", "444", "555");
        externalCustomFieldValue.setStringValue("123");

        ProjectPickerCustomFieldImporter projectPickerCustomFieldImporter = new ProjectPickerCustomFieldImporter();

        final ProjectCustomFieldImporter.MappedCustomFieldValue importValue = projectPickerCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, null);
        assertNull(importValue.getValue());
        assertNull(importValue.getParentKey());
    }

    @Test
    public void testGetMappedImportValueNullExternalValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().registerOldValue("123", "Test Project");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("333", "444", "555");

        ProjectPickerCustomFieldImporter projectPickerCustomFieldImporter = new ProjectPickerCustomFieldImporter();

        final ProjectCustomFieldImporter.MappedCustomFieldValue importValue = projectPickerCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, null);
        assertNull(importValue.getValue());
        assertNull(importValue.getParentKey());
    }

}
