package com.atlassian.sal.jira.timezone;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.timezone.TimeZoneService;

import java.util.TimeZone;

/**
 * @since v4.4
 */
public class JiraTimeZoneManager implements com.atlassian.sal.api.timezone.TimeZoneManager
{
    private final TimeZoneService timeZoneService;
    private final TimeZoneManager timeZoneManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public JiraTimeZoneManager(TimeZoneService timeZoneService, TimeZoneManager timeZoneManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.timeZoneService = timeZoneService;
        this.timeZoneManager = timeZoneManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public TimeZone getUserTimeZone()
    {
        return timeZoneManager.getLoggedInUserTimeZone();
    }

    @Override
    public TimeZone getDefaultTimeZone()
    {
        return timeZoneService.getDefaultTimeZoneInfo(new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser())).toTimeZone();
    }
}