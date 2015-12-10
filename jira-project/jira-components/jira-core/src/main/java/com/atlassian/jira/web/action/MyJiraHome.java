package com.atlassian.jira.web.action;

import com.atlassian.jira.plugin.myjirahome.MyJiraHomeLinker;

import javax.annotation.Nonnull;

/**
 * Redirects to the current My JIRA Home.
 *
 * @since 5.1
 */
public class MyJiraHome extends JiraWebActionSupport
{
    private MyJiraHomeLinker myJiraHomeLinker;

    public MyJiraHome(@Nonnull final MyJiraHomeLinker myJiraHomeLinker)
    {
        this.myJiraHomeLinker = myJiraHomeLinker;
    }

    @Override
    protected String doExecute() throws Exception
    {
        return getRedirect(findMyHome());
    }

    private String findMyHome()
    {
        return myJiraHomeLinker.getHomeLink(getLoggedInUser());
    }

}
