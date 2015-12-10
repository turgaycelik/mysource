package com.atlassian.jira.mock.controller;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;
import org.easymock.internal.JavaProxyFactory;
import org.easymock.internal.MocksControl;
import org.easymock.internal.Range;

/**
 * A MockControl that has a nicer toString() behaviour
 *
 * @since v3.13
 */
class ToStringedMockControl<T> extends MockControl
{
    private final Class<T> toMock;
    private final MockControl delegate;
    private static final JavaProxyFactory PROXY_FACTORY = new JavaProxyFactory();

    public static <T> ToStringedMockControl<T> createControl(final Class<T> toMock)
    {
        return new ToStringedMockControl<T>(toMock, MockControl.createControl(toMock));
    }

    public static <T> ToStringedMockControl<T> createNiceControl(final Class<T> toMock)
    {
        return new ToStringedMockControl<T>(toMock, MockControl.createNiceControl(toMock));
    }

    public static <T> ToStringedMockControl<T> createStrictControl(final Class<T> toMock)
    {
        return new ToStringedMockControl<T>(toMock, MockControl.createStrictControl(toMock));
    }

    private ToStringedMockControl(final Class<T> toMock, final MockControl delegate)
    {
        // we dont use these particular parameters but we need some to get one instatiated
        // ironic that a mock framework dowsnt allow easy mocking!
        super(new MocksControl(MocksControl.MockType.NICE), toMock);
        this.toMock = toMock;
        this.delegate = delegate;
    }

    MockControl getDelegateMockControl()
    {
        return delegate;
    }

    @Override
    public String toString()
    {
        return "MockControl for " + toMock.getName();
    }

    @Override
    public Object getMock()
    {
        return delegate.getMock();
    }

    @Override
    public void replay()
    {
        delegate.replay();
    }

    @Override
    public void verify()
    {
        delegate.verify();
    }

    @Override
    public void setVoidCallable()
    {
        delegate.setVoidCallable();
    }

    @Override
    public void setThrowable(final Throwable throwable)
    {
        delegate.setThrowable(throwable);
    }

    @Override
    public void setReturnValue(final Object value)
    {
        delegate.setReturnValue(value);
    }

    @Override
    public void setVoidCallable(final int times)
    {
        delegate.setVoidCallable(times);
    }

    @Override
    public void setThrowable(final Throwable throwable, final int times)
    {
        delegate.setThrowable(throwable, times);
    }

    @Override
    public void setReturnValue(final Object value, final int times)
    {
        delegate.setReturnValue(value, times);
    }

    @Override
    public void setVoidCallable(final Range range)
    {
        delegate.setVoidCallable(range);
    }

    @Override
    public void setThrowable(final Throwable throwable, final Range range)
    {
        delegate.setThrowable(throwable, range);
    }

    @Override
    public void setReturnValue(final Object value, final Range range)
    {
        delegate.setReturnValue(value, range);
    }

    @Override
    public void setDefaultVoidCallable()
    {
        delegate.setDefaultVoidCallable();
    }

    @Override
    public void setDefaultThrowable(final Throwable throwable)
    {
        delegate.setDefaultThrowable(throwable);
    }

    @Override
    public void setDefaultReturnValue(final Object value)
    {
        delegate.setDefaultReturnValue(value);
    }

    @Override
    public void setMatcher(final ArgumentsMatcher matcher)
    {
        delegate.setMatcher(matcher);
    }

    @Override
    public void setVoidCallable(final int minCount, final int maxCount)
    {
        delegate.setVoidCallable(minCount, maxCount);
    }

    @Override
    public void setThrowable(final Throwable throwable, final int minCount, final int maxCount)
    {
        delegate.setThrowable(throwable, minCount, maxCount);
    }

    @Override
    public void setReturnValue(final Object value, final int minCount, final int maxCount)
    {
        delegate.setReturnValue(value, minCount, maxCount);
    }

    @Override
    public void setDefaultMatcher(final ArgumentsMatcher matcher)
    {
        delegate.setDefaultMatcher(matcher);
    }

    @Override
    public void expectAndReturn(final Object ignored, final Object value)
    {
        delegate.expectAndReturn(ignored, value);
    }

    @Override
    public void expectAndReturn(final Object ignored, final Object value, final Range range)
    {
        delegate.expectAndReturn(ignored, value, range);
    }

    @Override
    public void expectAndReturn(final Object ignored, final Object value, final int count)
    {
        delegate.expectAndReturn(ignored, value, count);
    }

    @Override
    public void expectAndReturn(final Object ignored, final Object value, final int min, final int max)
    {
        delegate.expectAndReturn(ignored, value, min, max);
    }

    @Override
    public void expectAndThrow(final Object ignored, final Throwable throwable)
    {
        delegate.expectAndThrow(ignored, throwable);
    }

    @Override
    public void expectAndThrow(final Object ignored, final Throwable throwable, final Range range)
    {
        delegate.expectAndThrow(ignored, throwable, range);
    }

    @Override
    public void expectAndThrow(final Object ignored, final Throwable throwable, final int count)
    {
        delegate.expectAndThrow(ignored, throwable, count);
    }

    @Override
    public void expectAndThrow(final Object ignored, final Throwable throwable, final int min, final int max)
    {
        delegate.expectAndThrow(ignored, throwable, min, max);
    }

    @Override
    public void expectAndDefaultReturn(final Object ignored, final Object value)
    {
        delegate.expectAndDefaultReturn(ignored, value);
    }

    @Override
    public void expectAndDefaultThrow(final Object ignored, final Throwable throwable)
    {
        delegate.expectAndDefaultThrow(ignored, throwable);
    }

     @Override
    public void expectAndReturn(final int i, final int i1)
    {
        delegate.expectAndReturn(i, i1);
    }

    @Override
    public void expectAndReturn(final int i, final int i1, final int i2)
    {
        delegate.expectAndReturn(i, i1, i2);
    }

    @Override
    public void expectAndReturn(final int i, final int i1, final int i2, final int i3)
    {
        delegate.expectAndReturn(i, i1, i2, i3);
    }

    @Override
    public void expectAndReturn(final int i, final int i1, final Range range)
    {
        delegate.expectAndReturn(i, i1, range);
    }
}
