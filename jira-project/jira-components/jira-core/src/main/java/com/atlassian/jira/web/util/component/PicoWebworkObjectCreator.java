package com.atlassian.jira.web.util.component;

import java.io.IOException;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.util.JiraUtils;

import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

import webwork.util.injection.ObjectCreator;

/**
 * Allow object created in Webwork to be created using Pico (inside JIRA).
 */
public class PicoWebworkObjectCreator implements ObjectCreator
{
    public static final Logger log = Logger.getLogger("webwork");

    public Object instantiate(Class actionClass) throws IllegalAccessException, InstantiationException
    {
        // We use a requestLevelContainer purely for the resolution of Workflow names
        // If we removed the need for this resolution, then we can just use the JiraUtils.loadComponent()
        // instead
        
        //get the application scoped container
        PicoContainer applicationContainer = ComponentManager.getInstance().getContainer();

        //this container is the container that we can use to setup all the request level components
        //this container is created & destroyed 'per-request'
        //chain the containers together
        //note that the parent container for the web container is the core container, so we do not need to add it explicitly
        MutablePicoContainer requestContainer = new RequestComponentManager().getContainer(applicationContainer);

        //register the actionClass that we need.
        requestContainer.addComponent(actionClass);

        try
        {
            return requestContainer.getComponent(actionClass);
        } catch (RuntimeException re)
        {
            // Webwork sucks!! JavaActionFactory catches exceptions and then logs *nothing* except the name (ClassNotFoundException)
            // As a workaround, we log the actual error here.
            log.error("Error instantiating '"+actionClass.getName()+"': "+re.getMessage(), re);
            throw re;
        }
    }

    public Object instantiateBean(ClassLoader classloader, String className) throws IOException, ClassNotFoundException
    {
        return JiraUtils.loadComponent(className, this.getClass());
    }
}
