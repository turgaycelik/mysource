package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;

/**
 * @since v6.0
 */
public class NoAlertControl extends BackdoorControl<NoAlertControl>
{
    public NoAlertControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public NoAlertControl set(boolean noAlertMode)
    {
        createResource().path("noalert").put(String.valueOf(noAlertMode));
        return this;
    }

    public NoAlertControl enable()
    {
        return set(true);
    }

    public NoAlertControl disable()
    {
        return set(false);
    }
}
