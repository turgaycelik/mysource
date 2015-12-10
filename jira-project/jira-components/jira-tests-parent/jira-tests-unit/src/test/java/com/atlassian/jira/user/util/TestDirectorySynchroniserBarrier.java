package com.atlassian.jira.user.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.event.api.EventPublisher;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.stub;

/**
 * @since v5.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDirectorySynchroniserBarrier
{
    @Mock CrowdDirectoryService crowdDirectoryService;
    @Mock EventPublisher eventPublisher;
    @Mock ApplicationFactory factory;
    @Mock Application application;

    @Before
    public void initFactory()
    {
        stub(factory.getApplication()).toReturn(application);
    }

    @Test
    public void testDuringSetup()
    {
        stub(factory.getApplication()).toReturn(null);
        DirectorySynchroniserBarrier barrier = new DirectorySynchroniserBarrier(factory, crowdDirectoryService, eventPublisher, Long.MAX_VALUE);
        assertTrue(barrier.await(10, TimeUnit.MINUTES));
    }

    @Test
    public void testNothingSynching()
    {
        Directory dir1 = createDirectory(1, true);

        stub(crowdDirectoryService.findAllDirectories()).toReturn(ImmutableList.of(dir1));
        DirectorySynchroniserBarrier barrier = new DirectorySynchroniserBarrier(factory, crowdDirectoryService, eventPublisher, Long.MAX_VALUE);
        assertTrue(barrier.await(10, TimeUnit.MINUTES));
    }

    @Test
    public void testOneSynchingAndTimeOut()
    {
        Directory dir1 = createDirectory(0, true);
        Directory dir2 = createDirectory(2, true);

        stub(crowdDirectoryService.findAllDirectories()).toReturn(ImmutableList.of(dir1, dir2));
        stub(crowdDirectoryService.isDirectorySynchronising(2)).toReturn(true);

        DirectorySynchroniserBarrier barrier = new DirectorySynchroniserBarrier(factory, crowdDirectoryService, eventPublisher, Long.MAX_VALUE);
        assertFalse(barrier.await(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testOneSynchingButFinishesBeforeWait()
    {
        Directory dir1 = createDirectory(0, true);
        Directory dir2 = createDirectory(2, true);

        stub(crowdDirectoryService.findAllDirectories()).toReturn(ImmutableList.of(dir1, dir2));
        stub(crowdDirectoryService.isDirectorySynchronising(2)).toReturn(true).toReturn(false);

        DirectorySynchroniserBarrier barrier = new DirectorySynchroniserBarrier(factory, crowdDirectoryService, eventPublisher, 1);
        assertTrue(barrier.await(1, TimeUnit.MINUTES));
    }

    @Test
    public void testOneSynchingAndInterruptExitsWorks() throws InterruptedException
    {
        Directory dir1 = createDirectory(0, true);
        Directory dir2 = createDirectory(2, true);

        stub(crowdDirectoryService.findAllDirectories()).toReturn(ImmutableList.of(dir1, dir2));
        stub(crowdDirectoryService.isDirectorySynchronising(2)).toReturn(true);

        final DirectorySynchroniserBarrier barrier = new DirectorySynchroniserBarrier(factory, crowdDirectoryService, eventPublisher, Long.MAX_VALUE);
        final AtomicBoolean result = new AtomicBoolean(false);
        Thread th = new Thread()
        {
            @Override
            public void run()
            {
                result.set(barrier.await(1, TimeUnit.MINUTES));
            }
        };
        th.start();
        th.interrupt();
        th.join();

        assertFalse(result.get());
        assertFalse(th.isInterrupted());
    }

    @Test
    public void testOneSynchingAndInterruptExitsWorksNoneRunning() throws InterruptedException
    {
        Directory dir1 = createDirectory(0, true);
        Directory dir2 = createDirectory(2, true);

        stub(crowdDirectoryService.findAllDirectories()).toReturn(ImmutableList.of(dir1, dir2));
        stub(crowdDirectoryService.isDirectorySynchronising(2)).toReturn(true).toReturn(false);

        final DirectorySynchroniserBarrier barrier = new DirectorySynchroniserBarrier(factory, crowdDirectoryService, eventPublisher, Long.MAX_VALUE);
        final AtomicBoolean result = new AtomicBoolean(false);
        Thread th = new Thread()
        {
            @Override
            public void run()
            {
                result.set(barrier.await(1, TimeUnit.MINUTES));
            }
        };
        th.start();
        th.interrupt();
        th.join();

        assertTrue(result.get());
        assertFalse(th.isInterrupted());
    }

    private static Directory createDirectory(long id, boolean active)
    {
        Directory mock = Mockito.mock(Directory.class);
        stub(mock.isActive()).toReturn(active);
        stub(mock.getId()).toReturn(id);

        return mock;
    }
}
