package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.MessageSet;

import org.junit.Test;

import static com.atlassian.jira.mock.Strict.strict;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @since v3.13
 */
public class TestGroupCustomFieldImporter
{
    @Test
    public void testGetMappedImportValue()
    {
        final ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl("1", "2", "3");
        customFieldValue.setStringValue("n00bs");

        final GroupCustomFieldImporter groupCustomFieldImporter = new GroupCustomFieldImporter(null);
        final ProjectCustomFieldImporter.MappedCustomFieldValue mappedCustomFieldValue = groupCustomFieldImporter.getMappedImportValue(null, customFieldValue, null);

        assertEquals("n00bs", mappedCustomFieldValue.getValue());
        assertThat(mappedCustomFieldValue.getParentKey(), nullValue());
    }

    @Test
    public void testCanMapImportValue()
    {
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        GroupManager mockGroupManager = mock(GroupManager.class, strict());
        doReturn(true).when(mockGroupManager).groupExists("n00bs");
        doReturn(false).when(mockGroupManager).groupExists("l33tz");

        final ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl("1", "2", "3");
        GroupCustomFieldImporter groupCustomFieldImporter = new GroupCustomFieldImporter(mockGroupManager);
        MockI18nHelper i18n = new MockI18nHelper();

        // check "n00bs"
        customFieldValue.setStringValue("n00bs");
        MessageSet messageSet = groupCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, i18n);
        assertNoMessages(messageSet);

        // check "l33tz"
        customFieldValue.setStringValue("l33tz");
        messageSet = groupCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, i18n);
        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.group.validation.does.not.exist [l33tz]");

        // throw in an empty String for coverage
        customFieldValue.setStringValue("");
        messageSet = groupCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, i18n);
        assertNoMessages(messageSet);
        assertThat(projectImportMapper.getGroupMapper().getRequiredOldIds(), containsInAnyOrder("n00bs", "l33tz"));
    }
}
