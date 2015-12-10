package com.atlassian.jira.security.plugin;

import com.atlassian.fugue.Option;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.plugin.permission.GlobalPermissionModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;

import com.google.common.collect.Maps;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestPluginGlobalPermissionManagerImpl
{
    @Rule public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock private PluginAccessor pluginAccessor;
    @Mock private PluginEventManager pluginEventManager;

    private GlobalPermissionTypesManagerImpl pluginGlobalPermissionManager;

    @Before
    public void setup()
    {
        this.pluginGlobalPermissionManager = new GlobalPermissionTypesManagerImpl(pluginAccessor, pluginEventManager);
    }

    @Test
    public void globalPermissionAddedWithNewModuleDescriptor()
    {
        GlobalPermissionModuleDescriptor module = registerNewModuleDescriptor("plugin-name", "global-permission-name");

        assertGlobalPermissionRegistered(module.getKey());
    }

    @Test
    public void globalPermissionRemovedWhenPluginDisabled()
    {
        GlobalPermissionModuleDescriptor module1 = registerNewModuleDescriptor("plugin-name1", "global-permission-name1");
        GlobalPermissionModuleDescriptor module2 = registerNewModuleDescriptor("plugin-name2", "global-permission-name2");

        assertGlobalPermissionRegistered(module1.getKey());
        assertGlobalPermissionRegistered(module2.getKey());

        unregisterModuleDescriptor(module2);

        assertGlobalPermissionRegistered(module1.getKey());
        assertThat(pluginGlobalPermissionManager.getGlobalPermission(module2.getKey()), Matchers.is(Option.none(GlobalPermissionType.class)));

        unregisterModuleDescriptor(module1);
        assertThat(pluginGlobalPermissionManager.getGlobalPermission(module1.getKey()), Matchers.is(Option.none(GlobalPermissionType.class)));
    }

    @Test
    public void globalPermissionsRebuildAfterClearCacheEvent()
    {
        GlobalPermissionModuleDescriptor module1 = registerNewModuleDescriptor("plugin-name1", "global-permission-name1");
        GlobalPermissionModuleDescriptor module2 = registerNewModuleDescriptor("plugin-name2", "global-permission-name2");

        assertGlobalPermissionRegistered(module1.getKey());
        assertGlobalPermissionRegistered(module2.getKey());

        pluginGlobalPermissionManager.onClearCache(new ClearCacheEvent(Maps.<String, String>newHashMap()));

        assertGlobalPermissionRegistered(module1.getKey());
        assertGlobalPermissionRegistered(module2.getKey());
    }

    private void assertGlobalPermissionRegistered(final String permissionKey)
    {
        assertThat(pluginGlobalPermissionManager.getGlobalPermission(permissionKey), Matchers.notNullValue());
        assertThat(pluginGlobalPermissionManager.getAll(), Matchers.<GlobalPermissionType>hasItem(
                Matchers.hasProperty("key", Matchers.is(permissionKey))
        ));
    }

    private void unregisterModuleDescriptor(GlobalPermissionModuleDescriptor module)
    {
        pluginGlobalPermissionManager.getPluginModuleTracker().onPluginModuleDisabled(new PluginModuleDisabledEvent(module, true));
    }

    private GlobalPermissionModuleDescriptor registerNewModuleDescriptor(final String pluginKey, final String permissionKey)
    {
        GlobalPermissionModuleDescriptor module = mock(GlobalPermissionModuleDescriptor.class);
        when(module.getKey()).thenReturn(permissionKey);
        when(module.getPluginKey()).thenReturn(pluginKey);
        PluginModuleEnabledEvent pluginModuleEnabledEvent = new PluginModuleEnabledEvent(module);
        pluginGlobalPermissionManager.getPluginModuleTracker().onPluginModuleEnabled(pluginModuleEnabledEvent);
        return module;
    }
}
