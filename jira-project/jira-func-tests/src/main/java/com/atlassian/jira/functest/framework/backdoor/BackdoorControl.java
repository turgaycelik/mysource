package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @since v5.2
 */
public abstract class BackdoorControl<T extends BackdoorControl<T>> extends com.atlassian.jira.testkit.client.BackdoorControl<T>
{
    public BackdoorControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    @Override
    protected WebResource createResource()
    {
        return resourceRoot(rootPath).path("rest").path("func-test").path("1.0");
    }
}
