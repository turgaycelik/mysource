package com.atlassian.jira.web.filters.steps.senderror;

import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterCallContextImpl;
import com.atlassian.jira.web.filters.steps.FilterStep;

/**
 * Step that wraps the servlet response with a CaptureSendErrorMessageResponseWrapper
 *
 * @since v5.0
 */
public class CaptureSendErrorMessageStep implements FilterStep
{
    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext context)
    {

        return new FilterCallContextImpl(context.getHttpServletRequest(),
                new CaptureSendErrorMessageResponseWrapper(context.getHttpServletRequest(), context.getHttpServletResponse()),
                context.getFilterChain(), context.getFilterConfig());
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        return callContext;
    }
}
