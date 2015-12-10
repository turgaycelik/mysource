package com.atlassian.jira.web.session.currentusers;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.filters.accesslog.AtlassianSessionIdUtil;
import com.atlassian.sal.api.events.SessionDestroyedEvent;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.security.Principal;

/**
 * This {@link javax.servlet.http.HttpSessionListener} is used to track and remove entries from the {@link
 * JiraUserSessionTracker} when the sessions expired
 *
 * @since v4.0
 */
public class JiraUserSessionDestroyListener implements HttpSessionListener
{
    private static final Logger loggerSecurityEvents = LoginLoggers.LOGIN_SECURITY_EVENTS;

    public void sessionCreated(final HttpSessionEvent event)
    {
        HttpSession httpSession = event.getSession();
        // lets be defensive.  Very unlikely I know!
        if (httpSession != null)
        {
            if (loggerSecurityEvents.isInfoEnabled()) {
                loggerSecurityEvents.info("HttpSession created [" + encodeSessionId(httpSession.getId()) + "]");
            }
        }
    }

    public void sessionDestroyed(final HttpSessionEvent event)
    {
        // JRADEV-14417 giant hack
        if (isPluginsUp())
        {
            HttpSession httpSession = event.getSession();
            // lets be defensive.  Very unlikely I know!
            if (httpSession != null)
            {
                destroySession(httpSession);
            }
        }
    }

    private void destroySession(final HttpSession session)
    {
        getJiraSessionTracker().removeSession(session.getId());

        Principal principal = (Principal) session.getAttribute(DefaultAuthenticator.LOGGED_IN_KEY);
        getEventPublisher().publish
                (
                        SessionDestroyedEvent.builder().
                                sessionId(session.getId()).
                                userName(principal == null ? null : principal.getName()).
                                build()
                );
        if (loggerSecurityEvents.isInfoEnabled()) {
            loggerSecurityEvents.info("HttpSession [" + encodeSessionId(session.getId()) + "] destroyed for '" + (principal == null ? "anonymous" : principal.getName()) + "'");
        }
    }

    @VisibleForTesting
    JiraUserSessionTracker getJiraSessionTracker()
    {
        return JiraUserSessionTracker.getInstance();
    }

    @VisibleForTesting
    EventPublisher getEventPublisher()
    {
        return ComponentAccessor.getComponent(EventPublisher.class);
    }

    @VisibleForTesting
    boolean isPluginsUp() {
        @SuppressWarnings("deprecation")
        final ComponentManager instance = ComponentManager.getInstance();
        return instance.getState().isPluginSystemStarted();
    }

    private String encodeSessionId(final String id)
    {
        return AtlassianSessionIdUtil.generateASESSIONID(id);
    }
}
