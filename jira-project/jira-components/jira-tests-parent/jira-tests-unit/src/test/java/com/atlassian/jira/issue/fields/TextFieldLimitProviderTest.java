package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TextFieldLimitProviderTest
{
    private static final int DEFAULT_LIMIT_FOR_INCORRECT_PROPERTIES = 30000;
    private ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
    private TextFieldLimitProvider provider;

    @Before
    public void setUp()
    {
        provider = new TextFieldLimitProvider(applicationProperties);
    }

    @Test
    public void testProviderReturnsDefaultValueIfApplcationPropertyIsNull() throws Exception
    {
        expectMockApplicationPropertiesGetDefaultBackedString(null);
        assertEquals(30000, provider.getTextFieldLimit());
    }

    @Test
    public void testProviderReturnsDefaultValueIfApplcationPropertyIsEmpty() throws Exception
    {
        expectMockApplicationPropertiesGetDefaultBackedString("");
        assertEquals(DEFAULT_LIMIT_FOR_INCORRECT_PROPERTIES, provider.getTextFieldLimit());
    }

    @Test
    public void testProviderReturnsDefaultValueIfApplcationPropertyIsUnparsableNumber() throws Exception
    {
        expectMockApplicationPropertiesGetDefaultBackedString("10invalid");
        assertEquals(DEFAULT_LIMIT_FOR_INCORRECT_PROPERTIES, provider.getTextFieldLimit());
    }

    @Test
    public void testProviderReturnsValueFromApplicationProperties() throws Exception
    {
        expectMockApplicationPropertiesGetDefaultBackedString("10000");
        assertEquals(10000, provider.getTextFieldLimit());
    }

    private void expectMockApplicationPropertiesGetDefaultBackedString(String value)
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_TEXT_FIELD_CHARACTER_LIMIT)).thenReturn(value);
    }
}
