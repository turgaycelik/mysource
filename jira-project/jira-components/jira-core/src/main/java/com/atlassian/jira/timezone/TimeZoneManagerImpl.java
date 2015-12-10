package com.atlassian.jira.timezone;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import org.apache.commons.lang.StringUtils;

import java.util.TimeZone;

/**
 * @since v4.4
 */
public class TimeZoneManagerImpl implements TimeZoneManager
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserPreferencesManager userPreferencesManager;
    private final ApplicationProperties applicationProperties;

    public TimeZoneManagerImpl(JiraAuthenticationContext jiraAuthenticationContext, UserPreferencesManager userPreferencesManager, ApplicationProperties applicationProperties)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userPreferencesManager = userPreferencesManager;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public TimeZone getLoggedInUserTimeZone()
    {
        return getTimeZoneforUser(jiraAuthenticationContext.getLoggedInUser());
    }

    @Override
    public TimeZone getTimeZoneforUser(User user)
    {
        Preferences preferences = userPreferencesManager.getPreferences(user);
        String timezoneId = (preferences != null) ? preferences.getString(PreferenceKeys.USER_TIMEZONE) : null;
        if (StringUtils.isNotEmpty(timezoneId))
        {
            return TimeZone.getTimeZone(timezoneId);
        }
        return getDefaultTimezone();
    }

    @Override
    public TimeZone getDefaultTimezone()
    {
        String systemDefaultTimeZoneId = applicationProperties.getString(APKeys.JIRA_DEFAULT_TIMEZONE);
        if (StringUtils.isNotEmpty(systemDefaultTimeZoneId))
        {
            return TimeZone.getTimeZone(systemDefaultTimeZoneId);

        }
        return TimeZone.getDefault();
    }
}
