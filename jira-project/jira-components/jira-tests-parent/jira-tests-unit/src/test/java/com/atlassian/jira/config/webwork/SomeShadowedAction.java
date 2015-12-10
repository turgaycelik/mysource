package com.atlassian.jira.config.webwork;

import webwork.action.Action;

public class SomeShadowedAction implements Action
{
    public String execute()
    {
        return "someShadowedAction";
    }
}
