package com.atlassian.jira.timezone;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class TestTimeZoneManager extends MockControllerTestCase
{
    private TimeZoneService timeZoneManager;
    private ApplicationProperties applicationProperties;
    private PermissionManager permissionManager;
    private UserPreferencesManager userPreferenceManager;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = createMock(ApplicationProperties.class);
        permissionManager     = createMock(PermissionManager.class);
        userPreferenceManager = createMock(UserPreferencesManager.class);
        timeZoneManager =  new TimeZoneServiceImpl(applicationProperties, permissionManager, userPreferenceManager)
        {
            @Override
            protected TimeZone getJVMTimeZone()
            {
                return TimeZone.getTimeZone("Europe/Berlin");
            }
        };
    }

    @Test
    public void testGetJVMTimeZoneInfo() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        expect(i18nHelper.getLocale()).andReturn(new Locale("de", "DE"));
        expect(i18nHelper.getText("timezone.zone.europe.berlin")).andReturn("Berlin, Berlin!");
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        replay();
        TimeZoneInfo jvmTimeZoneInfo = timeZoneManager.getJVMTimeZoneInfo(jiraServiceContext);
        assertEquals("Europe", jvmTimeZoneInfo.getRegionKey());
        assertEquals("Berlin, Berlin!", jvmTimeZoneInfo.getCity());
        assertEquals("(GMT+01:00)", jvmTimeZoneInfo.getGMTOffset());
        verify();
    }

     @Test
    public void testGetDefaultTimeZoneInfo() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        expect(i18nHelper.getLocale()).andReturn(new Locale("en", "US"));
        expect(i18nHelper.getText("timezone.zone.europe.amsterdam")).andReturn("Off to Amsterdam!");
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        expect(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).andReturn("Europe/Amsterdam");
        replay();
        TimeZoneInfo timeZoneInfo = timeZoneManager.getDefaultTimeZoneInfo(jiraServiceContext);
        assertEquals("Europe", timeZoneInfo.getRegionKey());
        assertEquals("Off to Amsterdam!", timeZoneInfo.getCity());
        assertEquals("(GMT+01:00)", timeZoneInfo.getGMTOffset());
        verify();
    }

    @Test
    public void testGetTimeZoneRegions() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        String[] regions =  { "Etc", "Pacific", "America", "Antarctica", "Atlantic", "Africa", "Europe", "Asia", "Indian", "Australia" };
        for (String region : regions)
        {
           expect(i18nHelper.getText("timezone.region." + region.toLowerCase())).andReturn("GMT");
        }
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        replay();
        List<RegionInfo> timeZoneRegions = timeZoneManager.getTimeZoneRegions(jiraServiceContext);
        assertEquals(10, timeZoneRegions.size());
        verify();
    }

    @Test
    public void testUsesSystemTimeZone() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).andReturn(null);
        replay();
        assertEquals(true, timeZoneManager.useSystemTimeZone());
        verify();
    }

    @Test
    public void testUsesNotSystemTimeZone() throws Exception
    {
        expect(applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE)).andReturn("Europe/Berlin");
        replay();
        assertEquals(false, timeZoneManager.useSystemTimeZone());
        verify();
    }

    @Test
    public void testSetDefaultTimeZone() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);
        applicationProperties.setString(APKeys.JIRA_DEFAULT_TIMEZONE, "Europe/Berlin");
        replay();
        timeZoneManager.setDefaultTimeZone("Europe/Berlin", jiraServiceContext);
        verify();
    }

    @Test
    public void testSetDefaultTimeZoneNoPermission() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        replay();
        try
        {
            timeZoneManager.setDefaultTimeZone("Europe/Berlin", jiraServiceContext);
            fail("Should have thrown an exception due to a permission error.");
        }
        catch (Exception e)
        {
            assertEquals("This user does not have the JIRA Administrator permission. This permission is required to change the default timezone.", e.getMessage());
        }
        verify();
    }

    @Test
    public void testClearDefaultTimeZone() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(true);
        applicationProperties.setString(APKeys.JIRA_DEFAULT_TIMEZONE, null);
        replay();
        timeZoneManager.clearDefaultTimeZone(jiraServiceContext);
        verify();
    }

    @Test
    public void testClearDefaultTimeZoneNoAdminPermission() throws Exception
    {
        I18nHelper i18nHelper = createMock(I18nHelper.class);
        MockUser user = new MockUser("bob");
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(user, errorCollection, i18nHelper);
        expect(permissionManager.hasPermission(Permissions.ADMINISTER, user)).andReturn(false);
        replay();
        try
        {
            timeZoneManager.clearDefaultTimeZone(jiraServiceContext);
            fail("Should have thrown an exception due to a permission error.");
        }
        catch (Exception e)
        {
            assertEquals("This user does not have the JIRA Administrator permission. This permission is required to change the default timezone.", e.getMessage());
        }
        verify();
    }



}
