package com.atlassian.jira.config.component;

import com.atlassian.jira.component.CachingMutablePicoContainer;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.behaviors.Locking;

/**
 * Factory methods for setting up PICO with behaviours desired in JIRA
 * @since v6.2
 */
public class PicoContainerFactory
{

    public static MutablePicoContainer defaultJIRAContainer(PicoContainer parentContainer)
    {
        MutablePicoContainer realPicoContainer = new PicoBuilder(parentContainer).withBehaviors(new Locking(), new Caching()).withConstructorInjection().build();
        return new CachingMutablePicoContainer(realPicoContainer);
    }

    public static MutablePicoContainer defaultJIRAContainer()
    {
        return defaultJIRAContainer(null);
    }

}
