package com.atlassian.jira.web.filters.steps.requestinfo;

import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sets up the request info information in JIRA as one of the very first steps
 *
 * @since v4.3
 */
public class RequestInfoFirstStep implements FilterStep
{
    private final AccessLogRequestInfo accessLogRequestInfo;

    public RequestInfoFirstStep()
    {
        accessLogRequestInfo = new AccessLogRequestInfo();
    }

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        final HttpServletRequest httpServletRequest = callContext.getHttpServletRequest();
        final HttpServletResponse httpServletResponse = callContext.getHttpServletResponse();

        //
        // this is the first filter in JIRA and hence the earliest possible moment to set these things up
        ExecutingHttpRequest.set(httpServletRequest, httpServletResponse);
        accessLogRequestInfo.enterRequest(httpServletRequest, httpServletResponse);

        return callContext;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        ExecutingHttpRequest.clear();
        accessLogRequestInfo.exitRequest(callContext.getHttpServletRequest());

        return callContext;
    }
}
