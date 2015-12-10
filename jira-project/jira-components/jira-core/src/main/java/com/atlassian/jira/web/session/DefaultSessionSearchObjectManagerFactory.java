package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.search.util.SearchPropertiesManager;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.ExecutingHttpRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @since v4.2
 */
@InjectableComponent
public class DefaultSessionSearchObjectManagerFactory implements SessionSearchObjectManagerFactory
{
    private final SearchPropertiesManager searchPropertiesManager;

    public DefaultSessionSearchObjectManagerFactory(
            SearchPropertiesManager searchPropertiesManager)
    {
        this.searchPropertiesManager = searchPropertiesManager;
    }

    public SessionSearchRequestManager createSearchRequestManager()
    {
        // retrieve the current HttpServletRequest from the ExecutingHttpRequest thread-local which is setup by a filter.
        return createSearchRequestManager(ExecutingHttpRequest.get());
    }

    public SessionSearchRequestManager createSearchRequestManager(final HttpServletRequest request)
    {
        return new DefaultSessionSearchRequestManager(request, new AbstractSessionSearchObjectManager.HttpSessionWrapper(request.getSession()), searchPropertiesManager);
    }

    public SessionSearchRequestManager createSearchRequestManager(final VelocityRequestSession session)
    {
        return new DefaultSessionSearchRequestManager(ExecutingHttpRequest.get(), new AbstractSessionSearchObjectManager.VelocityRequestSessionWrapper(session), searchPropertiesManager);
    }

    public SessionPagerFilterManager createPagerFilterManager()
    {
        return createPagerFilterManager(ExecutingHttpRequest.get());
    }

    public SessionPagerFilterManager createPagerFilterManager(final HttpServletRequest request)
    {
        return new DefaultSessionPagerFilterManager(request, new AbstractSessionSearchObjectManager.HttpSessionWrapper(request.getSession()));
    }

    public SessionPagerFilterManager createPagerFilterManager(final VelocityRequestSession session)
    {
        return new DefaultSessionPagerFilterManager(ExecutingHttpRequest.get(), new AbstractSessionSearchObjectManager.VelocityRequestSessionWrapper(session));
    }

    public SessionNextPreviousPagerManager createNextPreviousPagerManager()
    {
        return createNextPreviousPagerManager(ExecutingHttpRequest.get());
    }

    public SessionNextPreviousPagerManager createNextPreviousPagerManager(final HttpServletRequest request)
    {
        return new DefaultSessionNextPreviousPagerManager(request, new AbstractSessionSearchObjectManager.HttpSessionWrapper(request.getSession()));
    }

    public SessionNextPreviousPagerManager createNextPreviousPagerManager(final VelocityRequestSession session)
    {
        return new DefaultSessionNextPreviousPagerManager(ExecutingHttpRequest.get(), new AbstractSessionSearchObjectManager.VelocityRequestSessionWrapper(session));
    }

    public SessionSelectedIssueManager createSelectedIssueManager()
    {
        return createSelectedIssueManager(ExecutingHttpRequest.get());
    }

    public SessionSelectedIssueManager createSelectedIssueManager(final HttpServletRequest request)
    {
        return new DefaultSessionSelectedIssueManager(request, new AbstractSessionSearchObjectManager.HttpSessionWrapper(request.getSession()));
    }

    public SessionSelectedIssueManager createSelectedIssueManager(final VelocityRequestSession session)
    {
        return new DefaultSessionSelectedIssueManager(ExecutingHttpRequest.get(), new AbstractSessionSearchObjectManager.VelocityRequestSessionWrapper(session));
    }
}
