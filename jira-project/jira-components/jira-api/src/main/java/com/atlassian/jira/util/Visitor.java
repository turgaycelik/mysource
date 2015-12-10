package com.atlassian.jira.util;

import com.atlassian.annotations.PublicSpi;

/**
 * Callback interface for code that implements the Visitor Pattern.
 *
 * @since v5.2
 */
@PublicSpi
public interface Visitor<T>
{
    void visit(T element);
}
