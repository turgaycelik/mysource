/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.AbstractService;
import org.apache.log4j.Logger;

/**
 * A simple debugging service, that prints to the JIRA logs every time it is called.
 */
public class DebugService extends AbstractService
{
    private static final Logger log = Logger.getLogger(DebugService.class);

    public void run()
    {
        log.debug("DebugService.run");
    }

    public DebugService()
    {
        log.debug("DebugService.DebugService");
    }

    public void destroy()
    {
        log.debug("DebugService.destroy");
        super.destroy();
    }

    public String getDescription()
    {
        return "A simple debugging service, that prints to the JIRA logs every time it is called.";
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("DEBUGSERVICE", "services/com/atlassian/jira/service/services/debugservice.xml", null);
    }
}
