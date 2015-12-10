package com.atlassian.jira.web.action.util;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.webresource.WebResourceManager;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.web.action.util.FieldsResourceIncluder}.
 *
 * @since v4.2
 */
public class FieldsResourceIncluderImpl implements FieldsResourceIncluder
{
    private final JiraAuthenticationContext authCtx;
    private final WebResourceManager webResourceManager;
    private final CalendarResourceIncluder calendarResourceIncluder;

    public FieldsResourceIncluderImpl(final JiraAuthenticationContext authCtx, final WebResourceManager webResourceManager,
            final CalendarResourceIncluder calendarResourceIncluder)
    {
        this.authCtx = notNull("authCtx", authCtx);
        this.webResourceManager = notNull("webResourceManager", webResourceManager);
        this.calendarResourceIncluder = notNull("calendarResourceIncluder", calendarResourceIncluder);
    }

    public void includeFieldResourcesForCurrentUser()
    {
        webResourceManager.requireResource("jira.webresources:jira-fields");
        //We don't use autCtx.getLocale() because it is more expensive than authCtx.getI18nHelper().getLocale()
        calendarResourceIncluder.includeForLocale(authCtx.getI18nHelper().getLocale());
    }
}
