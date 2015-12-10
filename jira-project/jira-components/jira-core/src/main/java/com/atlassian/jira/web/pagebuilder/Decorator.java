package com.atlassian.jira.web.pagebuilder;

import java.io.Writer;

/**
 * Interface for a page decorator.
 * @since v6.1
 */
public interface Decorator
{
    public void writePreHead(Writer writer);
    public void writeOnFlush(Writer writer);
    public void writePostHead(Writer writer, DecoratablePage.ParsedHead parsedHead);
    public void writePreBody(Writer writer, DecoratablePage.ParsedBody parsedBody);
    public void writePostBody(Writer writer, DecoratablePage.ParsedBody parsedBody);
}
