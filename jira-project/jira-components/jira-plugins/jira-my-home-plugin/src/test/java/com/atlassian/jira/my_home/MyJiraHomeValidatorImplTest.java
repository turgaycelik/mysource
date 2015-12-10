package com.atlassian.jira.my_home;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MyJiraHomeValidatorImplTest
{
    private static final String PLUGIN_MODULE_KEY = "complete.plugin.module.key";
    private static final String INVALID_KEY = "invalid-key";

    private final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
    
    private final MyJiraHomeValidator validator = new MyJiraHomeValidatorImpl(mockPluginAccessor);

    @Test
    public void testPluginModuleIsNotEnabled()
    {
        when(mockPluginAccessor.isPluginModuleEnabled(PLUGIN_MODULE_KEY)).thenReturn(Boolean.FALSE);

        assertFalse(validator.isValid(PLUGIN_MODULE_KEY));
        assertTrue(validator.isInvalid(PLUGIN_MODULE_KEY));
    }

    @Test
    public void testPluginModuleKeyIsInvalid()
    {
        when(mockPluginAccessor.isPluginModuleEnabled(INVALID_KEY)).thenThrow(new IllegalArgumentException());

        assertFalse(validator.isValid(INVALID_KEY));
        assertTrue(validator.isInvalid(INVALID_KEY));
    }

    @Test
    public void testPluginModuleIsNotAWebItem()
    {
        expectPluginIsEnabled();
        expectPluginModuleIsA(mock(WebSectionModuleDescriptor.class));

        assertFalse(validator.isValid(PLUGIN_MODULE_KEY));
        assertTrue(validator.isInvalid(PLUGIN_MODULE_KEY));
    }

    @Test
    public void testPluginModuleIsValid()
    {
        expectPluginIsEnabled();
        expectPluginModuleIsA(mock(WebItemModuleDescriptor.class));

        assertTrue(validator.isValid(PLUGIN_MODULE_KEY));
        assertFalse(validator.isInvalid(PLUGIN_MODULE_KEY));
    }

    private void expectPluginIsEnabled()
    {
        when(mockPluginAccessor.isPluginModuleEnabled(PLUGIN_MODULE_KEY)).thenReturn(Boolean.TRUE);
    }

    private void expectPluginModuleIsA(ModuleDescriptor mockWebItemPluginModule)
    {
        when(mockPluginAccessor.getPluginModule(PLUGIN_MODULE_KEY)).thenReturn(mockWebItemPluginModule);
    }
}
