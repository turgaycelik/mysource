package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.web.action.admin.LookAndFeelUpdatedEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Listener to check if a user has updated the look and feel of the instance.
 *
 * @since v6.0
 */
public class LookAndFeelUpdateListener implements InitializingBean, DisposableBean
{
    private final EventPublisher eventPublisher;
    private final AdminTaskUserPropertyManager adminTaskUserPropertyManager;

    public LookAndFeelUpdateListener(final EventPublisher eventPublisher,
            final AdminTaskUserPropertyManager adminTaskUserPropertyManager)
    {
        this.eventPublisher = eventPublisher;
        this.adminTaskUserPropertyManager = adminTaskUserPropertyManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onLookAndFeelUpdated(final LookAndFeelUpdatedEvent event)
    {
        final User user = event.getUser();
        if (user != null)
        {
            adminTaskUserPropertyManager.setLookAndFeelUpdated(user, true);
        }
    }
}
