package com.atlassian.sal.jira.lifecycle;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
import com.atlassian.sal.core.lifecycle.DefaultLifecycleManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.List;

/**
 *
 */
public class JiraLifecycleManager extends DefaultLifecycleManager
{
    private static final Logger log = Logger.getLogger(DefaultLifecycleManager.class);
    private final List<ListableBeanFactory> beanFactories;

    public JiraLifecycleManager(PluginEventManager pluginEventManager, List<ListableBeanFactory> beanFactories)
    {
        super(pluginEventManager);
        this.beanFactories = beanFactories;
    }

    public boolean isApplicationSetUp()
    {
        return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_SETUP) != null;
    }

    /**
	 * This method will be invoked by PluginEventManager when PluginFrameworkStartedEvent event occurs.
	 * PluginEventManager uses methods called "channel" and methods with annotation "@PluginEventListener"
	 * to notify a registered listeners about events.
	 * See {@link com.atlassian.plugin.event.impl.DefaultPluginEventManager} for more details on this black magic.
	 * @param event the Event
	 */
	@PluginEventListener
	public void onFrameworkStart(final PluginFrameworkStartedEvent event)
	{
		// Ignore this - wait until JIRA tells us it has started.
    }


    /**
	 * This method will be invoked by PluginEventManager when PluginFrameworkStartedEvent event occurs.
	 * PluginEventManager uses methods called "channel" and methods with annotation "@PluginEventListener"
	 * to notify a registered listeners about events.
	 * See {@link com.atlassian.plugin.event.impl.DefaultPluginEventManager} for more details on this black magic.
	 * @param event the JiraStartedEvent
	 */
    @PluginEventListener
	public void onJiraStart(final JiraStartedEvent event)
	{
        log.debug("Jira Started event received.");
		start();
	}

	protected void notifyOnStart()
	{
        // Let the super class do the default notify stuff.
        super.notifyOnStart();
        // Lets also look for Startable components and let them know that JIRA is started.
        for (ListableBeanFactory lbf : beanFactories)
        {
            String[] names = lbf.getBeanDefinitionNames();

            for (String name : names)
            {
                try
                {
                    if (lbf.isSingleton(name))
                    {
                        Object bean = lbf.getBean(name);
                        if (bean instanceof Startable)
                        {
                            try
                            {
                                ((Startable) bean).start();
                            }
                            catch (Exception e)
                            {
                                log.error("Error occurred while starting component '" + bean.getClass().getName() + "'. " + e.getMessage(), e);
                            }
                        }
                    }
                } 
                catch (BeanIsAbstractException ex)
                {
                    // skipping abstract beans (is there a better way to check for this?)
                }
            }
        }
	}
}
