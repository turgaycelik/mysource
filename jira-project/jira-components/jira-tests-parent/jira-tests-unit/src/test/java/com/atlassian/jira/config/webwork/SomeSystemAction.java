package com.atlassian.jira.config.webwork;

import webwork.action.Action;

public class SomeSystemAction implements Action
{
    public String execute()
    {
        return "someSystemAction";
    }
}
