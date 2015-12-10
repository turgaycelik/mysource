package com.atlassian.jira.workflow;

import com.opensymphony.workflow.TypeResolver;

/**
 * Provides a way for JIRA to configure OSWorkflow {@link TypeResolver} classes.
 *
 * @since v4.1.1
 */
public interface OSWorkflowConfigurator
{
    /**
     * Registers a {@link TypeResolver} for the given class. The last registered resolver against the class name will
     * take effect.
     *
     * @param className the class name.
     * @param typeResolver the resolver to register; should not be null.
     */
    void registerTypeResolver(String className, TypeResolver typeResolver);

    /**
     * Unregisters a {@link TypeResolver} from the given class. This class will no longer be mapped to any resolvers.
     *
     * @param className the class name.
     * @param typeResolver the resolver to unregister; should not be null.
     */
    void unregisterTypeResolver(String className, TypeResolver typeResolver);
}
