package com.atlassian.jira.index;

import java.util.Collection;

/**
 * Manages search extractors
 *
 * @since 6.2
 */
public interface SearchExtractorRegistrationManager
{
    /**
     * Return all extractors that can be applied for specified class no subclasses are taken into account
     *
     * @param entityClass class object to search for extractors
     * @return list of extractors
     */
    <T> Collection<EntitySearchExtractor<T>> findExtractorsForEntity(Class<T> entityClass);

    /**
     * Registers extractor as capable o processing documents of specified class
     *
     * @param extractor the extractor that will be registered for processing entities of {@code entityClass}
     * @param entityClass class that is process by this extractor
     */
    <T> void register(EntitySearchExtractor<? super T> extractor, Class<T> entityClass);

    /**
     * Remove this extractor (identified by equals method) from processing all registered classes.
     *
     * @param extractor instance of extractor to be unregistered
     * @param entityClass class that this extractor should be unregistered from.
     */
    <T> void unregister(EntitySearchExtractor<? super T> extractor, Class<T> entityClass);


}
