package com.atlassian.jira.web;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Supplier;

/**
 * This has a thread local that contains the currently executing {@link javax.servlet.http.HttpServletRequest}
 * <p/>
 * It is set in the entry filter and set back to null at the end of the filter chain
 *
 * @since v4.1
 */
public class ExecutingHttpRequest
{
    private static final ThreadLocal<HttpInfo> currentHttpInfo = new ThreadLocal<HttpInfo>();

    private static class HttpInfo
    {
        private final HttpServletRequest httpServletRequest;
        private final HttpServletResponse httpServletResponse;

        private HttpInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        {
            this.httpServletRequest = httpServletRequest;
            this.httpServletResponse = httpServletResponse;
        }
    }

    /**
     * @return the currently executing {@link javax.servlet.http.HttpServletRequest} on this thread within JIRA
     */
    public static HttpServletRequest get()
    {
        final HttpInfo httpInfo = currentHttpInfo.get();
        return httpInfo == null ? null : httpInfo.httpServletRequest;
    }

    /**
     * @return the currently executing {@link javax.servlet.http.HttpServletResponse} on this thread within JIRA
     */
    public static HttpServletResponse getResponse()
    {
        final HttpInfo httpInfo = currentHttpInfo.get();
        return httpInfo == null ? null : httpInfo.httpServletResponse;
    }

    /**
     * Return a {@link Supplier} for the currently executing HTTP request.
     *
     * @return a {@link Supplier} for the currently executing HTTP request.
     */
    @Nonnull
    public static Supplier<HttpServletRequest> getSupplier()
    {
        return new Supplier<HttpServletRequest>()
        {
            @Override
            public HttpServletRequest get()
            {
                return ExecutingHttpRequest.get();
            }
        };
    }

    /**
     * DO NOT CALL THIS OUTSIDE OF THE FIRST WEB FILTER
     *
     * @param httpServletRequest the current HttpServletRequest
     */
    public static void set(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        currentHttpInfo.set(new HttpInfo(httpServletRequest, httpServletResponse));
    }

    /**
     * DO NOT CALL THIS OUTSIDE OF THE FIRST WEB FILTER
     * <p/>
     * Called to clear the current HttpServletRequest back to null for this thread
     */
    public static void clear()
    {
        currentHttpInfo.set(null);
    }
}
