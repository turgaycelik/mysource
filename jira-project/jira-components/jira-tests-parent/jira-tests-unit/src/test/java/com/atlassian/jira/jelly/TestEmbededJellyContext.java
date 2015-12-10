/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.jelly.service.EmbededJellyContext;

import org.apache.commons.jelly.JellyContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestEmbededJellyContext
{
    private final String expectedOutput = "<some_sample_output>Hello</some_sample_output><some_sample_output>Hello</some_sample_output>";
    private final String jellyScriptHeader = "<?xml version=\"1.0\"?>" + "<j:jelly xmlns:j=\"jelly:core\"> ";
    private final String jellyScriptFooter = "</j:jelly>";
    private final String goodJellyScript = jellyScriptHeader + expectedOutput + jellyScriptFooter;
    private final String badJellyScript = jellyScriptHeader + "<j:choose yoplait=\"5\"/>" + jellyScriptFooter;

    @Before
    public void setUp() throws Exception
    {
        JiraSystemProperties.resetReferences();
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "false");
    }

    @After
    public void tearDown() throws Exception
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "false");
        JiraSystemProperties.resetReferences();
    }

    @Test
    public void testUnableToRun()
    {
        EmbededJellyContext jelly = new EmbededJellyContext();
        Writer writer = new StringWriter();

        try
        {
            jelly.runScript(goodJellyScript, writer);
            fail("Should have thrown an exception");
        }
        catch (Exception ignore)
        {
        }
    }

    @Test
    public void testRunBadScriptAsString()
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");

        EmbededJellyContext jelly = new EmbededJellyContext();
        Writer writer = new StringWriter();

        try
        {
            jelly.runScript(badJellyScript, writer);
            fail("Should have thrown an exception");
        }
        catch (Exception ignore)
        {
        }
    }

    @Test
    public void testRunGoodScriptAsString() throws Exception
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");

        EmbededJellyContext jelly = new EmbededJellyContext();
        StringWriter writer = new StringWriter();
        final JellyContext jellyContext = jelly.runScript(goodJellyScript, writer);

        assertNotNull(jellyContext);
        assertEquals(expectedOutput, writer.toString());
    }

    @Test
    public void testRunBadScript() throws IOException
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");

        EmbededJellyContext jelly = new EmbededJellyContext();
        try
        {
            runJellyScript(jelly, badJellyScript, "outputtest.xml");
            fail("Should have thrown an exception");
        }
        catch (Exception ignore)
        {
        }
    }

    @Test
    public void testRunGoodScript() throws Exception
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");

        EmbededJellyContext jelly = new EmbededJellyContext();
        final String outputFilename = "outputtest.xml";
        final File outputF = new File(outputFilename);
        outputF.deleteOnExit();
        
        final JellyContext jellyContext = runJellyScript(jelly, goodJellyScript, outputFilename);
        assertNotNull(jellyContext);

        File outFile = new File(outputFilename);
        final FileReader fileReader = new FileReader(outFile);
        int length = (int) outFile.length();
        char[] buff = new char[length];
        fileReader.read(buff);

        String output = new String(buff);
        assertEquals(expectedOutput, output);
    }

    private JellyContext runJellyScript(EmbededJellyContext jelly, String script, String outputFilename) throws Exception
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");

        final String jellyFileName = "jsc.jelly";
        File file = new File(jellyFileName);
        FileWriter of = new FileWriter(file);
        of.write(script);
        of.close();
        try
        {
            return jelly.runScript(jellyFileName, outputFilename);
        }
        finally
        {
            file.delete();
        }
    }
}
