package com.atlassian.jira.plugin.link.confluence;

/**
 * An interface to identify builders.
 *
 * @since v5.0
 */
public interface Builder<T>
{
    T build();

    void clear();
}
