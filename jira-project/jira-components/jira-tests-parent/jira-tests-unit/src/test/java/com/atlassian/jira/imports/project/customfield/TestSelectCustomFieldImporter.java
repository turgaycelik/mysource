package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.util.MessageSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestSelectCustomFieldImporter
{
    @Test
    public void testGetMappedImportValue()
    {
        SelectCustomFieldImporter selectCustomFieldImporter = new SelectCustomFieldImporter();
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("10", "1", "20");
        externalCustomFieldValue.setStringValue("12");

        projectImportMapper.getCustomFieldOptionMapper().mapValue("12", "1012");
        projectImportMapper.getCustomFieldOptionMapper().mapValue("6", "1006");

        final ProjectCustomFieldImporter.MappedCustomFieldValue mappedCustomFieldValue = selectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, null);

        assertEquals("1012", mappedCustomFieldValue.getValue());
        assertNull(mappedCustomFieldValue.getParentKey());
    }

    @Test
    public void testCanMapImportValue()
    {
        SelectCustomFieldImporter selectCustomFieldImporter = new SelectCustomFieldImporter();
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl("12", "22", "32");
        customFieldValue.setStringValue("15");

        MessageSet messageSet = selectCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, null);
        assertNull(messageSet);
        assertEquals(1, projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().size());
        assertEquals("15", projectImportMapper.getCustomFieldOptionMapper().getRequiredOldIds().iterator().next());

    }

}
