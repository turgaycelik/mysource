package com.atlassian.jira.web.action.admin.plugins;

import java.util.Collections;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestPluginReindexHelperImpl extends MockControllerTestCase
{
    private PluginAccessor pluginAccessor;

    @Before
    public void setUp() throws Exception
    {
        pluginAccessor = mockController.getMock(PluginAccessor.class);
    }

    @Test
    public void testDoesEnablingPluginModuleRequireMessageNull() throws Exception
    {
        final String key = "badKey";
        EasyMock.expect(pluginAccessor.getEnabledPluginModule(key))
                .andReturn(null);

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginModuleRequireMessage(key);
        assertFalse(result);
    }

    @Test
    public void testDoesEnablingPluginModuleRequireMessageNotInterestingType() throws Exception
    {
        final ModuleDescriptor descriptor = mockController.getMock(ModuleDescriptor.class);
        final String key = "notInteresting";
        EasyMock.expect(pluginAccessor.getEnabledPluginModule(key))
                .andReturn(descriptor);

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginModuleRequireMessage(key);
        assertFalse(result);
    }

    @Test
    public void testDoesEnablingPluginModuleRequireMessageCustomFieldType() throws Exception
    {
        final ModuleDescriptor descriptor = EasyMock.createMock(CustomFieldTypeModuleDescriptor.class);
        final String key = "customfieldtype";
        EasyMock.expect(pluginAccessor.getEnabledPluginModule(key))
                .andReturn(descriptor);

        EasyMock.replay(descriptor);
        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginModuleRequireMessage(key);
        assertTrue(result);

        EasyMock.verify(descriptor);
    }

    @Test
    public void testDoesEnablingPluginModuleRequireMessageCustomFieldSearcher() throws Exception
    {
        final ModuleDescriptor descriptor = EasyMock.createMock(CustomFieldSearcherModuleDescriptor.class);
        final String key = "customfieldsearcher";
        EasyMock.expect(pluginAccessor.getEnabledPluginModule(key))
                .andReturn(descriptor);

        EasyMock.replay(descriptor);
        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginModuleRequireMessage(key);
        assertTrue(result);

        EasyMock.verify(descriptor);
    }

    @Test
    public void testDoesEnablingPluginRequireMessageNull() throws Exception
    {
        final String key = "badKey";
        EasyMock.expect(pluginAccessor.getEnabledPlugin(key))
                .andReturn(null);

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginRequireMessage(key);
        assertFalse(result);
    }

    @Test
    public void testDoesEnablingPluginRequireMessageEmptyDescriptors() throws Exception
    {
        final Plugin plugin = mockController.getMock(Plugin.class);
        final String key = "key";
        EasyMock.expect(pluginAccessor.getEnabledPlugin(key))
                .andReturn(plugin);
        EasyMock.expect(plugin.getModuleDescriptors())
                .andReturn(Collections.<ModuleDescriptor<?>>emptyList());

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginRequireMessage(key);
        assertFalse(result);
    }

    @Test
    public void testDoesEnablingPluginRequireMessageOneNotInterestingDescriptor() throws Exception
    {
        final ModuleDescriptor descriptor = mockController.getMock(ModuleDescriptor.class);
        final Plugin plugin = mockController.getMock(Plugin.class);
        final String key = "key";
        EasyMock.expect(pluginAccessor.getEnabledPlugin(key))
                .andReturn(plugin);
        EasyMock.expect(plugin.getModuleDescriptors())
                .andReturn(Collections.<ModuleDescriptor<?>>singleton(descriptor));

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginRequireMessage(key);
        assertFalse(result);
    }

    @Test
    public void testDoesEnablingPluginRequireMessageOneInterestingDescriptor() throws Exception
    {
        final ModuleDescriptor descriptor = EasyMock.createMock(CustomFieldTypeModuleDescriptor.class);
        final Plugin plugin = mockController.getMock(Plugin.class);
        final String key = "key";
        EasyMock.expect(pluginAccessor.getEnabledPlugin(key))
                .andReturn(plugin);
        EasyMock.expect(plugin.getModuleDescriptors())
                .andReturn(Collections.<ModuleDescriptor<?>>singleton(descriptor));

        EasyMock.replay(descriptor);

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginRequireMessage(key);
        assertTrue(result);

        EasyMock.verify(descriptor);
    }

    @Test
    public void testDoesEnablingPluginRequireMessageOtherInterestingDescriptor() throws Exception
    {
        final ModuleDescriptor descriptor = EasyMock.createMock(CustomFieldSearcherModuleDescriptor.class);
        final Plugin plugin = mockController.getMock(Plugin.class);
        final String key = "key";
        EasyMock.expect(pluginAccessor.getEnabledPlugin(key))
                .andReturn(plugin);
        EasyMock.expect(plugin.getModuleDescriptors())
                .andReturn(Collections.<ModuleDescriptor<?>>singleton(descriptor));

        EasyMock.replay(descriptor);

        final PluginReindexHelperImpl helper = mockController.instantiate(PluginReindexHelperImpl.class);

        final boolean result = helper.doesEnablingPluginRequireMessage(key);
        assertTrue(result);

        EasyMock.verify(descriptor);
    }
}
