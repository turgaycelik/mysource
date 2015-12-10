package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestNoTransformationCustomFieldImporter
{
    @Test
    public void testCanMapImportValue()
    {
        final NoTransformationCustomFieldImporter noTransformationCustomFieldImporter = new NoTransformationCustomFieldImporter();
        assertNull(noTransformationCustomFieldImporter.canMapImportValue(null, null, null, null));
    }

    @Test
    public void testGetMappedImportValue()
    {
        final NoTransformationCustomFieldImporter noTransformationCustomFieldImporter = new NoTransformationCustomFieldImporter();
        // Test with null original value
        assertNull(noTransformationCustomFieldImporter.getMappedImportValue(new ProjectImportMapperImpl(null, null), getExternalCustomFieldValue(null), null).getValue());
        // Test with empty original value
        assertEquals("", noTransformationCustomFieldImporter.getMappedImportValue(new ProjectImportMapperImpl(null, null), getExternalCustomFieldValue(""), null).getValue());
        // Test with non-empty original value
        assertEquals("La de dah", noTransformationCustomFieldImporter.getMappedImportValue(new ProjectImportMapperImpl(null, null), getExternalCustomFieldValue("La de dah"), null).getValue());
    }

    private ExternalCustomFieldValue getExternalCustomFieldValue(final String value)
    {
        final ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123456", "123", "8888");
        externalCustomFieldValue.setStringValue(value);
        return externalCustomFieldValue;
    }

}
