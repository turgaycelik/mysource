package com.atlassian.jira.web.pagebuilder;

/**
 * Interface for working with decoration when rendering an HTML page.
 *
 * @since v6.1
 */
public interface PageBuilder
{
    /**
     * Sets the decorated page. This can be called any number of times before the first call to flush.
     * @param decorator Decorator for the current request.
     * @throws IllegalStateException if the page has already been flushed
     */
    public void setDecorator(Decorator decorator);

    /**
     * Flushes as much information as possible to the current HTTP response. This may be called multiple times in a
     * single request.
     * @throws IllegalStateException if the page has not been flushed
     */
    public void flush();
}
