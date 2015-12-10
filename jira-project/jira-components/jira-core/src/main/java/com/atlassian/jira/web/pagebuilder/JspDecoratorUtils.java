package com.atlassian.jira.web.pagebuilder;

import com.atlassian.jira.web.HttpRequestLocal;

/**
 * Static utilities for accessing values from the current request's decorated page, eg from within a JSP.
 *
 * @since v6.1
 */
public class JspDecoratorUtils
{
    private static HttpRequestLocal<DecoratablePage.ParsedHead> requestLocalParsedHead =
            new HttpRequestLocal<DecoratablePage.ParsedHead>(JspDecoratorUtils.class.getName());

    private static HttpRequestLocal<DecoratablePage.ParsedBody> requestLocalParsedBody =
            new HttpRequestLocal<DecoratablePage.ParsedBody>(JspDecoratorUtils.class.getName());

    protected static void setParsedHead(DecoratablePage.ParsedHead parsedHead)
    {
        requestLocalParsedHead.set(parsedHead);
    }

    protected static void clearParsedHead()
    {
        requestLocalParsedHead.remove();
    }

    protected static void setParsedBody(DecoratablePage.ParsedBody parsedBody)
    {
        requestLocalParsedBody.set(parsedBody);
    }

    protected static void clearParsedBody()
    {
        requestLocalParsedBody.remove();
    }

    /**
     * Returns the parsed head from the current decorated page
     * @return decorated page's parsed head
     */
    public static DecoratablePage.ParsedHead getHead()
    {
        return requestLocalParsedHead.get();
    }

    /**
     * Returns the parsed body from the current decorated page
     * @return decorated page's parsed body
     */
    public static DecoratablePage.ParsedBody getBody()
    {
        return requestLocalParsedBody.get();
    }
}
