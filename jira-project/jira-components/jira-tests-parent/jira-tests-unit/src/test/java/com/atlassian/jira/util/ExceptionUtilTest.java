package com.atlassian.jira.util;

import java.lang.reflect.InvocationTargetException;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @since v6.3
 */
public class ExceptionUtilTest
{
    public static final String ROOT_MESSAGE = "ex message 1";
    public static final String CAUSE_MESSAGE = "ex message 2";
    public static final String CAUSED_BY_MESSAGE = " caused by: ";

    @Test
    public void shouldUseMessageWhenNoCauseAndMessageProvided()
    {
        final Exception exception = ExBuilder.exception(RootException.class).message(ROOT_MESSAGE).build();

        final String message = ExceptionUtil.getMessage(exception);

        assertEquals(ROOT_MESSAGE, message);
    }

    @Test
    public void shouldUseClassNameWhenNoCauseAndNoMessage()
    {
        final Exception exception = ExBuilder.exception(RootException.class).build();

        final String message = ExceptionUtil.getMessage(exception);

        assertEquals(exception.getClass().getSimpleName(), message);
    }

    @Test
    public void shouldConcatenateMessageFromCauseAndException()
    {
        final Exception exception = ExBuilder
                .exception(RootException.class).message(ROOT_MESSAGE)
                .cause(CauseException.class).causeMessage(CAUSE_MESSAGE).build();

        final String message = ExceptionUtil.getMessage(exception);

        assertThat(message, Matchers.containsString(ROOT_MESSAGE));
        assertThat(message, Matchers.containsString(CAUSED_BY_MESSAGE));
        assertThat(message, Matchers.containsString(CAUSE_MESSAGE));
    }

    @Test
    public void shouldConcatenateClassNameFromCauseAndException()
    {
        final Exception exception = ExBuilder
                .exception(RootException.class)
                .cause(CauseException.class).build();

        final String message = ExceptionUtil.getMessage(exception);

        assertThat(message, Matchers.containsString(getClassSimpleName(RootException.class)));
        assertThat(message, Matchers.containsString(CAUSED_BY_MESSAGE));
        assertThat(message, Matchers.containsString(getClassSimpleName(CauseException.class)));
    }

    @Test
    public void shouldConcatenateClassNameFromCauseAndMessageFromException()
    {

        final Exception exception = ExBuilder
                .exception(RootException.class).message(ROOT_MESSAGE)
                .cause(CauseException.class).build();

        final String message = ExceptionUtil.getMessage(exception);

        assertThat(message, Matchers.containsString(ROOT_MESSAGE));
        assertThat(message, Matchers.containsString(CAUSED_BY_MESSAGE));
        assertThat(message, Matchers.containsString(getClassSimpleName(CauseException.class)));
    }

    @Test
    public void shouldConcatenateMessageFromCauseAndClassNameFromException()
    {
        final Exception exception = ExBuilder
                .exception(RootException.class)
                .cause(CauseException.class).causeMessage(CAUSE_MESSAGE).build();

        final String message = ExceptionUtil.getMessage(exception);

        assertThat(message, Matchers.containsString(getClassSimpleName(RootException.class)));
        assertThat(message, Matchers.containsString(CAUSED_BY_MESSAGE));
        assertThat(message, Matchers.containsString(CAUSE_MESSAGE));
    }

    private String getClassSimpleName(Class<? extends Exception> exClass)
    {
        return exClass.getSimpleName();
    }

    private static class ExBuilder
    {
        private final Class<? extends Exception> exceptionClass;
        private String causeMessage;
        private Class<? extends Exception> causeClass;
        private String message;

        private ExBuilder(final Class<? extends Exception> exceptionClass)
        {
            Preconditions.checkNotNull("Exception cannot be null", exceptionClass);
            this.exceptionClass = exceptionClass;
        }

        static ExBuilder exception(Class<? extends Exception> exceptionClass)
        {
            return new ExBuilder(exceptionClass);
        }

        private ExBuilder message(String message)
        {
            this.message = message;
            return this;
        }

        private ExBuilder cause(Class<? extends Exception> causeClass)
        {
            this.causeClass = causeClass;
            return this;
        }

        private ExBuilder causeMessage(String causeMessage)
        {
            this.causeMessage = causeMessage;
            return this;
        }

        Exception build()
        {
            Preconditions.checkArgument(causeClass != null || causeMessage == null, "Cause message is set but no cause class is present");
            try
            {
                final Exception exception = createException(exceptionClass, message);
                if (causeClass != null)
                {
                    exception.initCause(createException(causeClass, causeMessage));
                }

                return exception;
            }
            catch (final Exception e)
            {
                e.printStackTrace();
                fail();
                return null;
            }
        }

        private Exception createException(Class<? extends Exception> exClass, String exMessage)
                throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
        {
            if (StringUtils.isNotBlank(exMessage))
            {
                return exClass.getConstructor(String.class).newInstance(exMessage);
            }
            else
            {
                return exClass.getConstructor().newInstance();
            }
        }
    }

    private static class RootException extends Exception

    {

        public RootException(final String message)
        {
            super(message);
        }

        public RootException()
        {
        }
    }

    private static class CauseException extends Exception

    {

        public CauseException(final String message)
        {
            super(message);
        }

        public CauseException()
        {
        }
    }
}
