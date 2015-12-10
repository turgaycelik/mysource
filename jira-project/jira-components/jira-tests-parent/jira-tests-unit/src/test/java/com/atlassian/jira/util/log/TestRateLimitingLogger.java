package com.atlassian.jira.util.log;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.util.ConstantClock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test RateLimiting Logger
 *
 * All these test follow the same pattern.
 * <ul>
 *     <li>Call the logger at a particular level 6 times</li>
 *     <li>Reset the timestamp</li>
 *     <li>Call the logger at a particular level 6 times</li>
 * </ul>
 * <p>The rate limiter should kick in after 3 stacktraces for warn and error</p>
 * <p>4 Extra warn messages are written when the logger cuts over</p>
 *
 * @since v6.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestRateLimitingLogger
{
    @Mock private Logger mockDelegate;
    private RateLimitingLogger logger;

    private final Clock mockClock = new ConstantClock(12345678L);

    @Before
    public void setup()
    {
        logger = new RateLimitingLogger(mockDelegate, 3, 10, mockClock);
        RateLimitingLogger.reset();
    }

    /**
     * Test for the tests. :-)  This reproduced test flakiness since a static in RateLimitingLogger wasn't being reset before the tests
     * were run, causing a failure.  Without the <code>reset()</code> call in test setup, this test would fail.
     */
    @Test
    public void testToValidateResetIsNeededBeforeTests()
    throws Exception
    {
        //This first part pretends it is another test running in the VM before the real RateLimitingLogger tests
        //that uses RateLimitingLogger as part of normal execution

        //Don't affect the method counts on the mock for the other tests
        Logger anotherMockLogger = mock(Logger.class);

        RateLimitingLogger other = new RateLimitingLogger(anotherMockLogger, 3, 10, mockClock);
        other.error("Good morning", new Exception());


        //Now pretend we are JUnit running the real tests, including running the @Before s again followed by the
        //test that was flaking out
        setup();
        testErrorWithException();
    }

    @Test
    public void testTrace() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.trace("Message");
            }
        });
        verify(mockDelegate, new Times(12)).trace(any(String.class));
    }

    @Test
    public void testTraceWithException() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.trace("Message", new Exception());
            }
        });
        verify(mockDelegate, new Times(12)).trace(any(String.class), any(Exception.class));
    }

    @Test
    public void testIsTraceEnabled() throws Exception
    {
        when(mockDelegate.isTraceEnabled()).thenReturn(true);
        assertThat(logger.isTraceEnabled(), is(true));
        when(mockDelegate.isTraceEnabled()).thenReturn(false);
        assertThat(logger.isTraceEnabled(), is(false));
    }

    @Test
    public void testDebug() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.debug("Message");
            }
        });
        verify(mockDelegate, new Times(12)).debug(any(String.class));
    }

    @Test
    public void testDebugWithException() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.debug("Message", new Exception());
            }
        });
        verify(mockDelegate, new Times(12)).debug(any(String.class), any(Exception.class));
    }

    @Test
    public void testError() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.error("Message");
            }
        });
        verify(mockDelegate, new Times(12)).error(any(String.class));

    }

    @Test
    public void testErrorWithException() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.error("Message", new Exception());
            }
        });
        verify(mockDelegate, new Times(6)).error(any(String.class), any(Exception.class));
        verify(mockDelegate, new Times(6)).error(any(String.class));
        verify(mockDelegate, new Times(8)).warn(any(String.class));
    }

    @Test
    public void testFatal() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.fatal("Message");
            }
        });
        verify(mockDelegate, new Times(12)).fatal(any(String.class));

    }

    @Test
    public void testFatalWithException() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.fatal("Message", new Exception());
            }
        });
        verify(mockDelegate, new Times(12)).fatal(any(String.class), any(Exception.class));
    }

    @Test
    public void testIsDebugEnabled() throws Exception
    {
        when(mockDelegate.isDebugEnabled()).thenReturn(true);
        assertThat(logger.isDebugEnabled(), is(true));
        when(mockDelegate.isDebugEnabled()).thenReturn(false);
        assertThat(logger.isDebugEnabled(), is(false));
    }

    @Test
    public void testIsEnabledFor() throws Exception
    {
        when(mockDelegate.isEnabledFor(Level.DEBUG)).thenReturn(true);
        assertThat(logger.isEnabledFor(Level.DEBUG), is(true));
        when(mockDelegate.isEnabledFor(Level.DEBUG)).thenReturn(false);
        assertThat(logger.isEnabledFor(Level.DEBUG), is(false));
    }

    @Test
    public void testIsInfoEnabled() throws Exception
    {
        when(mockDelegate.isInfoEnabled()).thenReturn(true);
        assertThat(logger.isInfoEnabled(), is(true));
        when(mockDelegate.isInfoEnabled()).thenReturn(false);
        assertThat(logger.isInfoEnabled(), is(false));
    }

    @Test
    public void testInfo() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.info("Message");
            }
        });
        verify(mockDelegate, new Times(12)).info(any(String.class));
    }

    @Test
    public void testInfoWithException() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.info("Message", new Exception());
            }
        });
        verify(mockDelegate, new Times(12)).info(any(String.class), any(Exception.class));
    }

    @Test
    public void testWarn() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.warn("Message");
            }
        });
        verify(mockDelegate, new Times(12)).warn(any(String.class));
    }

    @Test
    public void testWarnWithException() throws Exception
    {
        logRunner(new Runnable()
        {
            @Override
            public void run()
            {
                logger.warn("Message", new Exception());
            }
        });
        verify(mockDelegate, new Times(6)).warn(any(String.class), any(Exception.class));
        verify(mockDelegate, new Times(14)).warn(any(String.class));
    }

    private void logRunner(Runnable runnable)
    {
        for (int i = 0; i < 6; i++)
        {
             runnable.run();
        }
        RateLimitingLogger.reset();
        for (int i = 0; i < 6; i++)
        {
            runnable.run();
        }
    }
}
