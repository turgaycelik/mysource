package com.atlassian.jira.web.monitor.dump;

import java.lang.management.ThreadInfo;

/**
 * Strategy interface for thread dump writers.
 */
public interface ThreadInfoWriter
{
    /**
     * Writes the given ThreadInfo[] somewhere.
     *
     * @param threads a ThreadInfo[]
     */
    public void write(ThreadInfo[] threads);
}
