package com.atlassian.jira.webtests.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs any passed runnable only once.
 * 
 * @since v5.0.1
 */
public class RunOnce
{
    private final AtomicBoolean run = new AtomicBoolean(false);
    
    public RunOnce()
    {
    }
    
    public void run(Runnable runnable)
    {
        if (run.compareAndSet(false, true))
        {
            runnable.run();
        }
    }
}
