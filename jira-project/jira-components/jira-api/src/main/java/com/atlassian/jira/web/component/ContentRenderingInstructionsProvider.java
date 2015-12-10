package com.atlassian.jira.web.component;

import java.util.Map;

/**
 * Provides web component rendering instructions based on the current context.
 */
public interface ContentRenderingInstructionsProvider
{
    /**
     * Provides web component rendering instructions based on the current context.
     * The context is the same which would be passed to the web component context provider
     * during this rendering cycle.
     *
     * @param context rendering context
     * @return web component rendering instruction
     */
    ContentRenderingInstruction getInstruction(Map<String, Object> context);
}
