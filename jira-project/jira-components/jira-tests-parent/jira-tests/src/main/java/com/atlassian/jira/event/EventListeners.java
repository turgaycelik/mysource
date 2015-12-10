package com.atlassian.jira.event;

import com.atlassian.event.api.EventListener;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Utility class for operating on event listeners.
 *
 * @since 4.4
 */
public class EventListeners
{
    /**
     * Returns true iff the listener class has a handler for the event class.
     *
     * @param listenerClass the Class of the listener
     * @param eventClass the Class of the event
     * @return a boolean indicating whether the listener class has a handler for the event class
     */
    public static boolean has(Class<?> listenerClass, final Class<?> eventClass)
    {
        final MutableBoolean handlesNotification = new MutableBoolean();
        ReflectionUtils.doWithMethods(listenerClass, new ReflectionUtils.MethodCallback()
        {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException
            {
                if (method.getAnnotation(EventListener.class) != null)
                {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 1 && types[0] == eventClass)
                    {
                        handlesNotification.setValue(true);
                    }
                }
            }
        });

        return handlesNotification.booleanValue();
    }
}
