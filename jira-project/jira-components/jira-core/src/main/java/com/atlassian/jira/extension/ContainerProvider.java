package com.atlassian.jira.extension;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

/**
 * A ContainerProvider provides a container to override the container created as a normal process inside JIRA.
 * <p>
 * This container is registered in application properties using the key {@link com.atlassian.jira.ComponentManager#EXTENSION_PROVIDER_PROPERTY}
 * and will be loaded only once upon startup.
 */
public interface ContainerProvider
{
    /**
     * Provide a container containing implementations that you want to override the defaults provided.
     * @param parent    The default container provided by JIRA.  You will most likely want to use this as the parent container for the container that is created
     */ 
    public MutablePicoContainer getContainer(PicoContainer parent);
}
