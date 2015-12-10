package com.atlassian.jira.web.monitor.dump;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

/**
 * Utility methods operating on ThreadInfo instances.
 *
 * @since v4.3
 */
public class ThreadInfos
{
    /**
     * Returns a string representation of the given ThreadInfo instance. This implementation is copied from {@link
     * java.lang.management.ThreadInfo#toString()}, with the difference that it does not set a limit on the maximum
     * number of stack frames that will be printed.
     *
     * @param thread a ThreadInfo
     * @return a String representation of the ThreadInfo
     * @see java.lang.management.ThreadInfo#toString()
     */
    public static String toString(ThreadInfo thread)
    {
        StringBuilder sb = new StringBuilder("\"" + thread.getThreadName() + "\"" +
                " Id=" + thread.getThreadId() + " " +
                thread.getThreadState());
        if (thread.getLockName() != null)
        {
            sb.append(" on " + thread.getLockName());
        }
        if (thread.getLockOwnerName() != null)
        {
            sb.append(" owned by \"" + thread.getLockOwnerName() +
                    "\" Id=" + thread.getLockOwnerId());
        }
        if (thread.isSuspended())
        {
            sb.append(" (suspended)");
        }
        if (thread.isInNative())
        {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        StackTraceElement[] stackTrace = thread.getStackTrace();
        for (; i < stackTrace.length; i++)
        {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && thread.getLockInfo() != null)
            {
                Thread.State ts = thread.getThreadState();
                switch (ts)
                {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + thread.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + thread.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + thread.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : thread.getLockedMonitors())
            {
                if (mi.getLockedStackDepth() == i)
                {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length)
        {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = thread.getLockedSynchronizers();
        if (locks.length > 0)
        {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks)
            {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    private ThreadInfos()
    {
    }
}
