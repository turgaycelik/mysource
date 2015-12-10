package com.atlassian.jira.web.filters.steps.senderror;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Captures the message in calls to sendError() and setStatus() and stores
 * them in a request attribute CAPTURED_MESSAGE_REQUEST_ATTR
 *
 * @since v5.0
 */
public class CaptureSendErrorMessageResponseWrapper extends HttpServletResponseWrapper
{
    public static final String CAPTURED_MESSAGE_REQUEST_ATTR = CaptureSendErrorMessageResponseWrapper.class.getName() + "_CAPTURED";
    private final HttpServletRequest request;

    public CaptureSendErrorMessageResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        capture(msg);
        super.sendError(sc, msg);
    }

    @Override
    public void setStatus(int sc, String msg)
    {
        capture(msg);
        super.setStatus(sc, msg);
    }

    private void capture(String msg)
    {
        if (msg != null)
        {
            request.setAttribute(CAPTURED_MESSAGE_REQUEST_ATTR, msg);
        }
    }

}
