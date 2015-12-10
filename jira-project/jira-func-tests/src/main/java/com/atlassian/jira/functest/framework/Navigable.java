package com.atlassian.jira.functest.framework;

/**
 * A place in JIRA that user can go to.
 *
 * @since v4.4
 */
public interface Navigable<T extends Navigable>
{

    /**
     * Go there.
     *
     * @return this instance
     */
    T goTo();
}
