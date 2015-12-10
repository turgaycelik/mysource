package com.atlassian.jira.config.webwork;

import webwork.action.Action;

/**
 * This is a stand alone class that exists only to help a unit test.  As you may have guessed its not very useful in its
 * own right!   It becomes a poisoned class because its static initialize block can not run
 */
public class ShesAManiac implements Action
{
    static
    {
        if (true)
        {
            throw new RuntimeException("And shes dancing like shes never danced before");
        }
    }

    public String execute() throws Exception
    {
        return "onTheFloor";
    }
}
