package com.atlassian.jira.functest.framework;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestListener;
import junit.framework.TestSuite;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.capture;
import static org.easymock.classextension.EasyMock.createControl;

public class TestJiraTestWatchDog
{
    @Test
    public void testOutOfOrder()
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);

        TestSuite test1 = new TestSuite("brenden");

        JiraTestWatchDog dog = new JiraTestWatchDog(Predicates.<WebTestDescription>alwaysTrue(), 1000, 2000,
                TimeUnit.DAYS, 4, new FailCallable(), service);
        TestListener junit3Dog = new JUnit3WebTestListener(dog);

        iMocksControl.replay();

        junit3Dog.endTest(test1);

        iMocksControl.verify();
    }

    @Test
    public void testClose()
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);

        JiraTestWatchDog dog = new JiraTestWatchDog(Predicates.<WebTestDescription>alwaysTrue(), 1000, 2000,
                TimeUnit.DAYS, 4, new FailCallable(), service);

        dog.close();

        iMocksControl.replay();
        dog.close();
        iMocksControl.verify();
    }

    @Test
    public void testCloseWithTests()
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);
        @SuppressWarnings ( { "unchecked" }) Predicate<Object> predicate = iMocksControl.createMock(Predicate.class);
        @SuppressWarnings ( { "unchecked" }) ScheduledFuture<?> future = iMocksControl.createMock(ScheduledFuture.class);
        @SuppressWarnings ( { "unchecked" }) Function<WebTestDescription, ?> callable = iMocksControl.createMock(Function.class);

        TestSuite test1 = new TestSuite("brenden");

        long timeout = 1000L;
        long delay = 2000L;

        JiraTestWatchDog dog = new JiraTestWatchDog(Predicates.<WebTestDescription>alwaysTrue(), timeout, delay,
                TimeUnit.DAYS, 4, callable, service);
        TestListener junit3Dog = new JUnit3WebTestListener(dog);

        EasyMock.expect(predicate.apply(test1)).andReturn(true).anyTimes();
        EasyMock.expect(service.isShutdown()).andReturn(false).anyTimes();

        service.scheduleWithFixedDelay(EasyMock.<Runnable>notNull(),
                EasyMock.eq(timeout), EasyMock.eq(delay), EasyMock.same(TimeUnit.DAYS));
        EasyMock.expectLastCall().andReturn(future);
        
        EasyMock.expect(future.cancel(true)).andReturn(true);

        dog.close();

        iMocksControl.replay();

        junit3Dog.startTest(test1);
        dog.close();

        iMocksControl.verify();
    }

    @Test
    public void testFailedPredicate()
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);
        @SuppressWarnings ( { "unchecked" }) Predicate<WebTestDescription> predicate = iMocksControl.createMock(Predicate.class);

        TestSuite test1 = new TestSuite("brenden");
        JUnit3WebTestDescription testDescription = new JUnit3WebTestDescription(test1);

        JiraTestWatchDog dog = new JiraTestWatchDog(predicate, 1000, 2000, TimeUnit.DAYS, 4, new FailCallable(), service);

        EasyMock.expect(predicate.apply(testDescription)).andReturn(false);

        iMocksControl.replay();

        dog.testStarted(testDescription);
        dog.testError(testDescription, new Throwable());
        dog.testFailure(testDescription, new AssertionFailedError());
        dog.testFailure(testDescription, new AssertionFailedError());

        iMocksControl.verify();
    }

    @Test
    public void testAlreadyShutdown()
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);
        @SuppressWarnings ( { "unchecked" }) Predicate<WebTestDescription> predicate = iMocksControl.createMock(Predicate.class);

        TestSuite test1 = new TestSuite("brenden");
        JUnit3WebTestDescription testDescription = new JUnit3WebTestDescription(test1);

        JiraTestWatchDog dog = new JiraTestWatchDog(predicate, 1000, 2000, TimeUnit.DAYS, 4, new FailCallable(), service);

        EasyMock.expect(predicate.apply(testDescription)).andReturn(true);
        EasyMock.expect(service.isShutdown()).andReturn(true);


        iMocksControl.replay();

        dog.testStarted(testDescription);
        dog.testError(testDescription, new Throwable());
        dog.testFailure(testDescription, new AssertionFailedError());
        dog.testFailure(testDescription, new AssertionFailedError());

        iMocksControl.verify();
    }
    
    @Test
    public void testQuickTest()
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);
        @SuppressWarnings ( { "unchecked" }) Predicate<WebTestDescription> predicate = iMocksControl.createMock(Predicate.class);
        @SuppressWarnings ( { "unchecked" }) ScheduledFuture<?> future = iMocksControl.createMock(ScheduledFuture.class);
        @SuppressWarnings ( { "unchecked" }) ScheduledFuture<?> future2 = iMocksControl.createMock(ScheduledFuture.class);

        WebTestDescription test1 = new JUnit3WebTestDescription(new TestSuite("brenden"));
        WebTestDescription test2 = new JUnit3WebTestDescription(new TestSuite("brenden"));

        long timeout = 1000L;
        long repeatDelay = 2000L;

        JiraTestWatchDog dog = new JiraTestWatchDog(predicate, timeout, repeatDelay, TimeUnit.DAYS, 4, new FailCallable(), service);

        EasyMock.expect(predicate.apply(test1)).andReturn(true);
        EasyMock.expect(predicate.apply(test2)).andReturn(true);
        EasyMock.expect(service.isShutdown()).andReturn(false).anyTimes();

        service.scheduleWithFixedDelay(EasyMock.<Runnable>notNull(),
                EasyMock.eq(timeout), EasyMock.eq(repeatDelay), EasyMock.eq(TimeUnit.DAYS));
        EasyMock.expectLastCall().andReturn(future).andReturn(future2);

        EasyMock.expect(future.cancel(true)).andReturn(false);
        EasyMock.expect(future2.cancel(true)).andReturn(false);

        iMocksControl.replay();

        dog.testStarted(test1);
        dog.testStarted(test2);
        dog.testFinished(test1);
        dog.testFinished(test1);
        dog.testFinished(test2);
        dog.testFinished(test2);

        iMocksControl.verify();
    }

    @Test
    public void testSlowTest() throws Exception
    {
        IMocksControl iMocksControl = createControl();
        ScheduledExecutorService service = iMocksControl.createMock(ScheduledExecutorService.class);
        @SuppressWarnings ( { "unchecked" }) Predicate<WebTestDescription> predicate = iMocksControl.createMock(Predicate.class);
        @SuppressWarnings ( { "unchecked" }) ScheduledFuture<?> future = iMocksControl.createMock(ScheduledFuture.class);
        @SuppressWarnings ( { "unchecked" }) ScheduledFuture<?> future2 = iMocksControl.createMock(ScheduledFuture.class);
        @SuppressWarnings ( { "unchecked" }) Function<WebTestDescription, ?> callable = iMocksControl.createMock(Function.class);

        WebTestDescription test1 = new JUnit3WebTestDescription(new TestSuite("brenden"));
        WebTestDescription slowTest = new JUnit3WebTestDescription(new TestSuite("brenden"));

        long timeout = 1000L;
        long repeatDelay = 2000L;

        JiraTestWatchDog dog = new JiraTestWatchDog(predicate, timeout, repeatDelay, TimeUnit.DAYS, 2, callable, service);

        EasyMock.expect(predicate.apply(test1)).andReturn(true).anyTimes();
        EasyMock.expect(predicate.apply(slowTest)).andReturn(true).anyTimes();
        EasyMock.expect(service.isShutdown()).andReturn(false).anyTimes();

        Capture<Runnable> capture = new Capture<Runnable>();

        service.scheduleWithFixedDelay(capture(capture), EasyMock.eq(timeout), EasyMock.eq(repeatDelay),
                EasyMock.eq(TimeUnit.DAYS));
        EasyMock.expectLastCall().andReturn(future).andReturn(future2);

        EasyMock.expect(future.cancel(true)).andReturn(false);
        EasyMock.expect(callable.apply(slowTest)).andReturn(false).times(2);
        EasyMock.expect(future2.cancel(true)).andReturn(true);

        iMocksControl.replay();

        dog.testStarted(test1);
        dog.testFinished(test1);

        dog.testStarted(slowTest);
        capture.getValue().run();

        //This will actually cancel the running of the task.
        capture.getValue().run();
        dog.testError(slowTest, new Throwable());

        iMocksControl.verify();
    }

    private static class FailCallable implements Function<WebTestDescription, Void>
    {
        @Override
        public Void apply(WebTestDescription args)
        {
            Assert.fail("Should not call timeout.");
            return null;
        }
    }
}
