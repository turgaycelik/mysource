package com.atlassian.jira.workflow.tabs;

import javax.annotation.Nullable;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;

import com.opensymphony.workflow.loader.ActionDescriptor;

/**
 * Allows enumerating and rendering tabs for workflow transitions ('Preconditions', 'Validators', "Post-functions', etc.).
 */
public interface WorkflowTransitionTabProvider
{
    Iterable<WorkflowTransitionTab> getTabs(ActionDescriptor action, JiraWorkflow workflow);

    @Nullable
    String getTabContentHtml(String panelKey, ActionDescriptor action, JiraWorkflow workflow);

    class WorkflowTransitionTab
    {
        private final String label;
        private final String count;
        private final WebPanelModuleDescriptor module;

        public WorkflowTransitionTab(final String label, final String count, final WebPanelModuleDescriptor module)
        {
            this.label = label;
            this.count = count;
            this.module = module;
        }

        public String getLabel()
        {
            return label;
        }

        public String getCount()
        {
            return count;
        }

        public WebPanelModuleDescriptor getModule()
        {
            return module;
        }
    }
}
