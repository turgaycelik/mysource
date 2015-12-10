package com.atlassian.jira.web.filters.steps.requestinfo;

import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is called form the l;ast filter to ensure the request info is as current as possible
 *
 * @since v4.3
 */
public class RequestInfoLastStep implements FilterStep
{
    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        HttpServletRequest httpServletRequest = callContext.getHttpServletRequest();
        HttpServletResponse httpServletResponse = callContext.getHttpServletResponse();

        //
        // This is pretty much the inner most filter and hence the request object is wrapped and decorated as much as possible
        // so we need to record that into the local variable
        ExecutingHttpRequest.set(httpServletRequest, httpServletResponse);

        // we make an extra call here to the ensure that the ASESSIONID is put into play.  Its possible
        // when we dont have a session (until the request gets to here for example) that the ASESSIONID will not have been
        // generated so lets give it a go
        new AccessLogRequestInfo().enterRequest(httpServletRequest, httpServletResponse);

        return callContext;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        return callContext;
    }
}
