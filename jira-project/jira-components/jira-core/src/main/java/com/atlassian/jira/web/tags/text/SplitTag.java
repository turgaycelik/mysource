package com.atlassian.jira.web.tags.text;

import com.google.common.base.Splitter;
import webwork.view.taglib.WebWorkTagSupport;

import javax.servlet.jsp.JspException;

/**
 * Represents a JSP {@link javax.servlet.jsp.tagext.Tag} that is able to divide a {@code String} into substrings, by
 * recognizing a <i>separator</i> (a.k.a. "delimiter") and make them available through an {@link java.util.Iterator}
 * that is pushed to the top of the {@code ValueStack}.
 *
 * @since v5.0.7
 */
public class SplitTag extends WebWorkTagSupport
{
    private String value;

    private String separator;

    /**
     * Gets the specified String to be split into sub-components.
     *
     * @return A String containing the text that will be split into sub-components.
     */
    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * Gets the separator to be used to recognise the substrings in the string to be split.
     *
     * @return A String containing the separator to be used to recognise the substrings in the string to be split.
     */
    public String getSeparator()
    {
        return separator;
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    @Override
    public int doEndTag() throws JspException
    {
        final Iterable<String> splitResult =
                Splitter.on(separator).trimResults().split((CharSequence) findValue(getValue()));

        getStack().pushValue(splitResult.iterator());
        return super.doEndTag();
    }
}
