package com.atlassian.jira.util.log;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

/**
 * Test for NShotLogger and OneShotLogger
 *
 * @since v3.13
 */
public class TestNShotLogger
{
    private static final LogMethodClosure DEBUG = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.debug(IGNORE_THIS_LOG_MSG);
        }
    };

    private static final LogMethodClosure DEBUG_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.debug(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };

    private static final LogMethodClosure INFO = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.info(IGNORE_THIS_LOG_MSG);
        }
    };

    private static final LogMethodClosure INFO_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.info(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };

    private static final LogMethodClosure WARN = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.warn(IGNORE_THIS_LOG_MSG);
        }
    };

    private static final LogMethodClosure WARN_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.warn(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };

    private static final LogMethodClosure FATAL = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.fatal(IGNORE_THIS_LOG_MSG);
        }
    };

    private static final LogMethodClosure FATAL_THROWABLE = new LogMethodClosure()
    {
        public void performLogging(final NShotLogger logger)
        {
            logger.fatal(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        }
    };

    private static final String IGNORE_THIS_LOG_MSG =
            "You can ignore these log messages, they are generated ON PURPOSE by " + TestNShotLogger.class;

    private static final Exception IGNORE_THIS_THROWABLE = new Exception(IGNORE_THIS_LOG_MSG);

    private CallCountingLogger callCountingLogger;

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldNotAcceptNullDelegate() throws Exception
    {
        new NShotLogger(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldNotAcceptZeroMaxTimes() throws Exception
    {
        new NShotLogger(new CallCountingLogger(getClass().getName()), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldNotAcceptNegativeMaxTimes() throws Exception
    {
        new NShotLogger(new CallCountingLogger(getClass().getName()), -1);
    }

    private interface LogMethodClosure
    {
        void performLogging(NShotLogger logger);
    }

    @Test
    public void testCallCountOnceInfo() throws Exception
    {
        final LogMethodClosure[] methods =
                {DEBUG, DEBUG_THROWABLE, INFO, INFO_THROWABLE, FATAL, FATAL_THROWABLE, WARN, WARN_THROWABLE};
        for (final LogMethodClosure method : methods)
        {
            NShotLogger logger = createNewNShotLogger(1);
            _testCallCountOnce(logger, method);

            logger = createNewOneShotLogger();
            _testCallCountOnce(logger, method);
        }
    }

    private void _testCallCountOnce(final NShotLogger logger, final LogMethodClosure closure) throws Exception
    {
        assertHasOutputNTimes(0);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
    }

    @Test
    public void testCallCountMany() throws Exception
    {
        final LogMethodClosure[] methods =
                {DEBUG, DEBUG_THROWABLE, INFO, INFO_THROWABLE, FATAL, FATAL_THROWABLE, WARN, WARN_THROWABLE};
        for (final LogMethodClosure method : methods)
        {
            final NShotLogger logger = createNewNShotLogger(5);
            _testCallCountMany(logger, method);
        }
    }

    private void _testCallCountMany(final NShotLogger logger, final LogMethodClosure closure) throws Exception
    {
        assertHasOutputNTimes(0);
        closure.performLogging(logger);
        assertHasOutputNTimes(1);
        closure.performLogging(logger);
        assertHasOutputNTimes(2);
        closure.performLogging(logger);
        assertHasOutputNTimes(3);
        closure.performLogging(logger);
        assertHasOutputNTimes(4);
        closure.performLogging(logger);
        assertHasOutputNTimes(5);
        closure.performLogging(logger);
        assertHasOutputNTimes(5);
        closure.performLogging(logger);
        assertHasOutputNTimes(5);
    }

    @Test
    public void testRightUnderlyingMethodsCalled()
    {
        final NShotLogger logger = createNewNShotLogger(100);

        logger.debug(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.debugCount.get());
        logger.debug(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.debugCount.get());

        logger.error(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.errorCount.get());
        logger.error(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.errorCount.get());

        logger.info(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.infoCount.get());
        logger.info(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.infoCount.get());

        logger.fatal(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.fatalCount.get());
        logger.fatal(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.fatalCount.get());

        logger.warn(IGNORE_THIS_LOG_MSG);
        assertEquals(1, callCountingLogger.warnCount.get());
        logger.warn(IGNORE_THIS_LOG_MSG, IGNORE_THIS_THROWABLE);
        assertEquals(2, callCountingLogger.warnCount.get());

        assertEquals(10, callCountingLogger.callCount.get());
        assertEquals(2, callCountingLogger.debugCount.get());
        assertEquals(2, callCountingLogger.errorCount.get());
        assertEquals(2, callCountingLogger.infoCount.get());
        assertEquals(2, callCountingLogger.fatalCount.get());
        assertEquals(2, callCountingLogger.warnCount.get());

    }

    private void assertHasOutputNTimes(final long expectedCallCount)
    {
        assertEquals(expectedCallCount, callCountingLogger.callCount.get());
    }

    private NShotLogger createNewNShotLogger(final int maxTimes)
    {
        callCountingLogger = new CallCountingLogger(getClass().getName());
        return new NShotLogger(callCountingLogger, maxTimes);
    }

    private NShotLogger createNewOneShotLogger()
    {
        callCountingLogger = new CallCountingLogger(getClass().getName());
        return new OneShotLogger(callCountingLogger);
    }

    private static class CallCountingLogger extends Logger
    {
        public static CallCountingLogger create(String name)
        {
            // Modifying the logger name, to avoid hitting the cache.
            return (CallCountingLogger) LogManager.getLogger("CallCountingLogger:" + name, new LoggerFactory()
            {
                @Override
                public Logger makeNewLoggerInstance(String name)
                {
                    return new CallCountingLogger(name);
                }
            });
        }

        private final AtomicLong callCount;
        private final AtomicLong debugCount;
        private final AtomicLong errorCount;
        private final AtomicLong infoCount;
        private final AtomicLong fatalCount;
        private final AtomicLong warnCount;
        private static final Logger delegate = Logger.getLogger(CallCountingLogger.class);

        public CallCountingLogger(String name)
        {
            super(name);
            callCount = new AtomicLong(0);
            debugCount = new AtomicLong(0);
            errorCount = new AtomicLong(0);
            infoCount = new AtomicLong(0);
            fatalCount = new AtomicLong(0);
            warnCount = new AtomicLong(0);
        }

        @Override
        public void debug(final Object o)
        {
            callCount.incrementAndGet();
            debugCount.incrementAndGet();
            delegate.debug(o);
        }

        @Override
        public void debug(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            debugCount.incrementAndGet();
            delegate.debug(o, throwable);
        }

        @Override
        public void error(final Object o)
        {
            callCount.incrementAndGet();
            errorCount.incrementAndGet();
            delegate.error(o);
        }

        @Override
        public void error(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            errorCount.incrementAndGet();
            delegate.error(o, throwable);
        }

        @Override
        public void fatal(final Object o)
        {
            callCount.incrementAndGet();
            fatalCount.incrementAndGet();
            delegate.fatal(o);
        }

        @Override
        public void fatal(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            fatalCount.incrementAndGet();
            delegate.fatal(o, throwable);
        }

        @Override
        public void info(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            infoCount.incrementAndGet();
            delegate.info(o, throwable);
        }

        @Override
        public void info(final Object o)
        {
            callCount.incrementAndGet();
            infoCount.incrementAndGet();
            delegate.info(o);
        }

        @Override
        public void warn(final Object o)
        {
            callCount.incrementAndGet();
            warnCount.incrementAndGet();
            delegate.warn(o);
        }

        @Override
        public void warn(final Object o, final Throwable throwable)
        {
            callCount.incrementAndGet();
            warnCount.incrementAndGet();
            delegate.warn(o, throwable);
        }

        @Override
        public void addAppender(final Appender appender)
        {
            delegate.addAppender(appender);
        }

        @Override
        public void assertLog(final boolean b, final String s)
        {
            delegate.assertLog(b, s);
        }

        @Override
        public void callAppenders(final LoggingEvent event)
        {
            delegate.callAppenders(event);
        }

        @Override
        public boolean getAdditivity()
        {
            return delegate.getAdditivity();
        }

        @Override
        public Enumeration getAllAppenders()
        {
            return delegate.getAllAppenders();
        }

        @Override
        public Appender getAppender(final String s)
        {
            return delegate.getAppender(s);
        }

        @Override
        public Level getEffectiveLevel()
        {
            return delegate.getEffectiveLevel();
        }

        @Override
        @SuppressWarnings("deprecation")
        public Priority getChainedPriority()
        {
            return delegate.getChainedPriority();
        }

        @Override
        @SuppressWarnings("deprecation")
        public LoggerRepository getHierarchy()
        {
            return delegate.getHierarchy();
        }

        @Override
        public LoggerRepository getLoggerRepository()
        {
            return delegate.getLoggerRepository();
        }

        @Override
        public ResourceBundle getResourceBundle()
        {
            return delegate.getResourceBundle();
        }

        @Override
        public boolean isAttached(final Appender appender)
        {
            return delegate.isAttached(appender);
        }

        @Override
        public boolean isDebugEnabled()
        {
            return delegate.isDebugEnabled();
        }

        @Override
        public boolean isEnabledFor(final Priority priority)
        {
            return delegate.isEnabledFor(priority);
        }

        @Override
        public boolean isInfoEnabled()
        {
            return delegate.isInfoEnabled();
        }

        @Override
        public void l7dlog(final Priority priority, final String s, final Throwable throwable)
        {
            delegate.l7dlog(priority, s, throwable);
        }

        @Override
        public void l7dlog(final Priority priority, final String s, final Object[] objects, final Throwable throwable)
        {
            delegate.l7dlog(priority, s, objects, throwable);
        }

        @Override
        public void removeAllAppenders()
        {
            delegate.removeAllAppenders();
        }

        @Override
        public void removeAppender(final Appender appender)
        {
            delegate.removeAppender(appender);
        }

        @Override
        public void removeAppender(final String s)
        {
            delegate.removeAppender(s);
        }

        @Override
        public void setAdditivity(final boolean b)
        {
            delegate.setAdditivity(b);
        }

        @Override
        public void setLevel(final Level level)
        {
            delegate.setLevel(level);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void setPriority(final Priority priority)
        {
            delegate.setPriority(priority);
        }

        @Override
        public void setResourceBundle(final ResourceBundle resourceBundle)
        {
            delegate.setResourceBundle(resourceBundle);
        }
    }
}
