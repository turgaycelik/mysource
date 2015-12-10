package com.atlassian.jira.config.webwork;

/**
 * Thrown when a WebWork action was not found.
 *
 * @since v5.0
 */
public class ActionNotFoundException extends Exception
{
    private final String actionName;

    public ActionNotFoundException(String actionName)
    {
        this.actionName = actionName;
    }

    /**
     * @return the name of the action that was not found
     */
    public String getActionName()
    {
        return actionName;
    }
}
