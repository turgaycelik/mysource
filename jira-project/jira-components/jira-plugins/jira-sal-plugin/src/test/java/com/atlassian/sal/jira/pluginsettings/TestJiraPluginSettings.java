package com.atlassian.sal.jira.pluginsettings;

import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestJiraPluginSettings
{
    @Mock
    private PropertySet propertySet;
    private JiraPluginSettings jiraPluginSettings;

    @Before
    public void setUp()
    {
        initMocks(this);
        ResettableLazyReference<PropertySet> propertySetRef = new ResettableLazyReference<PropertySet>()
        {
            @Override
            protected PropertySet create() throws Exception
            {
                return propertySet;
            }
        };

        jiraPluginSettings = new JiraPluginSettings(propertySetRef);
    }

    @Test
    public void testPropertySetSimpleString()
    {
        jiraPluginSettings.putActual("key", "value");
        verify(propertySet).exists("key");
        verify(propertySet).setString("key", "value");
        verifyNoMoreInteractions(propertySet);
    }

    @Test
    public void testPropertySetStringExists()
    {
        when(propertySet.exists("key")).thenReturn(true);
        when(propertySet.getType("key")).thenReturn(PropertySet.STRING);
        when(propertySet.getString("key")).thenReturn("oldvalue");
        jiraPluginSettings.putActual("key", "value");
        verify(propertySet).remove("key");
        verify(propertySet).setString("key", "value");
    }

    @Test
    public void testPropertySetVeryLongString()
    {
        String veryLongString = "thiiiiiiiiiiiiiisIIIIIIIIIIIIIsssssssVEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEERYLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONGSTRIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIINGbaaaaaaaaaaar";
        jiraPluginSettings.putActual("key", veryLongString);
        verify(propertySet).setText("key", veryLongString);
    }

    @Test
    public void testGetNotExists()
    {
        assertNull(jiraPluginSettings.getActual("key"));
    }

    @Test
    public void testGetNotRightType()
    {
        when(propertySet.exists("key")).thenReturn(true);
        when(propertySet.getType("key")).thenReturn(PropertySet.INT);
        assertNull(jiraPluginSettings.getActual("key"));
    }

    @Test
    public void testGetString()
    {
        when(propertySet.exists("key")).thenReturn(true);
        when(propertySet.getType("key")).thenReturn(PropertySet.STRING);
        when(propertySet.getString("key")).thenReturn("blah");
        assertEquals("blah", jiraPluginSettings.getActual("key"));
    }

    @Test
    public void testGetText()
    {
        when(propertySet.exists("key")).thenReturn(true);
        when(propertySet.getType("key")).thenReturn(PropertySet.TEXT);
        when(propertySet.getText("key")).thenReturn("blah");
        assertEquals("blah", jiraPluginSettings.getActual("key"));
    }

    @Test
    public void testRemove()
    {
        jiraPluginSettings.removeActual("key");
        verify(propertySet).remove("key");
    }

}
