/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.util;

import com.google.common.collect.Lists;
import com.mindprod.csv.CSVReader;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * A tag to parse a CSV file into a Jelly variable.
 * <p/>
 * Usage:<br>
 * &lt;jira:csv var="foo" file="c:\temp\bar.csv" /&gt;
 * <p/>
 * 'foo' will then be a two dimensional array representing the rows and columns in the CSV file.
 */
public class CsvTag extends TagSupport
{
    private String var;
    private File file;
    private String uri;

    public void doTag(final XMLOutput xmlOutput) throws JellyTagException
    {
        if (var == null)
        {
            throw new MissingAttributeException("This tag must have a 'var' attribute to store the CSV object in");
        }

        if (file == null && uri == null)
        {
            throw new JellyTagException("This tag must have a 'file' or 'uri' specified");
        }

        final Reader reader;
        if (file != null)
        {
            if (!file.exists())
            {
                throw new JellyTagException("The file: " + file + " does not exist");
            }

            try
            {
                reader = new FileReader(file);
            }
            catch (FileNotFoundException e)
            {
                throw new JellyTagException("could not find the file", e);
            }
        }
        else
        {
            final InputStream in = context.getResourceAsStream(uri);
            if (in == null)
            {
                throw new JellyTagException("Could not find uri: " + uri);
            }

            // @todo should we allow an encoding to be specified?
            reader = new InputStreamReader(in);
        }

        final CSVReader csv = new CSVReader(reader);
        final List<List<String>> allRows = Lists.newArrayList();
        try
        {
            while (true)
            {
                final List<String> row = Arrays.asList(csv.getAllFieldsInLine());
                allRows.add(row);
            }
        }
        catch (EOFException e)
        {
            context.setVariable(var, allRows);
        }
        catch (IOException e)
        {
            throw new JellyTagException(e);
        }
    }

    public String getVar()
    {
        return var;
    }

    public void setVar(String var)
    {
        this.var = var;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }
}
