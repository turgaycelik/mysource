package com.atlassian.jira.web.pagebuilder;

/**
 * Interface for accessing page builders
 * @since v6.1
 */
public interface JiraPageBuilderService extends com.atlassian.webresource.api.assembler.PageBuilderService
{
    /** Dark feature key for sending the head early */
    public static final String SEND_HEAD_EARLY_FEATURE_KEY = "com.atlassian.plugins.SEND_HEAD_EARLY";

    /**
     * Gets the page builder for the current request
     * @throws IllegalStateException if no page builder has been set for the current request
     * @return request-local page builder
     */
    public PageBuilder get();
}
