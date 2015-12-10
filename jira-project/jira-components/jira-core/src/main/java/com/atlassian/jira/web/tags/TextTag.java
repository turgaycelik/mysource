/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.tags;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.I18nHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import webwork.view.taglib.ParamTag;
import webwork.view.taglib.WebWorkBodyTagSupport;

/**
 * This JSP tag is used in the &lt;ww:text tags all throughout JIRA.
 * <p/>
 * Since it is {@link ParamTag.Parametric} it supports taking parameters as child tag elements via &lt;ww:param.
 * <p/>
 * It calls back to {@link com.atlassian.jira.security.JiraAuthenticationContext#getI18nHelper()} to do its work
 */
public class TextTag extends WebWorkBodyTagSupport implements ParamTag.UnnamedParametric
{
    private static final Logger log = Logger.getLogger(TextTag.class);

    private String nameAttr;
    private String value0Attr;
    private String value1Attr;
    private String value2Attr;
    private String value3Attr;
    private ArrayList<Object> values;

    public void setName(String aName)
    {
        nameAttr = aName;
    }

    public void setValue0(String aName)
    {
        value0Attr = aName;
    }

    public void setValue1(String aName)
    {
        value1Attr = aName;
    }

    public void setValue2(String aName)
    {
        value2Attr = aName;
    }

    public void setValue3(String aName)
    {
        value3Attr = aName;
    }

    public void addParameter(String aName, Object aValue)
    {
        addParameter(aValue);
    }

    public void addParameter(Object aValue)
    {
        if (aValue == null)
        {
            return;
        }
        if (values == null)
        {
            values = new ArrayList<Object>();
        }
        values.add(aValue);
    }

    // JIRA implementation ----------------------------------------
    public int doEndTag() throws JspException
    {
        try
        {
            addAttributeProperties();

            final String msgKey = determineMsgKey(nameAttr);
            if (StringUtils.isNotBlank(msgKey))
            {
                final Object[] substitutionValues = values == null ? new String[0] : values.toArray();

                final String formattedText = getI18nHelper().getText(msgKey, substitutionValues);
                safelyWrite(formattedText, msgKey);
            }
            else
            {
                // originally I threw
                HttpServletRequest httpServletRequest = (HttpServletRequest) pageContext.getRequest();
                log.info("An empty i18n key was provided in " + httpServletRequest.getRequestURI());
            }
            return EVAL_PAGE;
        }
        finally
        {
            clearTagState();
        }
    }

    /**
     * This seems arse about face in that attributes are added after any ParamTag values but in practice we don't mix
     * the two so its left here as is for legacy code reasons
     */
    private void addAttributeProperties()
    {
        if (value0Attr != null)
        {
            addParameter(findValue(value0Attr));
        }
        if (value1Attr != null)
        {
            addParameter(findValue(value1Attr));
        }
        if (value2Attr != null)
        {
            addParameter(findValue(value2Attr));
        }
        if (value3Attr != null)
        {
            addParameter(findValue(value3Attr));
        }
    }

    private String determineMsgKey(final String nameAttr)
    {
        String msgKey;
        try
        {
            msgKey = findString(nameAttr);
            if (msgKey == null)
            {
                msgKey = nameAttr;
            }
        }
        catch (Exception e)
        {
            msgKey = nameAttr;
        }
        return msgKey;
    }

    private void clearTagState()
    {
        nameAttr = value0Attr = value1Attr = value2Attr = value3Attr = null;
        values = null;
    }

    private I18nHelper getI18nHelper()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    //internal Atlassian debugging of i18n'd strings
    private static final boolean HIGHLIGHT = JiraSystemProperties.getInstance().getBoolean("jira.i18n.texthighlight");

    private void safelyWrite(String formattedText, String msgKey) throws JspException
    {
        try
        {
            write(formattedText, msgKey);
        }
        catch (IOException rootCause)
        {
            throw new JspException(rootCause);
        }
    }

    private void write(String string, String msgKey) throws IOException
    {
        JspWriter jspWriter = pageContext.getOut();
        if (HIGHLIGHT)
        {
            jspWriter.write("<span class='replaced' data-i18n='" + msgKey + "'>");
            jspWriter.write(string);
            jspWriter.write("</span>");
        }
        else
        {
            jspWriter.write(string);
        }
    }
}
