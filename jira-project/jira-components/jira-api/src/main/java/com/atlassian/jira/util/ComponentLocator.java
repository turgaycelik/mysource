package com.atlassian.jira.util;

import javax.annotation.Nonnull;

/**
 * A locator that allows components to be looked up at runtime. 
 *
 * @since v4.0
 */
@InjectableComponent
public interface ComponentLocator
{
    /**
     * Find a component of the passed class in JIRA.
     *
     * @param type the class of the component to look for. Must not be null.
     * @param <T> the type of the component to look for.
     * @return a refernce to the component or null if it could not be found.
     */
    <T> T getComponentInstanceOfType(Class<T> type);

    /**
     * Find a component of the passed class in JIRA.  This is a synonym for
     * {@link #getComponentInstanceOfType(Class)} but has a shorter and more
     * meaningful name.
     *
     * @param type the class of the component to look for. Must not be null.
     * @param <T> the type of the component to look for.
     * @return a refernce to the component or null if it could not be found.
     */
    <T> T getComponent(Class<T> type);

    /**
     * Create a {@link com.google.common.base.Supplier} for the passed component.
     *
     * @param type the class of the component to look for. Must not be null.
     * @param <T> the type of the component to look for.
     * @return a {@link com.google.common.base.Supplier} for the component.
     * @since 6.2.3.
     */
    @Nonnull
    <T> com.google.common.base.Supplier<T> getComponentSupplier(Class<T> type);
}
