package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @since v3.13
 */
public class TestUserCustomFieldImporter
{
    @Test
    public void testCanMapImportValue()
    {
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final UserCustomFieldImporter userCustomFieldImporter = new UserCustomFieldImporter();

        // Check first user
        // No errors should be returned.
        assertNull(userCustomFieldImporter.canMapImportValue(projectImportMapper, getExternalCustomFieldValue("johnno"), null, null));
        // But we expect the username to be added to the required values.
        assertEquals(1, projectImportMapper.getUserMapper().getOptionalOldIds().size());
        assertTrue(projectImportMapper.getUserMapper().getOptionalOldIds().contains("johnno"));

        // Check for a second user
        // No errors should be returned.
        assertNull(userCustomFieldImporter.canMapImportValue(projectImportMapper, getExternalCustomFieldValue("davo"), null, null));
        // But we expect the username to be added to the required values.
        assertEquals(2, projectImportMapper.getUserMapper().getOptionalOldIds().size());
        assertTrue(projectImportMapper.getUserMapper().getOptionalOldIds().contains("johnno"));
        assertTrue(projectImportMapper.getUserMapper().getOptionalOldIds().contains("davo"));
    }

    private ExternalCustomFieldValue getExternalCustomFieldValue(final String value)
    {
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123456", "123", "8888");
        externalCustomFieldValue.setStringValue(value);
        return externalCustomFieldValue;
    }

    @Test
    public void testCanMapImportValueEmptyUsername()
    {
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final UserCustomFieldImporter userCustomFieldImporter = new UserCustomFieldImporter();

        // Test for null user
        // No errors should be returned.
        assertNull(userCustomFieldImporter.canMapImportValue(projectImportMapper, getExternalCustomFieldValue(null), null, null));
        // And nothing added as required.
        assertTrue(projectImportMapper.getUserMapper().getRequiredOldIds().isEmpty());

        // Test for emtpy Sting user
        // No errors should be returned.
        assertNull(userCustomFieldImporter.canMapImportValue(projectImportMapper, getExternalCustomFieldValue(""), null, null));
        // And nothing added as required.
        assertTrue(projectImportMapper.getUserMapper().getRequiredOldIds().isEmpty());
    }

    @Test
    public void testGetMappedImportValue()
    {
        final UserUtil userUtil = mock(UserUtil.class);
        final UserCustomFieldImporter userCustomFieldImporter = new UserCustomFieldImporter();
        // Test with null original value
        assertNull(userCustomFieldImporter.getMappedImportValue(new ProjectImportMapperImpl(userUtil, null), getExternalCustomFieldValue(null), null).getValue());
        // Test with empty original value
        assertEquals("", userCustomFieldImporter.getMappedImportValue(new ProjectImportMapperImpl(userUtil, null), getExternalCustomFieldValue(""), null).getValue());
        // Test with non-empty original value
        assertEquals("john.doe", userCustomFieldImporter.getMappedImportValue(new ProjectImportMapperImpl(userUtil, null), getExternalCustomFieldValue("john.doe"), null).getValue());
    }
}
