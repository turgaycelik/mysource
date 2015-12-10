package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;

/**
 * Clears the remember me cookies for the current user
 *
 * @since v4.2
 */
public class ClearRememberMeCookies extends JiraWebActionSupport
{
    private final RememberMeTokenDao rememberMeTokenDao;

    public ClearRememberMeCookies(final RememberMeTokenDao rememberMeTokenDao)
    {
        this.rememberMeTokenDao = rememberMeTokenDao;
    }

    public String doDefault() throws Exception
    {
        final User current = getLoggedInUser();

        if (current == null)
        {
            return ERROR;
        }

        return super.doDefault();
    }

    protected void doValidation()
    {
        try
        {
            getLoggedInUser();
        }
        catch (Exception e)
        {
            addErrorMessage(getText("rememberme.could.not.find.user"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final User current = getLoggedInUser();
        if (current == null)
        {
            return ERROR;
        }
        if (invalidInput())
        {
            return ERROR;
        }
        rememberMeTokenDao.removeAllForUser(current.getName());
        return returnComplete();
    }

    public String doSuccess()
    {
        return SUCCESS;
    }


}
