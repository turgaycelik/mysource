package com.atlassian.jira;

/**
 * An implementation of {@link ComponentManager.State} interface
 */
enum ComponentManagerStateImpl implements ComponentManager.State
{


    /**
     * Not registered, plugins haven't started
     */
    NOT_STARTED(false, false, false, false),
    /**
     * Not registered, plugins not started, but container is initialised
     */
    CONTAINER_INITIALISED(true, false, false, false),
    /**
     * Not registered, plugins haven't started
     */
    PLUGINSYSTEM_STARTED(true, false, true, false),
    /**
     * All components registered with PICO including plugin components and plugin system has started.
     */
    COMPONENTS_REGISTERED(true, true, true, false),
    /**
     * All components registered with PICO including plugin components and plugin system has started.
     */
    STARTED(true, true, true, true);

    private final boolean componentsRegistered;
    private final boolean pluginSystemStarted;
    private final boolean started;
    private final boolean containerInitialised;

    ComponentManagerStateImpl(boolean containerInitialised, boolean componentsRegistered, boolean pluginSystemStarted, boolean started) {
        this.containerInitialised = containerInitialised;
        this.componentsRegistered = componentsRegistered;
        this.pluginSystemStarted = pluginSystemStarted;
        this.started = started;
    }

    public boolean isComponentsRegistered()
    {
        return componentsRegistered;
    }

    public boolean isPluginSystemStarted()
    {
        return pluginSystemStarted;
    }

    public boolean isStarted()
    {
        return started;
    }

    public boolean isContainerInitialised()
    {
        return containerInitialised;
    }
}
