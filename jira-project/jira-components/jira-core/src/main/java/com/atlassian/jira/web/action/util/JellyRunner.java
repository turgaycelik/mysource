/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jelly.JiraJelly;
import com.atlassian.jira.jelly.service.EmbededJellyContext;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import electric.xml.Document;
import electric.xml.ParseException;
import electric.xml.sax.SAXParser;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.parser.XMLParser;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import webwork.util.TextUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

@WebSudoRequired
public class JellyRunner extends JiraWebActionSupport
{
    private static final String newLine = "<BR>";
    private static final Logger log = Logger.getLogger(JellyRunner.class);
    private String script = getDefaultString();
    private final StringWriter stringWriter = new StringWriter();
    private String filename = null;

    protected void doValidation()
    {
        // Check if both items are not set
        if (!(TextUtils.stringSet(getScript()) || TextUtils.stringSet(getFilename())))
        {
            addErrorMessage(getText("admin.errors.jellyrunner.provide.path"));
        }
        else if (TextUtils.stringSet(getScript()) && TextUtils.stringSet(getFilename()))
        {
            addErrorMessage(getText("admin.errors.jellyrunner.path.or.script"));
        }
        else if (TextUtils.stringSet(getFilename()))
        {
            // Check that the filename exists, is not a directory and is readable
            File jellyFile = new File(getFilename());
            if (!(jellyFile.exists() && jellyFile.isFile() && jellyFile.canRead()))
            {
                addError("filename", getText("admin.errors.jellyrunner.file.does.not.exist"));
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (isAllowedToRun())
        {
            EmbededJellyContext embededJellyContext = new EmbededJellyContext();

            final User remoteUser = getLoggedInUser();
            if (remoteUser != null)
            {
                embededJellyContext.setVariable(JellyTagConstants.USERNAME, remoteUser.getName());
            }

            XMLOutput xmlOutput = XMLOutput.createXMLOutput(getStringWriter(), false);
            XMLParser xmlParser = new XMLParser(new SAXParser());
            xmlParser.setContext(embededJellyContext);
            xmlParser.setValidating(true);
            try
            {
                final Script script = getJellyScript(xmlParser);
                try
                {
                    final Script compiledScript = script.compile();

                    try
                    {
                        compiledScript.run(embededJellyContext, xmlOutput);
                    }
                    catch (JellyTagException e)
                    {
                        addError(getText("admin.errors.could.not.run.script"), getStringWriter(), e);
                    }
                    catch (RuntimeException e)
                    {
                        addError(getText("admin.errors.exception") + " " + e.toString(), null, e);
                    }
                }
                catch (JellyException e)
                {
                    addError(getText("admin.errors.could.not.compile.script"), null, e);
                }
            }
            catch (IOException e)
            {
                addError(getText("admin.errors.could.not.read.script.string"), null, e);
            }
            catch (SAXException e)
            {
                addError(getText("admin.errors.xml.script.invalid"), null, e);
            }
            if (invalidInput())
            {
                return ERROR;
            }
            else
            {
                return SUCCESS;
            }
        }
        else
        {
            //addErrorMessage(JiraJelly.JELLY_NOT_ON_MESSAGE);
            addErrorMessage(getText("admin.errors.jellyrunner.not.on"));
            return ERROR;
        }
    }

    private Script getJellyScript(XMLParser xmlParser) throws IOException, SAXException
    {
        Script script;
        if (TextUtils.stringSet(getScript()))
        {
            script = xmlParser.parse(new StringReader(getScript()));
        }
        else if (TextUtils.stringSet(getFilename()))
        {
            script = xmlParser.parse(new File(getFilename()));
        }
        else
        {
            // should never happen as errors are detected in the doValidation() method
            script = null;
        }
        return script;
    }

    public boolean isAllowedToRun()
    {
        return JiraJelly.allowedToRun();
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = trimNullSafe(script);
    }

    public String getResult()
    {
        String xmlOutputString = getStringWriter().toString().trim();
        if (xmlOutputString.length() > 0)
        {
            try
            {
                Document document = new Document(xmlOutputString);
                if (document.getRoot().hasChildNodes())
                {
                    return TextUtils.plainTextToHtml(document.toString());
                }
                else
                {
                    return null;
                }
            }
            catch (ParseException e)
            {
                return "Output was not valid xml." + newLine + TextUtils.plainTextToHtml(getStringWriter().toString());
            }
        }
        else
        {
            return null;
        }
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String fileName)
    {
        this.filename = trimNullSafe(fileName);
    }

    /**
     * Trims the given String and returns the result. Return null if given String is null.
     *
     * @param s String to trim
     * @return trimmed String or null
     */
    private String trimNullSafe(String s)
    {
        return s == null ? null : s.trim();
    }

    private void addError(String message, StringWriter jellyOutput, Exception e)
    {
        String jellyOutputStr = "";
        if (jellyOutput != null)
        {
            jellyOutputStr = "<b>Error:</b> " + TextUtil.escapeHTML(jellyOutput.toString()) + newLine;
        }
        try
        {
            log.error(message, e);
            addError("script", message);
            addError("scriptException", jellyOutputStr + "<b>Exception:</b> " + e.toString() + newLine + getStackTrace(e));
        }
        catch (Exception e1)
        {
            log.error("Exception adding error: " + e1, e1);
        }
    }

    private String getStackTrace(Exception e)
    {
        PrintWriter printWriter = new PrintWriter(new StringWriter());
        e.printStackTrace(printWriter);
        return printWriter.toString();
    }

    private StringWriter getStringWriter()
    {
        return stringWriter;
    }

    private String getDefaultString()
    {
        try
        {
            final String script = "/jelly-defaultscript-ent.xml";
            return IOUtil.toString(JellyRunner.class.getResourceAsStream(script));
        }
        catch (Exception e)
        {
            log.error(e);
            return "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n\n</JiraJelly>";
        }
    }

}
