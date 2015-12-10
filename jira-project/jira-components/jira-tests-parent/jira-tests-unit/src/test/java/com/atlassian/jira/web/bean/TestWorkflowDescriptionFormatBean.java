package com.atlassian.jira.web.bean;

import java.util.List;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
  @since v6.0
 */
public class TestWorkflowDescriptionFormatBean
{

    public static final String PLUGIN_TYPE = "pluginType";
    public static final String MODULE_KEY_1 = "ModuleKey1";
    public static final String MODULE_KEY_2 = "ModuleKey2";
    public static final String MODULE_KEY_3 = "ModuleKey3";
    public static final String PLUGIN_KEY_1 = "PluginKey1";

    private WorkflowDescriptorFormatBean workflowDescriptorFormatMock;
    private PluginAccessor pluginAccessorMock;
    private ModuleDescriptor<Object> moduleDescriptorMock1;
    private ModuleDescriptor<Object> moduleDescriptorMock2;
    private ModuleDescriptor<Object> moduleDescriptorMock3;

    class FunctionClassForDescriptor1 {}

    @Before
    public void setUp() {
        workflowDescriptorFormatMock = mock(WorkflowDescriptorFormatBean.class);
        pluginAccessorMock = mock(PluginAccessor.class);
        moduleDescriptorMock1 = mock(AbstractWorkflowModuleDescriptor.class);
        moduleDescriptorMock2 = mock(AbstractWorkflowModuleDescriptor.class);
        moduleDescriptorMock3 = mock(AbstractWorkflowModuleDescriptor.class);

        final List<ModuleDescriptor<Object>> descriptors = ImmutableList.of(moduleDescriptorMock1, moduleDescriptorMock2, moduleDescriptorMock3);

        when(pluginAccessorMock.getEnabledModuleDescriptorsByType(PLUGIN_TYPE)).thenReturn(descriptors);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock1).getImplementationClass()).thenReturn(FunctionClassForDescriptor1.class);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock2).getImplementationClass()).thenReturn(FunctionClassForDescriptor1.class);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock3).getImplementationClass()).thenReturn(FunctionClassForDescriptor1.class);

        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock1).getPluginKey()).thenReturn(PLUGIN_KEY_1);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock1).getKey()).thenReturn(MODULE_KEY_1);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock2).getPluginKey()).thenReturn(PLUGIN_KEY_1);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock2).getKey()).thenReturn(null);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock3).getPluginKey()).thenReturn(PLUGIN_KEY_1);
        when(((AbstractWorkflowModuleDescriptor) moduleDescriptorMock3).getKey()).thenReturn(MODULE_KEY_3);

    }

    @Test
    public void assumeYouGetWorkflowDescriptorWhenNoModuleKey()
    {
        final WorkflowDescriptorFormatBean workflowDescriptorFormatBean = new WorkflowDescriptorFormatBean(pluginAccessorMock);
        final AbstractWorkflowModuleDescriptor workflowModuleDescriptor =
                workflowDescriptorFormatBean.getWorkflowModuleDescriptor(FunctionClassForDescriptor1.class.getName(), PLUGIN_KEY_1, PLUGIN_TYPE);

        assertEquals(moduleDescriptorMock2, workflowModuleDescriptor);
    }

    @Test
    public void assumeYouGetWorkflowDescriptorWhenModuleKeyPresent()
    {
        final WorkflowDescriptorFormatBean workflowDescriptorFormatBean = new WorkflowDescriptorFormatBean(pluginAccessorMock);
        final AbstractWorkflowModuleDescriptor workflowModuleDescriptor =
                workflowDescriptorFormatBean.getWorkflowModuleDescriptor(FunctionClassForDescriptor1.class.getName(), PLUGIN_KEY_1 + MODULE_KEY_3, PLUGIN_TYPE);

        assertEquals(moduleDescriptorMock3, workflowModuleDescriptor);
    }
}
