package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestCascadingSelectCustomFieldImporter
{
    @Test
    public void testCanMapImportValueNullOption()
    {
        CascadingSelectCustomFieldImporter cascadingSelectCustomFieldImporter = new CascadingSelectCustomFieldImporter();
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("10", "1", "20");
        externalCustomFieldValue.setStringValue(null);
        cascadingSelectCustomFieldImporter.canMapImportValue(projectImportMapper, externalCustomFieldValue, null, null);

        assertEquals(0, projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().size());
    }

    @Test
    public void testCanMapImportValueParentOption()
    {
        CascadingSelectCustomFieldImporter cascadingSelectCustomFieldImporter = new CascadingSelectCustomFieldImporter();
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("10", "1", "20");
        externalCustomFieldValue.setStringValue("12");
        cascadingSelectCustomFieldImporter.canMapImportValue(projectImportMapper, externalCustomFieldValue, null, null);

        assertEquals(1, projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().size());
        assertEquals("12", projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().iterator().next());
    }

    @Test
    public void testCanMapImportValueChildOption()
    {
        CascadingSelectCustomFieldImporter cascadingSelectCustomFieldImporter = new CascadingSelectCustomFieldImporter();
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("10", "1", "20");
        externalCustomFieldValue.setStringValue("12");
        externalCustomFieldValue.setParentKey("6");
        cascadingSelectCustomFieldImporter.canMapImportValue(projectImportMapper, externalCustomFieldValue, null, null);

        assertEquals(1, projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().size());
        assertEquals("12", projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().iterator().next());
    }

    @Test
    public void testGetMappedImportValue()
    {
        CascadingSelectCustomFieldImporter cascadingSelectCustomFieldImporter = new CascadingSelectCustomFieldImporter();
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("10", "1", "20");
        externalCustomFieldValue.setStringValue("12");
        externalCustomFieldValue.setParentKey("6");

        projectImportMapper.getCustomFieldOptionMapper().mapValue("12", "1012");
        projectImportMapper.getCustomFieldOptionMapper().mapValue("6", "1006");

        final ProjectCustomFieldImporter.MappedCustomFieldValue mappedCustomFieldValue = cascadingSelectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, null);

        assertEquals("1012", mappedCustomFieldValue.getValue());
        assertEquals("1006", mappedCustomFieldValue.getParentKey());
    }
}
