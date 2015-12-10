package com.atlassian.jira.memoryinspector;

import java.util.List;

import org.apache.felix.framework.ModuleImpl;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

/**
 * Thread inspection report.
 *
 * @since v6.3
 */
public class ThreadsInspectionReport implements InspectionReport
{
    private final List<ThreadInfo> unexpectedThreads;

    public ThreadsInspectionReport(final List<ThreadInfo> unexpectedThreads)
    {
        this.unexpectedThreads = unexpectedThreads;
    }

    @Override
    public void printReport(final Logger log)
    {
        for (final ThreadInfo threadInfo : unexpectedThreads)
        {
            log.warn("Unexpected thread found, this probably will cause memory leaks and can result in OutOfMemoryError!");
            printThreadInfo(threadInfo, log);
        }
    }

    public List<ThreadInfo> getUnexpectedThreads()
    {
        return unexpectedThreads;
    }

    @Override
    public boolean inspectionPassed()
    {
        return unexpectedThreads.isEmpty();
    }

    private void printThreadInfo(final ThreadInfo threadInfo, final Logger log)
    {
        log.warn(String.format("********* Thread #%d %s [%s] *********", threadInfo.getThread().getId(), threadInfo.getThreadName(), threadInfo.getThreadGroupName()));
        log.warn(String.format(" threadInfo: %s", threadInfo));
        log.warn(String.format(" state: %s", threadInfo.getThread().getState()));
        log.warn(String.format(" priority: %d", threadInfo.getThread().getPriority()));
        log.warn(String.format(" class: %s", threadInfo.getThreadClassName()));
        final ClassLoader contextClassLoader = threadInfo.getThread().getContextClassLoader();
        log.warn(String.format(" contextClassLoader: %s", contextClassLoader));

        if (contextClassLoader instanceof ModuleImpl.ModuleClassLoader) {
            final Bundle bundle = ((ModuleImpl.ModuleClassLoader) contextClassLoader).getBundle();
            if (bundle != null) {
                log.warn(String.format(" contextClassLoader.bundle: %s", bundle));
            }
        }

        log.warn(String.format(""));
        log.warn(String.format("Thread stack trace:"));
        for (final StackTraceElement stackTraceElement : threadInfo.getThread().getStackTrace())
        {
            log.warn(String.format("\tat " + stackTraceElement));
        }
        log.warn(String.format("."));
    }
}
