package com.atlassian.jira.util;

/**
 * A factory that allows objects to be created through constructor dependency injection at runtime.
 *
 * @since v4.0
 */
public interface ComponentFactory
{
    /**
     * Create an object of the passed type using constructor dependency injection. A runtime exception will
     * be thrown if the object cannot be created.
     *
     * @param type the type of the object to create.
     * @param arguments additional objects that can be used to resolve dependencies 
     * @param <T> the type of the returned object.
     * @return return a newly created object.
     */
    <T> T createObject(Class<T> type, Object ... arguments);

    /**
     * Create an object of the passed type using constructor dependency injection. A runtime exception will
     * be thrown if the object cannot be created.
     *
     * @param type the type of the object to create.
     * @param <T> the type of the returned object.
     * @return return a newly created object.
     */
    <T> T createObject(Class<T> type);

}
