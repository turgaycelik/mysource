package com.atlassian.jira.web.tags;

import com.atlassian.jira.web.Renderable;
import webwork.view.taglib.WebWorkTagSupport;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Tag for rendering renderables
 *
 * @since v5.0
 */
public class RenderTag extends WebWorkTagSupport
{
    private String value;

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public void release()
    {
        super.release();
        // JSP tags must null out fields because they may be reused
        value = null;
    }

    @Override
    public int doStartTag() throws JspException
    {
        Object value = findValue(this.value);
        if (value != null)
        {
            if (!(value instanceof Renderable))
            {
                // Write out the value as a string
                throw new JspException("Value passed to render tag must be of type Renderable, was " + value.getClass());
            }
            try
            {
                ((Renderable) value).render(pageContext.getOut());
            }
            catch (IOException e)
            {
                throw new JspException("Error rendering renderable", e);
            }
        }
        return SKIP_BODY;
    }
}
