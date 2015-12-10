package com.atlassian.jira.web.session;

import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.velocity.VelocityRequestSession;

import javax.servlet.http.HttpServletRequest;

/**
 * Factory to obtain all {@link com.atlassian.jira.web.session.SessionSearchObjectManager} instances.
 *
 * @since v4.2
 * @see SessionSearchRequestManager
 * @see SessionSelectedIssueManager
 * @see SessionNextPreviousPagerManager
 * @see SessionPagerFilterManager
 */
@InjectableComponent
public interface SessionSearchObjectManagerFactory
{
    /**
     * @return instance based on the current {@link javax.servlet.http.HttpServletRequest}
     */
    SessionSearchRequestManager createSearchRequestManager();

    /**
     * @param request the current request
     * @return instance based on the specified request
     */
    SessionSearchRequestManager createSearchRequestManager(HttpServletRequest request);

    /**
     * @param session the session obtain when processing velocity requests
     * @return instance based on the specified session
     */
    SessionSearchRequestManager createSearchRequestManager(VelocityRequestSession session);

    /**
     * @return instance based on the current {@link javax.servlet.http.HttpServletRequest}
     */
    SessionPagerFilterManager createPagerFilterManager();

    /**
     * @param request the current request
     * @return instance based on the specified request
     */
    SessionPagerFilterManager createPagerFilterManager(HttpServletRequest request);

    /**
     * @param session the session obtain when processing velocity requests
     * @return instance based on the specified session
     */
    SessionPagerFilterManager createPagerFilterManager(VelocityRequestSession session);

    /**
     * @return instance based on the current {@link javax.servlet.http.HttpServletRequest}
     */
    SessionNextPreviousPagerManager createNextPreviousPagerManager();

    /**
     * @param request the current request
     * @return instance based on the specified request
     */
    SessionNextPreviousPagerManager createNextPreviousPagerManager(HttpServletRequest request);

    /**
     * @param session the session obtain when processing velocity requests
     * @return instance based on the specified session
     */
    SessionNextPreviousPagerManager createNextPreviousPagerManager(VelocityRequestSession session);

    /**
     * @return instance based on the current {@link javax.servlet.http.HttpServletRequest}
     */
    SessionSelectedIssueManager createSelectedIssueManager();

    /**
     * @param request the current request
     * @return instance based on the specified request
     */
    SessionSelectedIssueManager createSelectedIssueManager(HttpServletRequest request);

    /**
     * @param session the session obtain when processing velocity requests
     * @return instance based on the specified session
     */
    SessionSelectedIssueManager createSelectedIssueManager(VelocityRequestSession session);
}
