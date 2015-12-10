package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.seraph.service.rememberme.RememberMeToken;
import com.atlassian.seraph.spi.rememberme.RememberMeTokenDao;

import java.util.Date;
import java.util.List;

/**
 * This action shows the remember me cookies that a user has
 *
 * @since v4.2
 */
public class UserRememberMeCookies extends ViewUser
{
    private final OutlookDateManager outlookDateManager;

    private final RememberMeTokenDao rememberMeTokenDao;
    private List<RememberMeToken> rememberMeTokenList;

    public UserRememberMeCookies(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, final UserPropertyManager userPropertyManager, final RememberMeTokenDao rememberMeTokenDao, final OutlookDateManager outlookDateManager, final UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.outlookDateManager = outlookDateManager;
        this.rememberMeTokenDao = rememberMeTokenDao;
    }

    @Override
    public String doDefault() throws Exception
    {
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        rememberMeTokenDao.removeAllForUser(name);
        return INPUT;
    }

    public List<RememberMeToken> getTokens()
    {
        if (rememberMeTokenList == null)
        {
            rememberMeTokenList = rememberMeTokenDao.findForUserName(name);
        }
        return rememberMeTokenList;
    }

    public String getFormattedDate(long time)
    {
        final OutlookDate outlookDate = outlookDateManager.getOutlookDate(getLocale());
        return outlookDate.format(new Date(time));
    }

}
