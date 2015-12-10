package com.atlassian.jira.issue;

import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Default implementation that uses the http session for storage.
 *
 * @since v4.2
 */
public class DefaultTemporaryAttachmentsMonitorLocator implements TemporaryAttachmentsMonitorLocator
{
    public TemporaryAttachmentsMonitor get(boolean create)
    {
        final HttpSession session = getCurrentSession(create);
        if (session == null)
        {
            return null;
        }
        TemporaryAttachmentsMonitor monitor = (TemporaryAttachmentsMonitor)session.getAttribute(SessionKeys.TEMP_ATTACHMENTS);
        if(monitor == null && create)
        {
            monitor = new TemporaryAttachmentsMonitor();
            session.setAttribute(SessionKeys.TEMP_ATTACHMENTS, monitor);
        }
        return monitor;
    }

    private HttpSession getCurrentSession(boolean create)
    {
        final HttpServletRequest request = ExecutingHttpRequest.get();
        return request == null ? null : request.getSession(create);
    }
}
