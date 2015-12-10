package com.atlassian.jira.web.pagebuilder;

/**
 * Interface for hosts that handle requests.
 * @since v6.1
 */
public interface PageBuilderSpi
{
    /**
     * Finishes writing the decorated page and decorator to the current HTTP response
     * @param page page being decorated
     */
    public void finish(DecoratablePage page);
}
