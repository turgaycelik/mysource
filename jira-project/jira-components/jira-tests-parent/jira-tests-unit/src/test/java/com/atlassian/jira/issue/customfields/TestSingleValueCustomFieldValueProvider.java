package com.atlassian.jira.issue.customfields;

import java.lang.reflect.Method;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestSingleValueCustomFieldValueProvider
{
    @Test
    public void testGetStringValue() throws Exception
    {
        final Method method = SingleValueCustomFieldValueProvider.class.getMethod("getStringValue", CustomField.class, FieldValuesHolder.class);
        _testGetSingleString(method);
        _testGetList(method);
        _testGetEmptyList(method);
        _testGetNull(method);
    }
    
    @Test
    public void testGetValue() throws Exception
    {
        final Method method = SingleValueCustomFieldValueProvider.class.getMethod("getValue", CustomField.class, FieldValuesHolder.class);
        _testGetSingleString(method);
        _testGetList(method);
        _testGetEmptyList(method);
        _testGetNull(method);
    }

    private void _testGetSingleString(Method method) throws Exception
    {
        final CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(customFieldType.getStringValueFromCustomFieldParams(null)).thenReturn("string");

        final CustomField customField = mock(CustomField.class);
        when(customField.getCustomFieldValues(null)).thenReturn(null);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);

        assertEquals("string", method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));
    }    

    private void _testGetList(Method method) throws Exception
    {
        final CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(customFieldType.getStringValueFromCustomFieldParams(null)).thenReturn(singletonList("string"));

        final CustomField customField = mock(CustomField.class);
        when(customField.getCustomFieldValues(null)).thenReturn(null);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);

        assertEquals("string", method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));
    }

    private void _testGetEmptyList(Method method) throws Exception
    {
        final CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(customFieldType.getStringValueFromCustomFieldParams(null)).thenReturn(emptyList());

        final CustomField customField = mock(CustomField.class);
        when(customField.getCustomFieldValues(null)).thenReturn(null);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);

        assertNull(method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));
    }
    
    private void _testGetNull(Method method) throws Exception
    {
        final CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(customFieldType.getStringValueFromCustomFieldParams(null)).thenReturn(null);

        final CustomField customField = mock(CustomField.class);
        when(customField.getCustomFieldValues(null)).thenReturn(null);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);

        assertNull(method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));
    }
}
