package com.atlassian.jira.web.tags.i18n;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import webwork.view.taglib.ParamTag;
import webwork.view.taglib.WebWorkBodyTagSupport;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JSP {@link javax.servlet.jsp.tagext.Tag} that is able to output localised text given an i18n key.
 * <br/><br/>
 * The name of the key is written to the page in a &lt;span&gt; tag if the <em>includeMetaData</em> attribute is set to
 * <em>true</em>.
 */
public class MessageTag extends WebWorkBodyTagSupport implements ParamTag.UnnamedParametric
{
    private static final String I18N_METADATA_OUTPUT_DEFAULT_VALUE = "true";
    private String key;

    private boolean includeMetaData = false;

    private List<Object> arguments = new ArrayList<Object>();

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public boolean isIncludeMetaData()
    {
        return includeMetaData;
    }

    public void setIncludeMetaData(boolean includeMetaData)
    {
        this.includeMetaData = includeMetaData;
    }

    public void addArgument(Object argument)
    {
        arguments.add(argument);
    }

    @Override
    public void addParameter(String name, Object value)
    {
        addArgument(value);
    }

    @Override
    public void addParameter(Object value)
    {
        addArgument(value);
    }

    @Override
    public int doStartTag() throws JspException
    {
        arguments.clear();
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException
    {
        final String i18nKey = resolveKeyFromValueStack();
        final String message = getI18nHelper().getText(i18nKey, arguments);
        write(message, i18nKey);

        return EVAL_PAGE;
    }

    private String resolveKeyFromValueStack()
    {
        final String resolvedKey = findString(key);
        if (resolvedKey != null)
        {
            return resolvedKey;
        }
        return key;
    }

    private void write(String message, String i18nKey) throws JspTagException
    {
        try
        {
            if (shouldIncludeI18nMetaData())
            {
                pageContext.getOut().write("<span data-i18nKey="+ i18nKey + ">");
                pageContext.getOut().write(message);
                pageContext.getOut().write("</span>");
            }
            else
            {
                pageContext.getOut().write(message);
            }
        }
        catch(IOException ioe)
        {
            throw new JspTagException(ioe);
        }
    }

    private boolean shouldIncludeI18nMetaData()
    {
        return (includeMetaData && isI18nMetaDataOutputOn());
    }

    private boolean isI18nMetaDataOutputOn()
    {
        return Boolean.parseBoolean(getI18nMetaDataOutputProperty() == null ?
                I18N_METADATA_OUTPUT_DEFAULT_VALUE : getI18nMetaDataOutputProperty());
    }

    private String getI18nMetaDataOutputProperty()
    {
        return ComponentAccessor.getComponentOfType(ApplicationProperties.class).
                getDefaultBackedText(APKeys.JIRA_I18N_INCLUDE_META_DATA);
    }

    I18nHelper getI18nHelper()
    {
        return ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class).getI18nHelper();
    }
}
