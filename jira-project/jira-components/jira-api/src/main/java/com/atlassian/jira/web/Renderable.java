package com.atlassian.jira.web;

import java.io.IOException;
import java.io.Writer;

/**
 * Some renderable content that can be rendered by a render tag.
 *
 * Renderables are rendered straight to an HTML stream, no attempt is made by the caller to escape the content written,
 * hence it is the responsibility of the implementor to ensure that any user entered content is escaped.
 *
 * A renderable could be invoked many times, and hence should not consumer any resources it can't recreate.
 *
 * @since v5.0
 */
public interface Renderable
{
    /**
     * Render this content to the given writer.
     *
     * @param writer The writer to render to
     * @throws IOException If an error occured
     */
    void render(Writer writer) throws IOException;
}
