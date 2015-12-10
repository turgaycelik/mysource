package com.atlassian.jira.web.pagebuilder;

/**
 * Listener for decorators being set
 * @since v6.1
 */
public interface DecoratorListener
{
    /**
     * Called when the decorator is set
     */
    public void onDecoratorSet();
}
