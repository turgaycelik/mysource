package com.atlassian.sal.jira.usersettings;

import com.atlassian.fugue.Option;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.usersettings.UserSettings;
import com.atlassian.sal.api.usersettings.UserSettingsBuilder;
import com.atlassian.sal.api.usersettings.UserSettingsService;
import com.google.common.base.Function;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class UserSettingsServiceTest
{
    private static final String STRING_KEY = "string-key";
    private static final String BOOLEAN_KEY = "boolean-key";
    private static final String LONG_KEY = "long-key";
    private static final String STRING_VALUE = "string-value";
    private static final String TESTUSER = "testuser";
    private JiraUserSettingsService userSettingsService;

    @Mock
    private UserPropertyManager userPropertyManager;

    private MemoryPropertySet propertySet;
    @Mock
    private UserManager userManager;

    @Before
    public void setup()
    {
        userSettingsService = new JiraUserSettingsService(userPropertyManager, userManager);
        propertySet = new MemoryPropertySet();
        propertySet.init(null, null); // PropertySets are awesome!!
        MockApplicationUser testuser = new MockApplicationUser(TESTUSER);

        when(userManager.getUserByKey(TESTUSER)).thenReturn(testuser);
        when(userManager.getUserByName(TESTUSER)).thenReturn(testuser);
        when(userPropertyManager.getPropertySet(testuser)).thenReturn(propertySet);
    }

    @Test
    public void testGetUserSettings() throws Exception
    {
        // Set values for the PropertySet
        propertySet.setString(UserSettingsService.USER_SETTINGS_PREFIX + STRING_KEY, STRING_VALUE);
        propertySet.setBoolean(UserSettingsService.USER_SETTINGS_PREFIX + BOOLEAN_KEY, true);
        propertySet.setLong(UserSettingsService.USER_SETTINGS_PREFIX + LONG_KEY, 1L);

        UserSettings userSettings = userSettingsService.getUserSettings(TESTUSER);

        assertThat(userSettings.getKeys(), hasItems(STRING_KEY, BOOLEAN_KEY, LONG_KEY));

        Option<Long> longValue = userSettings.getLong(LONG_KEY);
        assertEquals(1L, (long) longValue.get());

        Option<String> stringValue = userSettings.getString(STRING_KEY);
        assertEquals(STRING_VALUE, stringValue.get());

        Option<Boolean> booleanValue = userSettings.getBoolean(BOOLEAN_KEY);
        assertTrue(booleanValue.get());
    }

    @Test
    public void testUpdateUserSettings() throws Exception
    {
        // Apply the update via the service to the underlying propertyset
        userSettingsService.updateUserSettings(TESTUSER, new Function<UserSettingsBuilder, UserSettings>()
        {
            @Override
            public UserSettings apply(UserSettingsBuilder input)
            {
                input.put(STRING_KEY, STRING_VALUE);
                return input.build();
            }
        });

        // Create a new representation of the UserSettings via the Builder to validate the update
        UserSettings userSettings = new JiraUserSettingsService.JiraPropertySetUserSettingsBuilder(propertySet).build();

        assertThat(userSettings.getKeys(), hasItems(STRING_KEY));

        Option<String> stringValue = userSettings.getString(STRING_KEY);
        assertEquals(STRING_VALUE, stringValue.get());
    }
}
