package com.atlassian.jira.util;

import javax.annotation.Nonnull;

import com.atlassian.annotations.PublicApi;

/**
 * Consume the object a {@link Supplier} produces.
 */
@PublicApi
public interface Consumer<T>
{
    /**
     * Consume the product.
     *
     * @param element must not be null
     */
    void consume(@Nonnull T element);
}