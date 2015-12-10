package com.atlassian.jira.dev.reference.plugin.actions;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.PrepareAction;

/**
 * Sample Reference Action that prints out "Welcome to JIRA".
 * @since v4.3
 */
public class PreparedReferenceAction extends JiraWebActionSupport implements PrepareAction
{
    private String preparedMessage = "Not Prepared";

    @Override
    protected String doExecute() throws Exception
    {
        return "success";
    }

    public String getProductName()
    {
        return "JIRA";
    }

    public void prepare() throws Exception
    {
        preparedMessage = "I am ready for anything";
    }

    public String getPreparedMessage()
    {
        return preparedMessage;
    }
}
