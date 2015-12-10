package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.OSWorkflowConfigurator;
import com.atlassian.plugin.module.ModuleFactory;

import com.opensymphony.workflow.Condition;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestWorkflowConditionModuleDescriptor
{
    private WorkflowConditionModuleDescriptor workflowConditionModuleDescriptor;
    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private OSWorkflowConfigurator workflowConfigurator;
    @Mock
    private ComponentClassManager componentClassManager;
    @Mock
    private ModuleFactory moduleFactory;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        workflowConditionModuleDescriptor = new WorkflowConditionModuleDescriptor(authenticationContext, workflowConfigurator, componentClassManager, moduleFactory);
    }

    @Test
    public void testCreatePluginTypeResolver()
    {
        final AbstractWorkflowModuleDescriptor<WorkflowPluginConditionFactory>.PluginTypeResolver pluginTypeResolver = workflowConditionModuleDescriptor.createPluginTypeResolver();

        assertThat(pluginTypeResolver, instanceOf(WorkflowConditionModuleDescriptor.SafeConditionPluginTypeResolver.class));
    }

    @Test
    public void shouldReturnFalseWhenExceptionThrownFromWrappedPlugin() throws Exception
    {
        final Condition mockCondition = mock(Condition.class);
        when(mockCondition.passesCondition(null, null, null))
                .thenThrow(new RuntimeException());

        final WorkflowConditionModuleDescriptor.SafeConditionPlugin safeConditionPlugin = new WorkflowConditionModuleDescriptor.SafeConditionPlugin(mockCondition);

        final boolean result = safeConditionPlugin.passesCondition(null, null, null);

        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldReturnResultFromWrapperPluginWhenNotExceptions() throws Exception
    {
        final Condition mockCondition = mock(Condition.class);
        when(mockCondition.passesCondition(null, null, null))
                .thenReturn(true);

        final WorkflowConditionModuleDescriptor.SafeConditionPlugin safeConditionPlugin = new WorkflowConditionModuleDescriptor.SafeConditionPlugin(mockCondition);

        final boolean result = safeConditionPlugin.passesCondition(null, null, null);

        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnNullWhenIsNotAbleToInstantiateConditionPlugin()
    {
        final AbstractWorkflowModuleDescriptor<WorkflowPluginConditionFactory>.PluginTypeResolver pluginTypeResolver = workflowConditionModuleDescriptor.createPluginTypeResolver();

        final Object loadedPlugin = pluginTypeResolver.loadObject("notExistingClassName");

        assertThat(loadedPlugin, nullValue());
    }

}
