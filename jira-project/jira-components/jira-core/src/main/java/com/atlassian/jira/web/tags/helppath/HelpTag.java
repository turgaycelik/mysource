package com.atlassian.jira.web.tags.helppath;

import com.atlassian.jira.web.util.HelpUtil;
import webwork.view.taglib.WebWorkTagSupport;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Template class for {@link com.atlassian.jira.web.util.HelpUtil.HelpPath}-related tags.
 */
public abstract class HelpTag extends WebWorkTagSupport
{
    /**
     * The HelpUtil to look up paths from.
     */
    protected final HelpUtil helpUtil;

    /**
     * The key to look up.
     */
    private String key;

    public HelpTag()
    {
        this(HelpUtil.getInstance());
    }

    public HelpTag(HelpUtil helpUtil)
    {
        this.helpUtil = helpUtil;
    }

    public final void setKey(String key)
    {
        this.key = key;
    }

    @Override
    public final int doEndTag() throws JspException
    {
        HelpUtil.HelpPath path = helpUtil.getHelpPath(key);
        if (path != null)
        {
            try
            {
                writeContent(pageContext.getOut(), path);
            }
            catch (IOException e)
            {
                throw new JspException("Error writing string for HelpPath " + path.getKey(), e);
            }
        }

        key = null;
        return super.doEndTag();
    }

    /**
     * Writes the tag's value the given JspWriter. Implementations should perform their own HTML escaping if necessary.
     *
     * @param out a JspWriter
     * @param path a HelpPath
     */
    protected abstract void writeContent(final JspWriter out, HelpUtil.HelpPath path) throws IOException;
}
