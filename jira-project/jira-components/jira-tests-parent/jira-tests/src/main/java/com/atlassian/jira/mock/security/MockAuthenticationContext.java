package com.atlassian.jira.mock.security;

import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.util.OutlookDate;

public class MockAuthenticationContext implements JiraAuthenticationContext
{
    private final I18nHelper i18nHelper;
    private ApplicationUser user;

    public MockAuthenticationContext(User user)
    {
        if (user != null)
            this.user = new DelegatingApplicationUser(user.getName().toLowerCase(), user);
        this.i18nHelper = new MockI18nBean();
    }

    public MockAuthenticationContext(User user, final I18nHelper i18nHelper)
    {
        if (user != null)
            this.user = new DelegatingApplicationUser(user.getName().toLowerCase(), user);
        if (i18nHelper == null)
            this.i18nHelper = new MockI18nBean();
        else
            this.i18nHelper = i18nHelper;
    }

    @Override
    public ApplicationUser getUser()
    {
        return user;
    }

    @Override
    public User getLoggedInUser()
    {
        return ApplicationUsers.toDirectoryUser(user);
    }

    @Override
    public boolean isLoggedInUser()
    {
        return user != null;
    }

    @Override
    public Locale getLocale()
    {
        return i18nHelper.getLocale();
    }

    @Override
    public OutlookDate getOutlookDate()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getText(final String key)
    {
        return getI18nHelper().getText(key);
    }

    @Override
    public I18nHelper getI18nHelper()
    {
        return i18nHelper;
    }

    @Override
    public I18nHelper getI18nBean()
    {
        return getI18nHelper();
    }

    @Override
    public void setLoggedInUser(final User user)
    {
        setLoggedInUser(ApplicationUsers.from(user));
    }

    @Override
    public void setLoggedInUser(final ApplicationUser user)
    {
        this.user = user;
    }

    @Override
    public void clearLoggedInUser()
    {
        this.user = null;
    }
}
