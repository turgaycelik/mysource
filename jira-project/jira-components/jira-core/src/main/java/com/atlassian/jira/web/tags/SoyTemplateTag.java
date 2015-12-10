package com.atlassian.jira.web.tags;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.util.ContainUtil;
import webwork.view.taglib.ParamTag;
import webwork.view.taglib.WebWorkBodyTagSupport;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to call a soy template
 *
 * @since v6.0
 */
public class SoyTemplateTag
        extends WebWorkBodyTagSupport
        implements ParamTag.Parametric
{
    private static final Logger log = LoggerFactory.getLogger(SoyTemplateTag.class);

    // Attributes ----------------------------------------------------
    protected String template;
    protected Map<String,Object> params = new HashMap<String,Object>();
    protected String completeModuleKey; // TODO: Use ModuleDescriptor?

    // Public --------------------------------------------------------

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public String getModuleKey()
    {
        return completeModuleKey;
    }

    public void setModuleKey(String completeModuleKey)
    {
        this.completeModuleKey = completeModuleKey;
    }

    public void addParameter(String name, Object value)
    {
        addParameterInternal(name, value);
    }

    private void addParameterInternal(String name, Object value)
    {
        this.params.put(name, value);
    }

    public Map<String, Object> getParameters()
    {
        return params;
    }

    public boolean memberOf(Object obj1, Object obj2)
    {
        return ContainUtil.contains(obj1, obj2);
    }

    // IncludeTag overrides ------------------------------------------
    public int doEndTag()
            throws JspException
    {
        final String id = removeSingleQuotes(getId());
        final String template = findString(getTemplate());
        final String moduleKey = findString(getModuleKey());

        if (StringUtils.isEmpty(moduleKey))
        {
            throw new JspTagException("Failed to render soy tag; "
                    + String.format("moduleKey attribute's value of \"%s\" evaluated to an empty string.", getModuleKey())
                    + "\nPerhaps you need to enclose the value in single quotes?");
        }

        if (StringUtils.isEmpty(template))
        {
            throw new JspTagException("Failed to render soy tag; "
                    + String.format("template attribute's value of \"%s\" evaluated to an empty string.", getTemplate())
                    + "\nPerhaps you need to enclose the value in single quotes?");
        }

        if (!StringUtils.isEmpty(id))
        {
            addParameterInternal("id", id);
        }

        getStack().pushValue(this);

        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Using template '%s' from module '%s'", template, moduleKey);
            }

            final String output = getSoyRenderer().render(moduleKey, template, getParameters());
            final JspWriter jspWriter = pageContext.getOut();

            jspWriter.write(output);

            return EVAL_PAGE;
        }
        catch (SoyException e)
        {
            throw new JspTagException("Soy rendering failed for template '%s'.", e);
        }
        catch (IOException e)
        {
            throw new JspTagException("Failed to render soy tag, using template '%s' from module '%s'.", e);
        }
        finally
        {
            getStack().popValue();
            params = new HashMap<String,Object>();
        }
    }

    /**
     * Removes the prefix and suffix single quotes ' from the value.  This is so that non evaluated attributes like id
     * can be specified as "'myid'" and still have the value "myid"
     *
     * @param s the string in play
     * @return s without quotes
     */
    private String removeSingleQuotes(final String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }
        int start = 0;
        int end = s.length()-1;
        if (s.charAt(start) == '\'')
        {
            start++;
        }
        if (s.charAt(end) == '\'')
        {
            end--;
        }
        if (start == end)
        {
            return ""; // all quotes
        }
        return s.substring(start,end+1);
    }

    protected SoyTemplateRenderer getSoyRenderer()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(SoyTemplateRenderer.class);
    }
}
