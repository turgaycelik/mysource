package com.atlassian.jira.memoryinspector;

import java.util.regex.Pattern;

import com.google.common.base.Predicate;

/**
* Provides predicates for ThreadInfo class.
*
* @since v6.3
*/
public class ThreadInfoPredicate implements Predicate<ThreadInfo>
{
    private static final Pattern ANY = Pattern.compile(".*");
    private final Pattern threadNamePattern;
    private final Pattern threadClassNamePattern;
    private final Pattern threadGroupPattern;

    public static Predicate<ThreadInfo> matchClass(final String threadClassNamePattern)
    {
        return new Predicate<ThreadInfo>()
        {
            @Override
            public boolean apply(final ThreadInfo threadInfo)
            {
                return Pattern.compile(threadClassNamePattern).matcher(threadInfo.getThreadClassName()).matches();
            }
        };
    }

    public static Predicate<ThreadInfo> matchName(final String threadNamePattern)
    {
        return new Predicate<ThreadInfo>()
        {
            @Override
            public boolean apply(final ThreadInfo threadInfo)
            {
                return Pattern.compile(threadNamePattern).matcher(threadInfo.getThreadName()).matches();
            }
        };
    }

    public static Predicate<ThreadInfo> matchGroup(final String threadGroupPattern) {
        return new Predicate<ThreadInfo>()
        {
            @Override
            public boolean apply(final ThreadInfo threadInfo)
            {
                return Pattern.compile(threadGroupPattern).matcher(threadInfo.getThreadGroupName()).matches();
            }
        };
    }

    public static Predicate<ThreadInfo> matchClassLoaderClassName(final String pattern) {
        return new Predicate<ThreadInfo>()
        {
            @Override
            public boolean apply(final ThreadInfo threadInfo)
            {
                return Pattern.compile(pattern).matcher(threadInfo.getClassLoaderClassName()).matches();
            }
        };
    }

    public static Predicate<ThreadInfo> matchClassAndName(final String threadClassNamePattern, final String threadNamePattern)
    {
        return new ThreadInfoPredicate(Pattern.compile(threadClassNamePattern), Pattern.compile(threadNamePattern), ANY);
    }

    public static Predicate<ThreadInfo> matchNameAndGroup(final String threadNamePattern, final String threadGroupPattern)
    {
        return new ThreadInfoPredicate(ANY, Pattern.compile(threadNamePattern), Pattern.compile(threadGroupPattern));
    }

    public static Predicate<ThreadInfo> matchClassAndNameAndGroup(final String threadClassNamePattern, final String threadNamePattern, final String threadGroupPattern)
    {
        return new ThreadInfoPredicate(Pattern.compile(threadClassNamePattern), Pattern.compile(threadNamePattern), Pattern.compile(threadGroupPattern));
    }

    public static Predicate<ThreadInfo> matchState(final Thread.State state)
    {
        return new Predicate<ThreadInfo>()
        {
            @Override
            public boolean apply(final ThreadInfo threadInfo)
            {
                return state.equals(threadInfo.getThread().getState());
            }
        };
    }

    public ThreadInfoPredicate(final Pattern threadClassNamePattern, final Pattern threadNamePattern, final Pattern threadGroupPattern)
    {
        this.threadNamePattern = threadNamePattern;
        this.threadClassNamePattern = threadClassNamePattern;
        this.threadGroupPattern = threadGroupPattern;
    }

    @Override
    public boolean apply(final ThreadInfo threadInfo)
    {
        return threadNamePattern.matcher(threadInfo.getThreadName()).matches()
                && threadClassNamePattern.matcher(threadInfo.getThreadClassName()).matches()
                && threadGroupPattern.matcher(threadInfo.getThreadGroupName()).matches();
    }
}
