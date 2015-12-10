package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.plugin.myjirahome.MyJiraHomeLinker;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * This either goes home, or if home is Admin, then it goes to the dashboard
 *
 * @since v5.0
 */
public class LeaveAdmin extends JiraWebActionSupport
{
    static final String ADMIN_SUMMARY_LOCATION = "/secure/AdminSummary.jspa";

    private final MyJiraHomeLinker myJiraHomeLinker;

    public LeaveAdmin(MyJiraHomeLinker myJiraHomeLinker)
    {
        this.myJiraHomeLinker = myJiraHomeLinker;
    }

    @Override
    protected String doExecute() throws Exception
    {
        final String myHome = findHome();
        return redirectButNotToAdminSummary(myHome);
    }

    private String findHome()
    {
        return myJiraHomeLinker.getHomeLink(getLoggedInUser());
    }

    private String redirectButNotToAdminSummary(final String myHome)
    {
        if (ADMIN_SUMMARY_LOCATION.equalsIgnoreCase(myHome))
        {
            return getRedirect(MyJiraHomeLinker.DEFAULT_HOME_NOT_ANON);
        }
        return getRedirect(myHome);
    }

}
