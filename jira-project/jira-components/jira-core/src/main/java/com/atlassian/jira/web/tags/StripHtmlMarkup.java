package com.atlassian.jira.web.tags;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import webwork.view.taglib.WebWorkBodyTagSupport;

public class StripHtmlMarkup extends WebWorkBodyTagSupport
{
    private static final Logger LOGGER = Logger.getLogger(StripHtmlMarkup.class);

    private String expression;

    public void setValue(final String expression)
    {
        this.expression = Strings.nullToEmpty(expression);
    }

    @Override
    public int doEndTag()
    {
        final String html = resolveValue();
        final String htmlStrippedContent = stripAllHtml(html);
        writeValue(htmlStrippedContent);
        return EVAL_PAGE;
    }

    @Nonnull
    private String resolveValue()
    {
        return Strings.nullToEmpty(findString(expression));
    }

    @Nonnull
    private String stripAllHtml(@Nonnull final String html)
    {
        return Jsoup.clean(html, Whitelist.none());
    }

    private void writeValue(@Nonnull final String encodedValue)
    {
        try
        {
            pageContext.getOut().write(encodedValue);
        }
        catch (Exception e)
        {
            LOGGER.warn("Failed to strip markup from a given expression; exception message: " + e.getMessage());
            LOGGER.debug("Failed to strip markup from expression: " + expression, e);
        }
    }
}
