package com.atlassian.jira.memoryinspector;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.log4j.Logger;

import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchClass;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchClassAndName;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchClassAndNameAndGroup;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchClassLoaderClassName;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchGroup;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchName;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchNameAndGroup;
import static com.atlassian.jira.memoryinspector.ThreadInfoPredicate.matchState;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * This class provides memory inspection that search for threads that should not be present in memory.
 *
 * @since v6.3
 */
public class ThreadsInspectorImpl implements ThreadsInspector
{

    private static final Logger log = Logger.getLogger(ThreadsInspectorImpl.class);
    public static final Function<Thread, ThreadInfo> THREAD_TO_THREAD_INFO_FUNCTION = new Function<Thread, ThreadInfo>()
    {

        @Override
        public ThreadInfo apply(final Thread thread)
        {
            try
            {
                return new ThreadInfo(thread);
            }
            catch (final Exception e)
            {
                log.warn("Got exception while getting thread info for thread: " + thread, e);
                return null;
            }
        }
    };

    @Override
    public InspectionReport inspectThreads(final Iterable<Thread> threads)
    {
        return inspectThreadInfos(transformThreadsToThreadInfo(threads));
    }

    ThreadsInspectionReport inspectThreadInfos(final Iterable<ThreadInfo> threadInfos)
    {
        return inspectThreads(threadInfos, this.knownBaseThreads);
    }

    final Iterable<ThreadInfo> transformThreadsToThreadInfo(final Iterable<Thread> threads)
    {
        return ImmutableList.copyOf(filter(transform(threads, THREAD_TO_THREAD_INFO_FUNCTION), notNull()));
    }

    ThreadsInspectionReport inspectThreads(final Iterable<ThreadInfo> threadInfo, final List<Predicate<ThreadInfo>> knownThreads)
    {
        return new ThreadsInspectionReport(ImmutableList.copyOf(Iterables.filter(threadInfo, not(or(knownThreads)))));
    }

    /**
     * This method is here only to avoid awful casting, as java generics are broken.
     */
    private Predicate<ThreadInfo> and(final Predicate<ThreadInfo>... predicates) {
        return Predicates.and(predicates);
    }

    @SuppressWarnings ("unchecked")
    private final List<Predicate<ThreadInfo>> knownBaseThreads = ImmutableList.<Predicate<ThreadInfo>>builder()
            // ofbiz uses the org.apache.commons.pool.impl.EvictionTimer which creates new Timer
            .add(and(
                    matchClass("\\Qjava.util.TimerThread\\E"), matchGroup("main"), matchName("Timer-\\d+"),
                    matchClassLoaderClassName("\\Qorg.apache.catalina.loader.WebappClassLoader\\E")
            ))
                    // tomcat / catalina
            .add(matchNameAndGroup("(http|ajp)(-bio)?-\\d+-Acceptor-\\d+", "main"))
            .add(matchNameAndGroup("(http|ajp)(-bio)?-\\d+-AsyncTimeout", "main"))
            .add(matchNameAndGroup("ContainerBackgroundProcessor\\[StandardEngine\\[Catalina\\]\\]", "main"))
            .add(matchClassAndNameAndGroup("\\Qorg.apache.tomcat.util.threads.TaskThread\\E", "(http|ajp)-bio-\\d+-exec-\\d+", "main"))
            .add(new TomcatNioThreadPoolExecutorMatcher()) // visible in java7
                    // tomcat 6
            .add(and(
                    matchClassAndNameAndGroup("\\Qjava.lang.Thread\\E", "TP-Monitor", "main"),
                    matchClassLoaderClassName("\\Qorg.apache.catalina.loader.StandardClassLoader\\E")
            ))
            .add(and(
                    matchClassAndNameAndGroup("\\Qorg.apache.tomcat.util.threads.ThreadWithAttributes\\E", "TP-Processor\\d+", "main"),
                    matchClassLoaderClassName("\\Qorg.apache.catalina.loader.StandardClassLoader\\E")
            ))
                    // JTA, Hibernate, Transactions...
            .add(matchClassAndName("\\Qorg.objectweb.jonas_timer.Clock\\E", "JonasClock"))
            .add(matchClassAndName("\\Qorg.objectweb.jonas_timer.Batch\\E", "JonasBatch"))
                    // java
            .add(matchNameAndGroup("Finalizer", "system"))
            .add(matchNameAndGroup("Signal Dispatcher", "system"))
            .add(matchNameAndGroup("Reference Handler", "system"))
            .add(matchNameAndGroup("GC Daemon", "system"))
                    // I can only guess that this is due to image generation, it's fine
            .add(matchNameAndGroup("Java2D Disposer", "system"))
                    // hsql
            .add(matchNameAndGroup("HSQLDB Timer .*", "main"))
            .add(matchNameAndGroup("ConnectionKeeper:thread-1", "main")) // see com.atlassian.jira.upgrade.ConnectionKeeper
                    // main
            .add(matchNameAndGroup("main", "main"))
                    // felix
            .add(matchClassAndName("\\Qjava.lang.Thread\\E", "Felix(StartLevel|DispatchQueue|PackageAdmin|Shutdown)"))
                    // terminated threads
            .add(matchState(Thread.State.TERMINATED))
                    // RMI, JMX
            .add(matchGroup("RMI Runtime"))
            .add(matchNameAndGroup("RMI TCP Accept-\\d", "system"))
            .add(matchName("JMX server connection timeout.*"))
            .add(matchNameAndGroup("RMI Scheduler.*", "system"))
            .add(matchNameAndGroup("Attach Listener", "system"))
                    // jira import task
            .add(matchNameAndGroup("JiraImportTaskExecutionThread-\\d+", "main"))
                    // IDEA ctrl+break monitor - when running debug
            .add(matchNameAndGroup("Monitor Ctrl-Break", "main"))
            .build();

    /**
     * This predicate looks for NIO EPoll that was created by ThreadPoolExecutor. Shame on java that this thread is
     * identified as 'Thread-\d'.
     */
    public static class TomcatNioThreadPoolExecutorMatcher implements Predicate<ThreadInfo>
    {
        @Override
        public boolean apply(final ThreadInfo threadInfo)
        {
            if (matchClassAndNameAndGroup("\\Qjava.lang.Thread\\E", "Thread-\\d+", "main").apply(threadInfo))
            {
                // inspect thread.target.this$0.pool
                try
                {
                    final Thread thread = threadInfo.getThread();
                    // thread.target
                    final Object threadTarget = getFieldValueWithClassCheck(thread, "target", "sun.nio.ch.EPollPort.EventHandlerTask");
                    // target.this$0
                    final Object outerThis = getFieldValueWithClassCheck(threadTarget, "this$0", "sun.nio.ch.EPollPort");
                    // this$0.pool
                    final Object pool = getFieldValueWithClassCheck(outerThis, "pool", "sun.nio.ch.ThreadPool");
                    // pool.poolExecutor
                    final Object poolExecutor = getFieldValueWithClassCheck(pool, "executor", "org.apache.tomcat.util.threads.ThreadPoolExecutor");
                    return poolExecutor != null;
                }
                catch (final Exception e)
                {
                    // ignore: this is not the thread we were looking for
                }
            }
            return false;
        }

        @Nullable
        private Object getFieldValueWithClassCheck(@Nullable final Object object, final String fieldName, final String expectedClassName)
                throws NoSuchFieldException, IllegalAccessException
        {
            if (object == null)
            {
                return null;
            }

            final Field field = findField(object.getClass(), fieldName);
            if (field == null)
            {
                return null;
            }
            field.setAccessible(true);
            final Object value = field.get(object);
            return value != null && expectedClassName.equals(value.getClass().getCanonicalName()) ? value : null;
        }


        @Nullable
        private Field findField(@Nullable final Class clazz, final String fieldName)
        {
            if (clazz == null)
            {
                return null;
            }

            for (final Field field : clazz.getDeclaredFields())
            {
                if (fieldName.equals(field.getName()))
                {
                    return field;
                }
            }

            return findField(clazz.getSuperclass(), fieldName);
        }
    }
}
