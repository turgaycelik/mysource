package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.ConditionDescriptor;

public interface WorkflowConditionDescriptorEditPreprocessor
{
    /**
     * Gets called immediately before saving of the workflow condition descriptor.
     *
     * @param descriptor condition descriptor being saved
     */
    void beforeSaveOnEdit(ConditionDescriptor descriptor);
}
