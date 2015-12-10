package com.atlassian.jira.mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import junit.framework.AssertionFailedError;

/**
 * Mockito mocks are lenient by default, and this makes them throw an exception for
 * anything that was not expressly stubbed.  Note that this means the
 * {@code when(mock.method())} style of stubbing will not work because the
 * {@code mock.method()} call will throw an exception instead of returning
 * {@code null}.  You will need to use the {@code doReturn(null).when(mock).method()}
 * style of stubbing, instead.
 * <p/>
 * Why would you do this instead of just letting it throw a {@code NullPointerException}
 * when the unstubbed method is invoked?  Because this will actually tell you what
 * the unexpected invocation was.  Much more helpful!
 * <p/>
 *
 * @since v6.2
 */
public class Strict<T> implements Answer<T>
{
    private static final Strict INSTANCE = new Strict();

    /** Factory method that will infer the correct type from the context */
    @SuppressWarnings("unchecked")
    public static <T> Strict<T> strict()
    {
        return (Strict<T>)INSTANCE;
    }

    /** Factory method that will infer the correct type from the supplied class */
    public static <T> Strict<T> strict(Class<T> tClass)
    {
        return strict();
    }

    /**
     * Throws an exception that reports the unexpected method invocation.
     *
     * @param invocationOnMock the invocation that reached us
     * @return <em>never returns normally</em>
     * @throws AssertionFailedError reporting the invocation
     */
    @Override
    public T answer(final InvocationOnMock invocationOnMock) throws Throwable
    {
        throw new AssertionFailedError("Unexpected invocation: " + invocationOnMock);
    }
}
