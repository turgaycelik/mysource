package com.atlassian.jira.web;

import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Per-request variable (similar to {@code ThreadLocal}, but request-scoped).
 *
 * @since v4.4
 */
@Immutable
public class HttpRequestLocal<T>
{
    /**
     * The name of the request attribute that will be used.
     */
    private final String name;

    /**
     * Creates a new HttpRequestLocal, specifying the name to use. This name is used as the attribute name under which
     * this request local is stored in the servlet request.
     *
     * @param name a String containing the attribute name to use in the servlet request
     */
    public HttpRequestLocal(String name)
    {
        this.name = Assertions.notNull(name);
    }

    /**
     * Returns the value of this HttpRequestLocal, or null if it is not set. If there is no current HTTP request, this
     * method returns null regardless of what {@link #initialValue()} would have returned.
     *
     * @return the value of this HttpRequestLocal, or null
     */
    @SuppressWarnings ("unchecked")
    public T get()
    {
        return ifRequestAvailable(new RequestOperation<T>()
        {
            @Override
            public T doInRequest(HttpServletRequest request)
            {
                T value = (T) request.getAttribute(name);
                if (value == null)
                {
                    // assign from initialValue()
                    value = initialValue();
                    request.setAttribute(name, value);
                }

                return value;
            }
        });
    }

    /**
     * Sets the value of this HttpRequestLocal.
     *
     * @param value the value to set
     */
    public void set(final T value)
    {
        ifRequestAvailable(new RequestOperation<Void>()
        {
            @Override
            public Void doInRequest(HttpServletRequest request)
            {
                request.setAttribute(name, value);
                return null;
            }
        });
    }

    /**
     * Removes the value of this HttpRequestLocal.
     */
    public void remove()
    {
        ifRequestAvailable(new RequestOperation<Void>()
        {
            @Override
            public Void doInRequest(HttpServletRequest request)
            {
                request.removeAttribute(name);
                return null;
            }
        });
    }

    /**
     * Returns the current request's "initial value" for this request-local variable. This method will be invoked the
     * first time that this request local variable's {@link #get()} method is called in a request.
     * <p/>
     * This implementation simply returns null. Override this method for different initial values.
     *
     * @return the initial value for this request local
     */
    protected T initialValue()
    {
        return null;
    }

    private <T> T ifRequestAvailable(RequestOperation<T> requestOperation)
    {
        HttpServletRequest request = ExecutingHttpRequest.get();
        if (request != null)
        {
            return requestOperation.doInRequest(request);
        }

        return null;
    }

    private interface RequestOperation<T>
    {
        T doInRequest(HttpServletRequest request);
    }
}
