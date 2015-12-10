/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.jelly.JiraJelly;
import com.atlassian.jira.service.AbstractService;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

/**
 * Responsible for periodically running a Jelly script.
 */
public class JellyService extends AbstractService
{
    private static final Logger LOGGER = Logger.getLogger(JellyService.class);
    private static final String KEY_INPUT_FILE = "input-file";
    private static final String KEY_OUTPUT_FILE = "output-file";

    private String outputFilename = null;
    private String inputFilename = null;
    EmbededJellyContext jelly = new EmbededJellyContext();

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("JELLYSERVICE", "services/com/atlassian/jira/service/services/jelly/jellyservice.xml", null);
    }

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);
        outputFilename = getProperty(KEY_OUTPUT_FILE);
        inputFilename = getProperty(KEY_INPUT_FILE);
        LOGGER.info("JellyService.init : " + KEY_INPUT_FILE + "=" + inputFilename + " " + KEY_OUTPUT_FILE + "=" + outputFilename);
    }

    public void run()
    {
        LOGGER.info("JellyService.run");
        if (inputFilename == null || outputFilename == null)
        {
            if (inputFilename == null) LOGGER.warn("Jelly service not running - " + KEY_INPUT_FILE + " not specified");
            if (outputFilename == null) LOGGER.warn("Jelly service not running - " + KEY_OUTPUT_FILE + " not specified");
            return;
        }
        if (JiraJelly.allowedToRun())
        {
            try
            {
                jelly.runScript(inputFilename, outputFilename);
            }
            catch (Exception e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }
        else
        {
            LOGGER.error(JiraJelly.JELLY_NOT_ON_MESSAGE);
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
        }
    }
}
