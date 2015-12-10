package com.atlassian.jira.my_home;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomePreference;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the current My JIRA Home (the plugin module key).
 */
public class MyJiraHomeSelectionProvider extends AbstractJiraContextProvider
{
    public static final String MY_JIRA_HOME_PROPERTY_KEY = "currentMyJiraHome";

    private final MyJiraHomePreference preference;

    public MyJiraHomeSelectionProvider(@Nonnull final MyJiraHomePreference preference)
    {
        this.preference = preference;
    }

    @Override
    public Map getContextMap(@Nullable final User user, @Nonnull final JiraHelper jiraHelper)
    {
        final String home = preference.findHome(user);
        return Collections.<String, Object>singletonMap(MY_JIRA_HOME_PROPERTY_KEY, home);
    }
}
