package com.atlassian.jira.web.action.admin.issuetypes;

/**
 * This interface defines something than can be executed. This is used to allow various things to <em>stall</em> the execution
 * of the action until a certain time. For example, if some migration wizard needs to happen before this action can be
 * executed.
 */
public interface ExecutableAction
{
    /**
     * Run the action
     */
    void run();
}
