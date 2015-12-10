package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * Control for manipulating server-side barriers.
 *
 * @since v5.2
 */
public class BarrierControl extends BackdoorControl<BarrierControl>
{
    public BarrierControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Raises the barrier called {@code barrierName} and calls the given {@code Runnable} before lowering the barrier
     * again. This is useful to test for race conditions in production code.
     *
     * @param barrierName a String containing the barrier name
     * @param r a Runnable
     */
    public void raiseBarrierAndRun(String barrierName, Runnable r)
    {
        raise(barrierName);
        try
        {
            r.run();
        }
        finally
        {
            lower(barrierName);
        }
    }

    private void raise(String barrierName)
    {
        createResource().path("barrier").path("raise").queryParam("barrierName", barrierName).post();
    }

    private void lower(String barrierName)
    {
        createResource().path("barrier").path("lower").queryParam("barrierName", barrierName).post();
    }
}
