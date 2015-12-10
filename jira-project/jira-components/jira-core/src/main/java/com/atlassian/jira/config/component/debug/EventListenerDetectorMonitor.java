package com.atlassian.jira.config.component.debug;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.event.api.EventListenerRegistrar;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.picocontainer.ComponentMonitor;
import org.picocontainer.Injector;
import org.picocontainer.monitors.AbstractComponentMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since v6.2
 */
public class EventListenerDetectorMonitor extends AbstractComponentMonitor
{

    private static final Logger LOG = LoggerFactory.getLogger(EventListenerDetectorMonitor.class);

    public EventListenerDetectorMonitor(final ComponentMonitor delegate)
    {
        super(delegate);
    }

    public EventListenerDetectorMonitor()
    {
    }

    @Override
    public Injector newInjector(final Injector injector)
    {
        final Class componentImplementation = injector.getComponentImplementation();
        if (containsEventRegistrarInAnyOfConstructors.apply(componentImplementation))
        {
            LOG.warn("Class {} contains event registrar in constructor", componentImplementation.getCanonicalName());
        }
        return injector;
    }

    public static Predicate<Class<?>> containsEventRegistrarInAnyOfConstructors = new Predicate<Class<?>>()
    {

        @Override
        public boolean apply(@Nullable final Class<?> input)
        {
            final List<Constructor<?>> constructors = Arrays.asList(input.getConstructors());
            return Iterables.any(constructors, containsEventRegistrarInParams);
        }
    };


    public static final Predicate<Constructor<?>> containsEventRegistrarInParams = new Predicate<Constructor<?>>()
    {

        @Override
        public boolean apply(@Nullable final Constructor<?> input)
        {
            final List<Class<?>> params = Lists.<Class<?>>newArrayList(input.getParameterTypes());
            final Predicate<Class<?>> predicate = Predicates.assignableFrom(EventListenerRegistrar.class);
            return Iterables.any(params, predicate);
        }
    };
}
