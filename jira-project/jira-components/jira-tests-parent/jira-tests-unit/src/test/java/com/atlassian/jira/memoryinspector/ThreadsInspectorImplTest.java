package com.atlassian.jira.memoryinspector;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.3
 */
public class ThreadsInspectorImplTest
{
    private static final String DEFAULT_CLASS_LOADER = "default";
    private static final String CATALINA_CLASS_LOADER = "org.apache.catalina.loader.WebappClassLoader";
    public static final String CATALINA6_CLASS_LOADER = "org.apache.catalina.loader.StandardClassLoader";
    private final Thread threadOne = new ThreadOneClass(new ThreadGroup("group-one"), "thread-one");
    private final Thread threadTwo = new ThreadTwoClass(new ThreadGroup("group-two"), "thread-one");
    private final ThreadInfo threadOneInfo = new ThreadInfo(threadOne);
    private final ThreadInfo threadTwoInfo = new ThreadInfo(threadTwo);

    @Test
    public void testThreadInspectionShouldPassWithEmptyThreadsAndEmptyKnownThreads() throws Exception
    {
        final Iterable<ThreadInfo> threadInfo = ImmutableList.of();
        final List<Predicate<ThreadInfo>> knownThreads = ImmutableList.of();
        final ThreadsInspectorImpl threadsInspector = new ThreadsInspectorImpl();

        final ThreadsInspectionReport report = threadsInspector.inspectThreads(threadInfo, knownThreads);

        assertThat(report.getUnexpectedThreads(), Matchers.<ThreadInfo>emptyIterable());
        assertTrue(report.inspectionPassed());
    }

    @Test
    public void testThreadInspectionShouldFailWhenThreadIsNotMatched() throws Exception
    {
        final List<ThreadInfo> threadInfo = ImmutableList.of(threadOneInfo);
        final List<Predicate<ThreadInfo>> knownThreads = ImmutableList.of(
                ThreadInfoPredicate.matchName("NOT-thread-one")
        );
        final ThreadsInspectorImpl threadsInspector = new ThreadsInspectorImpl();

        final ThreadsInspectionReport report = threadsInspector.inspectThreads(threadInfo, knownThreads);

        assertEquals(threadInfo, report.getUnexpectedThreads());
        assertFalse(report.inspectionPassed());
    }

    @Test
    public void testThreadInspectionShouldPassWhenThreadIsMatched() throws Exception
    {
        final List<ThreadInfo> threadInfo = ImmutableList.of(threadOneInfo);
        final List<Predicate<ThreadInfo>> knownThreads = ImmutableList.of(
                ThreadInfoPredicate.matchClassAndNameAndGroup(threadOneInfo.getThreadClassName(), threadOneInfo.getThreadName(), threadOneInfo.getThreadGroupName())
        );
        final ThreadsInspectorImpl threadsInspector = new ThreadsInspectorImpl();

        final ThreadsInspectionReport report = threadsInspector.inspectThreads(threadInfo, knownThreads);

        assertThat(report.getUnexpectedThreads(), Matchers.<ThreadInfo>emptyIterable());
        assertTrue(report.inspectionPassed());
    }


    @Test
    public void testThreadInspectionShouldFailWhenAtLeastOneThreadIsNotMatched() throws Exception
    {
        final List<ThreadInfo> threadInfo = ImmutableList.of(threadOneInfo, threadTwoInfo);
        final List<Predicate<ThreadInfo>> knownThreads = ImmutableList.of(
                ThreadInfoPredicate.matchClassAndNameAndGroup(threadOneInfo.getThreadClassName(), threadOneInfo.getThreadName(), threadOneInfo.getThreadGroupName())
        );
        final ThreadsInspectorImpl threadsInspector = new ThreadsInspectorImpl();

        final ThreadsInspectionReport report = threadsInspector.inspectThreads(threadInfo, knownThreads);

        assertThat(report.getUnexpectedThreads(), Matchers.hasItem(threadTwoInfo));
        assertFalse(report.inspectionPassed());
    }

    @Test
    public void testTransformThreadsToThreadInfo() throws Exception
    {
        final Iterable<Thread> threads = ImmutableList.of(threadOne, threadTwo);
        final ThreadsInspectorImpl threadsInspector = new ThreadsInspectorImpl();
        final Iterable<ThreadInfo> threadInfos = threadsInspector.transformThreadsToThreadInfo(threads);

        assertThat(threadInfos, Matchers.containsInAnyOrder(threadOneInfo, threadTwoInfo));
    }

    @Test
    public void inspectThreadInfosShouldAcceptAllKnownGoodThreads() throws Exception
    {
        final Thread mockThread = mock(Thread.class);
        final Thread terminatedThread = mock(Thread.class);
        when(terminatedThread.getState()).thenReturn(Thread.State.TERMINATED);

        final ImmutableList<ThreadInfo> threadsKnownAsGood = ImmutableList.<ThreadInfo>builder()
                .add(new ThreadInfo("java.lang.Thread", "JiraImportTaskExecutionThread-1", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "ContainerBackgroundProcessor[StandardEngine[Catalina]]", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.util.TimerThread", "Timer-0", "main", CATALINA_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "HSQLDB Timer @73a80183", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.objectweb.jonas_timer.Batch", "JonasBatch", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.objectweb.jonas_timer.Clock", "JonasClock", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "http-bio-8090-Acceptor-0", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "http-bio-8090-AsyncTimeout", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "ajp-bio-8009-Acceptor-0", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "ajp-bio-8009-AsyncTimeout", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "ConnectionKeeper:thread-1", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-5", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-20", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-12", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-1", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-16", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-18", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-23", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-8", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-25", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-9", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-19", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-14", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-24", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-22", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-11", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-21", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-13", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-4", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-7", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-10", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-2", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-3", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-17", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-6", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("org.apache.tomcat.util.threads.TaskThread", "http-bio-8090-exec-15", "main", DEFAULT_CLASS_LOADER, mockThread))
                // tomcat 6 {
                .add(new ThreadInfo("org.apache.tomcat.util.threads.ThreadWithAttributes", "TP-Processor1", "main", CATALINA6_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "TP-Monitor", "main", CATALINA6_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "http-8090-Acceptor-0", "main", CATALINA6_CLASS_LOADER, mockThread))
                // }
                .add(new ThreadInfo("java.lang.ref.Reference.ReferenceHandler", "Reference Handler", "system", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.ref.Finalizer.FinalizerThread", "Finalizer", "system", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "Java2D Disposer", "system", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "Signal Dispatcher", "system", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "GC Daemon", "system", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "main", "main", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "FelixStartLevel", "", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "FelixDispatchQueue", "", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "FelixPackageAdmin", "", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("java.lang.Thread", "FelixShutdown", "", DEFAULT_CLASS_LOADER, mockThread))
                .add(new ThreadInfo("com.atlassian.thread", "RandomTerminatedThread", "main", DEFAULT_CLASS_LOADER, terminatedThread))
                .build();

        final ThreadsInspectorImpl threadsInspector = new ThreadsInspectorImpl();
        final ThreadsInspectionReport report = threadsInspector.inspectThreadInfos(threadsKnownAsGood);

        assertThat(report.getUnexpectedThreads(), Matchers.<ThreadInfo>emptyIterable());
        assertTrue(report.inspectionPassed());
    }

    public static class ThreadOneClass extends Thread
    {
        public ThreadOneClass(final ThreadGroup group, final String name)
        {
            super(group, name);
        }
    }

    public static class ThreadTwoClass extends Thread
    {
        public ThreadTwoClass(final ThreadGroup group, final String name)
        {
            super(group, name);
        }
    }
}
