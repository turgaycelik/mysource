package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.ValidatorDescriptor;

/**
 * @since 6.3.3
 */
public interface WorkflowValidatorDescriptorEditPreprocessor
{
    /**
     * Gets called immediately before saving of the workflow validator descriptor.
     *
     * @param descriptor validator descriptor being saved
     */
    void beforeSaveOnEdit(ValidatorDescriptor descriptor);
}
