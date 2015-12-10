package com.atlassian.jira.timezone;

import java.util.Locale;
import java.util.TimeZone;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
public class TestTimeZoneService
{
    private TimeZoneService timeZoneService;
    private ApplicationProperties applicationProperties;
    private I18nHelper i18nHelper;
    private PermissionManager permissionManager;
    private UserPreferencesManager userPreferencesManager;

    @Before
    public void setUp()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        applicationProperties = Mockito.mock(ApplicationProperties.class);
        permissionManager = Mockito.mock(PermissionManager.class);
        userPreferencesManager = Mockito.mock(UserPreferencesManager.class);
        timeZoneService = new TimeZoneServiceImpl(applicationProperties, permissionManager, userPreferencesManager);
        i18nHelper = Mockito.mock(I18nHelper.class);
    }


    @Test
    public void testGetDefaultTimeZoneInfoFromJVM() throws Exception
    {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        try
        {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            when(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).thenReturn(null);
            when(i18nHelper.getLocale()).thenReturn(Locale.UK);
            JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(new MockUser("bob"), new SimpleErrorCollection(), i18nHelper);
            TimeZoneInfo defaultTimeZoneInfo = timeZoneService.getDefaultTimeZoneInfo(serviceContext);
            assertEquals("UTC", defaultTimeZoneInfo.getTimeZoneId());

            String[] availableIDs = TimeZone.getAvailableIDs();
            for (String availableID : availableIDs)
            {
                TimeZone.setDefault(TimeZone.getTimeZone(availableID));
                TimeZoneInfo tempTimeZoneInfo = timeZoneService.getDefaultTimeZoneInfo(serviceContext);
                tempTimeZoneInfo.getRegionKey();
                tempTimeZoneInfo.getCity();
                try
                {
                    DateTimeZone dateTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone(availableID));
                    assertEquals(dateTimeZone.toTimeZone().getID(), tempTimeZoneInfo.getTimeZoneId());
                }
                catch (IllegalArgumentException ex)
                {
                    assertEquals(availableID, tempTimeZoneInfo.getTimeZoneId());
                }
            }
        }
        finally
        {
            TimeZone.setDefault(defaultTimeZone);
        }
    }


    @Test
    public void testUseSystemTimeZone() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).thenReturn("UTC");
        assertEquals(false, timeZoneService.useSystemTimeZone());
    }

    @Test
    public void testDontUseSystemTimeZone() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).thenReturn("");
        assertEquals(true, timeZoneService.useSystemTimeZone());

    }

    @Test
    public void testGetJVMTimeZoneInfo() throws Exception
    {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        try
        {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            when(i18nHelper.getLocale()).thenReturn(Locale.UK);
            JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(new MockUser("bob"), new SimpleErrorCollection(), i18nHelper);
            TimeZoneInfo jvmTimeZoneInfo = timeZoneService.getJVMTimeZoneInfo(serviceContext);
            assertEquals("UTC", jvmTimeZoneInfo.getTimeZoneId());
        }
        finally
        {
            TimeZone.setDefault(defaultTimeZone);
        }
    }

    @Test
    public void testSetInvalidDefaultTimeZone() throws Exception
    {
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(new MockUser("bob"), new SimpleErrorCollection(), i18nHelper);
        try
        {
            timeZoneService.setDefaultTimeZone("Etc/UTC", serviceContext);
            fail("IllegalArgumentException expected.");
        }
        catch (IllegalArgumentException ex)
        {
            //expected!
        }
    }

    @Test
    public void testSetValidDefaultTimeZone() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        timeZoneService.setDefaultTimeZone("Europe/Berlin", serviceContext);
        Mockito.verify(permissionManager).hasPermission(Permissions.ADMINISTER, user);
        Mockito.verify(applicationProperties).setString(APKeys.JIRA_DEFAULT_TIMEZONE, "Europe/Berlin");
    }

    @Test
    public void testClearDefaultTimeZone() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        timeZoneService.clearDefaultTimeZone(serviceContext);
        Mockito.verify(permissionManager).hasPermission(Permissions.ADMINISTER, user);
        Mockito.verify(applicationProperties).setString(APKeys.JIRA_DEFAULT_TIMEZONE, null);

    }

    @Test
    public void testGetDefaultTimeZoneRegionKeyUseSystemTimeZone() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).thenReturn(null);
        String defaultTimeZoneRegionKey = timeZoneService.getDefaultTimeZoneRegionKey();
        assertEquals(TimeZoneService.SYSTEM, defaultTimeZoneRegionKey);
    }

    @Test
    public void testGetDefaultTimeZoneRegionKeyDonTUseSystemTimeZone() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).thenReturn("Europe/Berlin");
        String defaultTimeZoneRegionKey = timeZoneService.getDefaultTimeZoneRegionKey();
        assertEquals("Europe", defaultTimeZoneRegionKey);
    }

    @Test
    public void testClearUserDefaultTimeZone() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        Preferences preferences = mock(Preferences.class);
        when(userPreferencesManager.getPreferences(user)).thenReturn(preferences);
        timeZoneService.clearUserDefaultTimeZone(serviceContext);
        Mockito.verify(preferences).setString(PreferenceKeys.USER_TIMEZONE, null);
    }

    @Test
    public void testGetUserTimeZoneInfo() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        Preferences preferences = mock(Preferences.class);
        when(i18nHelper.getLocale()).thenReturn(Locale.CHINA);
        when(userPreferencesManager.getPreferences(user)).thenReturn(preferences);
        when(preferences.getString(PreferenceKeys.USER_TIMEZONE)).thenReturn("Australia/Sydney");
        TimeZoneInfo userTimeZoneInfo = timeZoneService.getUserTimeZoneInfo(serviceContext);
        assertEquals("Australia/Sydney", userTimeZoneInfo.getTimeZoneId());
    }

    @Test
    public void testUsesJiraTimeZone() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        Preferences preferences = mock(Preferences.class);
        when(userPreferencesManager.getPreferences(user)).thenReturn(preferences);
        when(preferences.getString(PreferenceKeys.USER_TIMEZONE)).thenReturn(null);
        assertEquals(true, timeZoneService.usesJiraTimeZone(serviceContext));
    }

    @Test
    public void testDoesNotUseJiraTimeZone() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        Preferences preferences = mock(Preferences.class);
        when(userPreferencesManager.getPreferences(user)).thenReturn(preferences);
        when(preferences.getString(PreferenceKeys.USER_TIMEZONE)).thenReturn("Asia/Yakutsk");
        assertEquals(false, timeZoneService.usesJiraTimeZone(serviceContext));
    }

    @Test
    public void testSetUserDefaultTimeZone() throws Exception
    {
        User user = new MockUser("bob");
        JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user, new SimpleErrorCollection(), i18nHelper);
        Preferences preferences = mock(Preferences.class);
        when(userPreferencesManager.getPreferences(user)).thenReturn(preferences);
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Yakutsk");
        timeZoneService.setUserDefaultTimeZone(timeZone.getID(), serviceContext);
        verify(preferences).setString(PreferenceKeys.USER_TIMEZONE, timeZone.getID());
    }
}
