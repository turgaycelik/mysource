package com.atlassian.jira.web.tags.helppath;

import com.atlassian.jira.web.util.HelpUtil;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;

/**
 * A WebWork {@code <help-title>} tag.
 *
 * @see com.atlassian.jira.web.util.HelpUtil.HelpPath#getTitle()
 */
public class HelpTitleTag extends HelpTag
{
    @Override
    protected void writeContent(final JspWriter out, final HelpUtil.HelpPath path) throws IOException
    {
        out.write(StringEscapeUtils.escapeHtml4(path.getTitle()));
    }
}
