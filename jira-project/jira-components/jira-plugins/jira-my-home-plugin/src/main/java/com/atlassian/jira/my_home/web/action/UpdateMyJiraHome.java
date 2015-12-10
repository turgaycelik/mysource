package com.atlassian.jira.my_home.web.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.my_home.MyJiraHomeUpdateService;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Web action to update the My JIRA Home.
 */
public class UpdateMyJiraHome extends JiraWebActionSupport
{
    static final String MY_JIRA_HOME = "/secure/MyJiraHome.jspa";

    private final MyJiraHomeUpdateService myJiraHomeUpdateService;

    private String target;

    public UpdateMyJiraHome(@Nonnull final MyJiraHomeUpdateService myJiraHomeUpdateService)
    {
        this.myJiraHomeUpdateService = myJiraHomeUpdateService;
    }

    @RequiresXsrfCheck
    @Override
    protected String doExecute() throws Exception
    {
        final User authenticatedUser = getLoggedInUser();
        if (authenticatedUser != null)
        {
            updateMyHome(authenticatedUser);
        }

        return getRedirect(MY_JIRA_HOME);
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(@Nullable final String target)
    {
        this.target = target;
    }

    private void updateMyHome(@Nonnull final User user)
    {
        try
        {
            myJiraHomeUpdateService.updateHome(user, nullToEmpty(target));
        }
        catch (MyJiraHomeUpdateException e)
        {
            addErrorMessage(e.getMessage());
        }
    }

}
