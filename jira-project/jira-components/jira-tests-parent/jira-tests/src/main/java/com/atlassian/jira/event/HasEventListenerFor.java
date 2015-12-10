package com.atlassian.jira.event;

import com.atlassian.event.api.EventListener;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Hamcrest matcher that determines if a given class is capable of handling an event.
 *
 * @since 4.4
 */
public class HasEventListenerFor extends TypeSafeMatcher<Class<?>>
{
    public static Matcher<Class<?>> hasEventListenerFor(Class<?> eventClass)
    {
        return new HasEventListenerFor(eventClass);
    }

    /**
     * The event class.
     */
    private final Class<?> eventClass;

    /**
     * Creates a new HasEventListenerFor matcher.
     *
     * @param eventClass the event class to check for
     */
    public HasEventListenerFor(Class<?> eventClass)
    {
        if (eventClass == null)
        {
            throw new NullPointerException("eventClass");
        }

        this.eventClass = eventClass;
    }

    @Override
    public boolean matchesSafely(Class<?> eventListenerClass)
    {
        final MutableBoolean handlesNotification = new MutableBoolean();
        ReflectionUtils.doWithMethods(eventListenerClass, new ReflectionUtils.MethodCallback()
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

    @Override
    public void describeTo(Description description)
    {
        description.appendText(String.format("A class containing an @EventListener method that takes a single argument of type %s", eventClass.getSimpleName()));
    }
}
