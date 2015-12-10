package com.atlassian.jira.dev.reference.plugin.actions;

import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * Sample Reference Action that prints out "Welcome to JIRA".
 * @since v4.3
 */
public class ReferenceAction extends JiraWebActionSupport
{
    @Override
    protected String doExecute() throws Exception
    {
        return "success";
    }

    public String getProductName()
    {
        return "JIRA";
    }
}
