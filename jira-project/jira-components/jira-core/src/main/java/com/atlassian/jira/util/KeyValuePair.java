package com.atlassian.jira.util;

/**
 * Represents a Key-Value Pair
 *
 * @since v4.4
 */
public interface KeyValuePair<K, V>
{
    K getKey();

    V getValue();
}
