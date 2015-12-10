package com.atlassian.jira.memoryinspector;

import com.google.common.base.Objects;

import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * Holds thread and thread information.
 *
 * @since v6.3
 */
public class ThreadInfo
{
    public static final String SYSTEM_CLASS_LOADER_NAME = "<system>";
    public static final String UNKNOWN_CLASS_LOADER_NAME = "<?>";
    public static final String UNKNOWN_GROUP_NAME = "<?>";

    private final String threadClassName;
    private final String threadName;
    private final String threadGroupName;
    private final String classLoaderClassName;
    private final Thread thread;
    private final Thread.State state;

    public ThreadInfo(final Thread thread)
    {
        this.thread = thread;
        this.threadClassName = defaultString(thread.getClass().getCanonicalName());
        this.threadName = defaultString(thread.getName());
        final ThreadGroup threadGroup = thread.getThreadGroup();
        this.threadGroupName = defaultString(threadGroup == null ? UNKNOWN_GROUP_NAME : threadGroup.getName());
        final ClassLoader contextClassLoader = thread.getContextClassLoader();
        this.classLoaderClassName = defaultString(
                contextClassLoader == null ? UNKNOWN_CLASS_LOADER_NAME : contextClassLoader.getClass().getCanonicalName(),
                SYSTEM_CLASS_LOADER_NAME
        );
        this.state = thread.getState();
    }

    public ThreadInfo(final String threadClassName, final String threadName, final String threadGroupName,
            final String classLoaderClassName, final Thread thread)
    {
        this.threadClassName = threadClassName;
        this.threadName = threadName;
        this.threadGroupName = threadGroupName;
        this.classLoaderClassName = classLoaderClassName;
        this.thread = thread;
        this.state = null;
    }

    public String getThreadClassName()
    {
        return threadClassName;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public String getThreadGroupName()
    {
        return threadGroupName;
    }

    public Thread getThread()
    {
        return thread;
    }

    public String getClassLoaderClassName()
    {
        return classLoaderClassName;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(threadClassName, threadName, threadGroupName, classLoaderClassName, thread);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        final ThreadInfo other = (ThreadInfo) obj;
        return Objects.equal(this.threadClassName, other.threadClassName)
                && Objects.equal(this.threadName, other.threadName)
                && Objects.equal(this.threadGroupName, other.threadGroupName)
                && Objects.equal(this.classLoaderClassName, other.classLoaderClassName)
                && Objects.equal(this.thread, other.thread);
    }

    @Override
    public String toString()
    {
        return "ThreadInfo{" +
                "threadClassName='" + threadClassName + '\'' +
                ", threadName='" + threadName + '\'' +
                ", threadGroupName='" + threadGroupName + '\'' +
                ", classLoaderClassName='" + classLoaderClassName + '\'' +
                ", thread=" + thread +
                ", state=" + state +
                '}';
    }
}
