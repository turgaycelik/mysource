package com.atlassian.jira.charts.portlet;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.junit.rules.InitMockitoMocks;

import com.google.common.collect.ImmutableSet;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.matchers.MapMatchers.isMapOf;
import static com.atlassian.jira.matchers.MapMatchers.isSingletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * @since v4.0
 */
public class TestDateFieldValuesGenerator
{
    @Rule public final InitMockitoMocks initMocks = new InitMockitoMocks(this);

    @Mock private FieldManager fieldManager;


    private DateFieldValuesGenerator createGenerator()
    {
        return new DateFieldValuesGenerator()
        {
            @Override
            FieldManager getFieldManager()
            {
                return fieldManager;
            }
        };
    }

    private NavigableField newMockDateField(String id, String name)
    {
        final NavigableField mockField = mock(NavigableField.class, withSettings().extraInterfaces(DateField.class));
        when(mockField.getId()).thenReturn(id);
        when(mockField.getName()).thenReturn(name);
        return mockField;
    }

    private CustomField newMockCustomField(String id, String name)
    {
        final CustomField mockField = mock(CustomField.class);
        when(mockField.getId()).thenReturn(id);
        when(mockField.getName()).thenReturn(name);
        return mockField;
    }


    @Test
    public void testGetValuesNoFields() throws FieldException
    {
        when(fieldManager.getAllAvailableNavigableFields()).thenReturn(Collections.<NavigableField>emptySet());

        final Map map = createGenerator().getValues(null);
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    @Test
    public void testGetSystemFields() throws FieldException
    {
        final NavigableField searchableField = mock(NavigableField.class, withSettings().extraInterfaces(SearchableField.class));
        final NavigableField createdSystemField = newMockDateField("created", "Created");
        when(fieldManager.getAllAvailableNavigableFields()).thenReturn(ImmutableSet.<NavigableField>of(createdSystemField, searchableField));
        when(fieldManager.isCustomField(searchableField)).thenReturn(false);
        when(fieldManager.isCustomField(createdSystemField)).thenReturn(false);

        @SuppressWarnings ("unchecked") final Map<String,String> map = createGenerator().getValues(null);
        assertNotNull(map);
        assertThat(map, isSingletonMap("created", "Created"));
    }

    @Test
    public void testGetSystemAndTextCustomFields() throws FieldException
    {
        final NavigableField someSearchableField = mock(NavigableField.class, withSettings().extraInterfaces(SearchableField.class));
        final NavigableField createdSystemField = newMockDateField("created", "Created");

        final CustomFieldType mockTextCustomFieldType = mock(CustomFieldType.class);
        final CustomField textCustomField = mock(CustomField.class);
        when(textCustomField.getCustomFieldType()).thenReturn(mockTextCustomFieldType);

        when(fieldManager.getAllAvailableNavigableFields()).thenReturn(ImmutableSet.of(createdSystemField,
                someSearchableField, textCustomField));
        when(fieldManager.isCustomField(someSearchableField)).thenReturn(false);
        when(fieldManager.isCustomField(createdSystemField)).thenReturn(false);
        when(fieldManager.isCustomField(textCustomField)).thenReturn(true);

        @SuppressWarnings ("unchecked") final Map<String,String> map = createGenerator().getValues(null);
        assertNotNull(map);
        assertThat(map, isSingletonMap("created", "Created"));
    }

    @Test
    public void testGetSystemAndDateCustomFields() throws FieldException
    {
        final NavigableField someSearchableField = mock(NavigableField.class, withSettings().extraInterfaces(SearchableField.class));
        final NavigableField createdSystemField = newMockDateField("created", "Created");

        final CustomFieldType dateCustomFieldType = mock(CustomFieldType.class, withSettings().extraInterfaces(DateField.class));
        final CustomField dateCustomField = newMockCustomField("customfield_10023", "My Date CF");
        when(dateCustomField.getCustomFieldType()).thenReturn(dateCustomFieldType);

        when(fieldManager.getAllAvailableNavigableFields()).thenReturn(ImmutableSet.of(createdSystemField,
                someSearchableField, dateCustomField));
        when(fieldManager.isCustomField(someSearchableField)).thenReturn(false);
        when(fieldManager.isCustomField(createdSystemField)).thenReturn(false);
        when(fieldManager.isCustomField(dateCustomField)).thenReturn(true);

        @SuppressWarnings ("unchecked") final Map<String,String> map = createGenerator().getValues(null);
        assertNotNull(map);
        assertThat(map, isMapOf("created", "Created", "customfield_10023", "My Date CF"));
    }

}
