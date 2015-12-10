package com.atlassian.jira.event.user;

import com.atlassian.event.spi.ListenerHandler;
import com.atlassian.event.spi.ListenerInvoker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Used to invoke the legacy UserEvents from atlassian-events.
 *
 * @since v4.1
 */
public class UserEventListenerHandler implements ListenerHandler
{
    public List<? extends ListenerInvoker> getInvokers(final Object o)
    {
        if (o instanceof UserEventListener)
        {
            return Collections.singletonList(new UserEventInvoker((UserEventListener) o));
        }
        return Collections.emptyList();
    }

    class UserEventInvoker implements ListenerInvoker
    {
        private final UserEventListener userEventListener;

        private UserEventInvoker(final UserEventListener userEventListener)
        {
            this.userEventListener = userEventListener;
        }

        public Set<Class<?>> getSupportedEventTypes()
        {
            return Collections.<Class<?>>singleton(UserEvent.class);
        }

        public void invoke(final Object o)
        {
            if (o instanceof UserEvent)
            {
                UserEvent event = (UserEvent)o;

                if (event.getEventType() == UserEventType.USER_SIGNUP)
                {
                    userEventListener.userSignup(event);
                }
                else if (event.getEventType() == UserEventType.USER_CREATED)
                {
                    userEventListener.userCreated(event);
                }
                else if (event.getEventType() == UserEventType.USER_FORGOTPASSWORD)
                {
                    userEventListener.userForgotPassword(event);
                }
                else if (event.getEventType() == UserEventType.USER_FORGOTUSERNAME)
                {
                    userEventListener.userForgotUsername(event);
                }
                else if (event.getEventType() == UserEventType.USER_CANNOTCHANGEPASSWORD)
                {
                    userEventListener.userCannotChangePassword(event);
                }
            }
        }

        public boolean supportAsynchronousEvents()
        {
            return false;
        }
    }
}
