package com.atlassian.jira.web.action.admin.user;

import com.atlassian.jira.security.auth.rememberme.JiraRememberMeTokenDao;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;

/**
 * A page to allow all rememember me cookies to be cleared
 *
 * @since v4.2
 */
@WebSudoRequired
public class AllUsersRememberMeCookies extends JiraWebActionSupport
{
    private final RememberMeTokenDao rememberMeTokenDao;
    private boolean cleared = false;

    public AllUsersRememberMeCookies(RememberMeTokenDao rememberMeTokenDao)
    {
        this.rememberMeTokenDao = rememberMeTokenDao;
    }

    @Override
    public String doDefault() throws Exception
    {
        return INPUT;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        rememberMeTokenDao.removeAll();
        cleared = true;
        return INPUT;
    }

    public String getTotalCountString()
    {
        //this is dodgy but the interface in seraph doesn't expose it and we can't
        //inject it or this action breaks during profiling and logging. SER-169
        final long l = ((JiraRememberMeTokenDao) rememberMeTokenDao).countAll();
        return String.format("%,d", l);
    }

    public boolean isCleared()
    {
        return cleared;
    }
}
