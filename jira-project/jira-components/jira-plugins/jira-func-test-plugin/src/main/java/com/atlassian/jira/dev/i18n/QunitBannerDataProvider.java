package com.atlassian.jira.dev.i18n;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.json.marshal.Jsonable;
import com.atlassian.json.marshal.wrapped.JsonableBoolean;
import com.atlassian.webresource.api.data.WebResourceDataProvider;

public class QunitBannerDataProvider implements WebResourceDataProvider
{
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public QunitBannerDataProvider(final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public Jsonable get()
    {
        boolean isMoonLocale = QunitLocaleSwitcher.EN_MOON.equals(jiraAuthenticationContext.getLocale());
        return new JsonableBoolean(isMoonLocale);
    }
}
