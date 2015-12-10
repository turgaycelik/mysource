package com.atlassian.jira.mail;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.config.properties.APKeys.JIRA_MAIL_ENCODING;
import static com.atlassian.jira.config.properties.APKeys.JIRA_WEBWORK_ENCODING;
import static com.atlassian.jira.config.properties.ApplicationPropertiesImpl.DEFAULT_ENCODING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMailEncoding
{
    private static final String ENCODING = "someEncoding";

    private ApplicationProperties applicationProperties;
    @Mock private ApplicationPropertiesStore mockApplicationPropertiesStore;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        applicationProperties = new ApplicationPropertiesImpl(mockApplicationPropertiesStore);
    }

    @Test
    public void encodingShouldBeTheSetValueIfExplicitlySet()
    {
        // Set up
        when(mockApplicationPropertiesStore.getStringFromDb(JIRA_WEBWORK_ENCODING)).thenReturn(ENCODING);

        // Invoke
        final String encoding = applicationProperties.getEncoding();

        // Check
        assertEquals(ENCODING, encoding);
    }

    @Test
    public void encodingShouldBeTheDefaultIfNotExplicitlySet()
    {
        // Set up
        when(mockApplicationPropertiesStore.getStringFromDb(JIRA_WEBWORK_ENCODING)).thenReturn(null);

        // Invoke
        final String encoding = applicationProperties.getEncoding();

        // Check
        assertEquals(DEFAULT_ENCODING, encoding);
        verify(mockApplicationPropertiesStore).setString(JIRA_WEBWORK_ENCODING, DEFAULT_ENCODING);
    }

    @Test
    public void mailEncodingShouldBeTheSetValueIfExplicitlySet()
    {
        // Set up
        when(mockApplicationPropertiesStore.getString(JIRA_MAIL_ENCODING)).thenReturn(ENCODING);

        // Invoke
        final String encoding = applicationProperties.getMailEncoding();

        // Check
        assertEquals(ENCODING, encoding);
    }

    @Test
    public void mailEncodingShouldBeTheDefaultIfNeitherEncodingExplicitlySet()
    {
        // Set up
        when(mockApplicationPropertiesStore.getString(JIRA_MAIL_ENCODING)).thenReturn("");
        when(mockApplicationPropertiesStore.getStringFromDb(JIRA_WEBWORK_ENCODING)).thenReturn("");

        // Invoke
        final String encoding = applicationProperties.getMailEncoding();

        // Check
        assertEquals(DEFAULT_ENCODING, encoding);
        verify(mockApplicationPropertiesStore).setString(JIRA_WEBWORK_ENCODING, DEFAULT_ENCODING);
    }

    @Test
    public void mailEncodingShouldBeTheWebEncodingIfNoMailEncodingExplicitlySet()
    {
        // Set up
        when(mockApplicationPropertiesStore.getString(JIRA_MAIL_ENCODING)).thenReturn("");
        when(mockApplicationPropertiesStore.getStringFromDb(JIRA_WEBWORK_ENCODING)).thenReturn(ENCODING);

        // Invoke
        final String encoding = applicationProperties.getMailEncoding();

        // Check
        assertEquals(ENCODING, encoding);
    }
}
