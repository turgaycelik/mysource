package com.atlassian.jira.util;

import javax.annotation.Nonnull;

/**
 * Consume the object a {@link Supplier} produces.
 */
public interface Consumer<T>
{
    /**
     * Consume the product.
     *
     * @param element must not be null
     */
    void consume(@Nonnull T element);
}