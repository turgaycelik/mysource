package com.atlassian.jira.web.pagebuilder;

import java.io.Writer;

/**
 * Representation of a page that will be decorated. This class contains methods for extracting information about the
 * page such as title, meta properties, body tag properties, as well as for rendering the head and body content.
 *
 * @since v6.1
 */
public interface DecoratablePage
{
    public String getTitle();
    public String getMetaProperty(String key);
    public String getBodyTagProperty(String key);
    public String getPageProperty(String key);
    public void writeHead(Writer writer);
    public void writeBody(Writer writer);

    interface ParsedHead
    {
        String getTitle();
        String getMetaProperty(String key);
    }

    interface ParsedBody
    {
        String getBodyTagProperty(String key);
        String getPageProperty(String key);
    }
}
