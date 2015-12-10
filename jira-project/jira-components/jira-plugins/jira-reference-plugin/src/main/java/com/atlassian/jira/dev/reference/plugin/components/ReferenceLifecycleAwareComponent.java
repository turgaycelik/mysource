package com.atlassian.jira.dev.reference.plugin.components;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * This document may provide more background on the plugin lifecycle: https://developer.atlassian.com/display/JIRADEV/JIRA+Plugin+Lifecycle
 *
 * @since v5.2
 */
public class ReferenceLifecycleAwareComponent implements LifecycleAware
{
    private static final Logger log = Logger.getLogger(ReferenceLifecycleAwareComponent.class);

    @PostConstruct
    public void onSpringContextStarted()
    {
        log.info("@PostConstruct - The Sprint Context has started. Now would be a good time to register this component with the EventPublisher.");
    }

    @PreDestroy
    public void onSpringContextStopped()
    {
        log.info("@PreDestroy - The Sprint Context has stopped. Don't forget to unregister this component with the EventPublisher.");
    }

    @Override
    public void onStart()
    {
        log.info("onStart() - If JIRA is starting up, then the plugin system has completely started. If the reference plugin is being enabled/installed, not all plugin modules have been enabled yet!");
    }
}
