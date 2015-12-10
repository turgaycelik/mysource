package com.atlassian.jira.plugin.projectoperation;

import com.atlassian.annotations.PublicSpi;

/**
 * A very simple helper class that abstracts away the handling of the descriptor,
 *
 * @since v3.12
 */
@PublicSpi
public abstract class AbstractPluggableProjectOperation implements PluggableProjectOperation
{
    protected volatile ProjectOperationModuleDescriptor descriptor;

    public void init(ProjectOperationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }
}
