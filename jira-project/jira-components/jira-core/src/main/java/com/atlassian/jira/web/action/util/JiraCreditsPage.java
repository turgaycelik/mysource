package com.atlassian.jira.web.action.util;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.Action;

/**
 * Displays the JIRA credits page
 *
 * @since v4.3
 */
public class JiraCreditsPage extends JiraWebActionSupport
{
    private final String buildVersion;

    public JiraCreditsPage(final BuildUtilsInfo buildUtilsInfo)
    {
        buildVersion = buildUtilsInfo.getVersion();
    }

    protected String doExecute() throws Exception
    {
        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return super.doExecute();
    }

    public String doDefault() throws Exception
    {
        return Action.INPUT;
    }


    public String getBuildVersion()
    {
        return buildVersion;
    }
}
