package com.atlassian.jira.web.action.admin.workflow.tabs;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.ResultDescriptor;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestWorkflowTransitionPostFunctionsContextProvider
{
    WorkflowTransitionPostFunctionsContextProvider contextProvider = new WorkflowTransitionPostFunctionsContextProvider();
    private DescriptorFactory descriptorFactory = DescriptorFactory.getFactory();

    @Test
    public void testGetContext() throws Exception
    {
        // having
        final ActionDescriptor actionDescriptor = descriptorFactory.createActionDescriptor();
        final ResultDescriptor resultDescriptor = descriptorFactory.createResultDescriptor();
        actionDescriptor.setUnconditionalResult(resultDescriptor);
        resultDescriptor.getPostFunctions().add(descriptorFactory.createFunctionDescriptor());
        final Map<String, Object> context = ImmutableMap.<String, Object>
                of(WorkflowTransitionContext.TRANSITION_KEY, actionDescriptor);

        // when
        final Map<String, Object> contextMap = contextProvider.getContextMap(context);

        // then
        assertThat((Integer) contextMap.get(WorkflowTransitionContext.COUNT_KEY), equalTo(1));
    }

    @Test
    public void testGetContextWithNoAction() throws Exception
    {
        // having
        final Map<String, Object> context = Maps.newHashMap();
        context.put(WorkflowTransitionContext.TRANSITION_KEY, null);

        // when
        final Map<String, Object> contextMap = contextProvider.getContextMap(context);

        // then
        assertThat((Integer) contextMap.get(WorkflowTransitionContext.COUNT_KEY), equalTo(0));
    }
}
