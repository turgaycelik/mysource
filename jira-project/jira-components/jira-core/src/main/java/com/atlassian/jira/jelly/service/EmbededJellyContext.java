/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.service;

import com.atlassian.jira.jelly.JiraJelly;
import com.atlassian.jira.jelly.WebWorkAdaptor;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

public class EmbededJellyContext extends JellyContext
{
    private final Logger log = Logger.getLogger(getClass());
    private final WebWorkAdaptor webWorkAdaptor = new WebWorkAdaptor();

    public EmbededJellyContext()
    {
        setUseContextClassLoader(true);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="PATH_TRAVERSAL_OUT", justification = "outputFilename is taken from a system configuration file.")
    public JellyContext runScript(String jellyFilename, String outputFilename) throws Exception
    {
        JellyContext jellyContext = null;
        if (jellyFilename == null) throw new IllegalArgumentException("null Jelly filename");
        if (outputFilename == null) throw new IllegalArgumentException("null output filename");
        File jellyScriptFile = new File(jellyFilename);
        if (!jellyScriptFile.exists())
        {
            return null;
        }

        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(outputFilename);

            XMLOutput xmlOutput = XMLOutput.createXMLOutput(fileWriter);
            try
            {
                jellyContext = runScript(jellyScriptFile, xmlOutput);
            }
            catch (JellyException e)
            {
                // Write the message to the output file and throw up the error
                // so it goes to the console as well
                fileWriter.write("\n"+e.getMessage());
                throw e;
            }
        }
        finally
        {
            try
            {
                if (fileWriter != null)
                    fileWriter.close();
            }
            catch (IOException ignore)
            {
            }
        }
        return jellyContext;
    }

    public JellyContext runScriptAsString(String scriptAsString, XMLOutput output) throws JellyServiceException
    {
        if (JiraJelly.allowedToRun())
        {
            try
            {
                XMLParser parse = new XMLParser();
                Script script = parse.parse(new StringReader(scriptAsString));
                JellyContext newJellyContext = new JellyContext(this);

                if (log.isDebugEnabled())
                {
                    log.debug("running script : " + scriptAsString);
                }

                script.run(newJellyContext, output);
                return newJellyContext;

            }
            catch(Exception e)
            {
                throw new JellyServiceException(e);
            }
        }
        else
        {
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
        }
    }

    public JellyContext runScript(String scriptAsString, Writer outputWriter) throws JellyServiceException
    {
       return runScriptAsString(scriptAsString, XMLOutput.createXMLOutput(outputWriter));
    }

    public JellyContext runScript(File file, XMLOutput xmlOutput) throws JellyException
    {
        if (JiraJelly.allowedToRun())
            return super.runScript(file, xmlOutput);
        else
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
    }

    public JellyContext runScript(URL url, XMLOutput xmlOutput) throws JellyException
    {
        if (JiraJelly.allowedToRun())
            return super.runScript(url, xmlOutput);
        else
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
    }

    public JellyContext runScript(String string, XMLOutput xmlOutput) throws JellyException
    {
        if (JiraJelly.allowedToRun())
            return super.runScript(string, xmlOutput);
        else
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
    }

    public JellyContext runScript(String string, XMLOutput xmlOutput, boolean b, boolean b1) throws JellyException
    {
        if (JiraJelly.allowedToRun())
            return super.runScript(string, xmlOutput);
        else
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
    }

    public JellyContext runScript(File file, XMLOutput xmlOutput, boolean b, boolean b1) throws JellyException
    {
        if (JiraJelly.allowedToRun())
            return super.runScript(file, xmlOutput, b, b1);
        else
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
    }

    public JellyContext runScript(URL url, XMLOutput xmlOutput, boolean b, boolean b1) throws JellyException
    {
        if (JiraJelly.allowedToRun())
            return super.runScript(url, xmlOutput, b, b1);
        else
            throw new UnsupportedOperationException(JiraJelly.JELLY_NOT_ON_MESSAGE);
    }

    public WebWorkAdaptor getWebWorkAdaptor()
    {
        return webWorkAdaptor;
    }
}
