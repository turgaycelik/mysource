package com.atlassian.jira.workflow.tabs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.jira.plugin.webfragment.DefaultWebFragmentContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.admin.workflow.tabs.WorkflowTransitionContext;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.ozymandias.PluginPointFunction;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.log4j.Logger;

public class WebPanelWorkflowTransitionTabProvider implements WorkflowTransitionTabProvider
{
    private static final String TAB_PANELS_LOCATION = "workflow.transition.tabs";

    private static final Logger log = Logger.getLogger(WebPanelWorkflowTransitionTabProvider.class);

    private final WebInterfaceManager webInterfaceManager;
    private Function<WebPanelModuleDescriptor, WorkflowTransitionTab> function;

    public WebPanelWorkflowTransitionTabProvider(final WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public Iterable<WorkflowTransitionTab> getTabs(final ActionDescriptor action, final JiraWorkflow workflow)
    {
        final Map<String, Object> context = createContext(action, workflow);
        final Iterable<WebPanelModuleDescriptor> panels = getDisplayableWebPanelDescriptors(context);
        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(panels, new Function<WebPanelModuleDescriptor, WorkflowTransitionTab>()
        {
            @Override
            public WorkflowTransitionTab apply(final WebPanelModuleDescriptor input)
            {
                return SafePluginPointAccess.call(new Callable<WorkflowTransitionTab>()
                {
                    @Override
                    public WorkflowTransitionTab call() throws Exception
                    {
                        final String label = input.getWebLabel().getDisplayableLabel(ExecutingHttpRequest.get(), context);
                        final String count = input.getWebParams().getRenderedParam(WorkflowTransitionContext.COUNT_KEY, Maps.newHashMap(context)); // mutates the context
                        return new WorkflowTransitionTab(label, count, input);
                    }
                }).getOrNull();
            }
        }), Predicates.notNull()));
    }

    private Map<String, Object> createContext(ActionDescriptor action, JiraWorkflow workflow)
    {
        final ImmutableMap.Builder<String, Object> context = ImmutableMap.<String, Object>builder()
                .putAll(DefaultWebFragmentContext.get(TAB_PANELS_LOCATION));
        if (action != null) {
            context.put(WorkflowTransitionContext.TRANSITION_KEY, action);
        }
        if (workflow != null) {
            context.put(WorkflowTransitionContext.WORKFLOW_KEY, workflow);
        }

        return context.build();
    }

    @Override
    @Nullable
    public String getTabContentHtml(final String panelKey, ActionDescriptor action, JiraWorkflow workflow)
    {
        final Map<String, Object> context = createContext(action, workflow);
        final Iterable<WebPanelModuleDescriptor> panels = getDisplayableWebPanelDescriptors(context);
        final Option<WebPanelModuleDescriptor> tabDescriptor = com.atlassian.fugue.Iterables.findFirst(panels,
                new Predicate<WebPanelModuleDescriptor>()
                {
                    @Override
                    public boolean apply(final WebPanelModuleDescriptor input)
                    {
                        return Objects.equal(input.getKey(), panelKey);
                    }
                }
        );

        return Iterables.getFirst(SafePluginPointAccess.to().descriptors(tabDescriptor,
                new PluginPointFunction<WebPanelModuleDescriptor, WebPanel, String>()
                {
                    @Override
                    public String onModule(final WebPanelModuleDescriptor moduleDescriptor, final WebPanel module)
                    {
                        return moduleDescriptor.getModule().getHtml(context);
                    }
                }
        ), null);
    }

    private Iterable<WebPanelModuleDescriptor> getDisplayableWebPanelDescriptors(final Map<String, Object> context)
    {
        return Iterables.filter(SafePluginPointAccess.call(new Callable<List<WebPanelModuleDescriptor>>()
        {
            @Override
            public List<WebPanelModuleDescriptor> call() throws Exception
            {
                return webInterfaceManager.getDisplayableWebPanelDescriptors(TAB_PANELS_LOCATION, context);
            }
        }).getOrElse(Collections.<WebPanelModuleDescriptor>emptyList()), Predicates.notNull());
    }
}
