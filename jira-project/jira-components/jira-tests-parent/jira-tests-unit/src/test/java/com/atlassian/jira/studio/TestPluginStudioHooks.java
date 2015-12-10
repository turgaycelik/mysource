package com.atlassian.jira.studio;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.plugin.studio.StudioHooks;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

/**
 * @since v4.4.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPluginStudioHooks
{
    @Mock
    private PluginAccessor accessor;

    @Test
    public void testCacheEmpty() throws Exception
    {
        stub(accessor.getEnabledModulesByClass(StudioHooks.class)).toReturn(Collections.<StudioHooks>emptyList());

        PluginStudioHooks.Cache cache = new PluginStudioHooks.Cache(accessor);

        StudioHooks first = cache.get();
        assertTrue(first instanceof VanillaStudioHooks);
        assertSame(first, cache.get());

        verify(accessor, only()).getEnabledModulesByClass(StudioHooks.class);
    }

    @Test
    public void testCacheOnePlugin() throws Exception
    {
        StudioHooks studioHooks = mock(StudioHooks.class);

        stub(accessor.getEnabledModulesByClass(StudioHooks.class)).toReturn(asList(studioHooks));

        PluginStudioHooks.Cache cache = new PluginStudioHooks.Cache(accessor);

        assertSame(studioHooks, cache.get());
        assertSame(studioHooks, cache.get());

        verify(accessor, only()).getEnabledModulesByClass(StudioHooks.class);
    }

    @Test
    public void testCacheTwoOrMorePlugins() throws Exception
    {
        StudioHooks studioHooks = mock(StudioHooks.class);
        StudioHooks studioHooks2 = mock(StudioHooks.class);

        stub(accessor.getEnabledModulesByClass(StudioHooks.class)).toReturn(asList(studioHooks, studioHooks2));

        PluginStudioHooks.Cache cache = new PluginStudioHooks.Cache(accessor);

        try
        {
            cache.get();
            fail("Expected an illegal state exception.");
        }
        catch (IllegalStateException expected)
        {
        }

        try
        {
            cache.get();
            fail("Expected an illegal state exception.");
        }
        catch (IllegalStateException expected)
        {
        }

        verify(accessor, only()).getEnabledModulesByClass(StudioHooks.class);
    }

    @SuppressWarnings ( { "unchecked" })
    @Test
    public void testCacheClear() throws Exception
    {
        StudioHooks studioHooks = mock(StudioHooks.class);
        StudioHooks studioHooks2 = mock(StudioHooks.class);

        ModuleDescriptor<StudioHooks> goodModule = mock(ModuleDescriptor.class);
        ModuleDescriptor<StudioHooks> nullModule = mock(ModuleDescriptor.class);
        ModuleDescriptor<List> badModule = mock(ModuleDescriptor.class);

        stub(goodModule.getModuleClass()).toReturn(StudioHooks.class);
        stub(badModule.getModuleClass()).toReturn(List.class);

        stub(accessor.getEnabledModulesByClass(StudioHooks.class))
                .toReturn(Collections.<StudioHooks>emptyList())
                .toReturn(asList(studioHooks))
                .toReturn(asList(studioHooks2, studioHooks))
                .toReturn(asList(studioHooks2));

        PluginStudioHooks.Cache cache = new PluginStudioHooks.Cache(accessor);

        StudioHooks hooks = cache.get();
        assertTrue(hooks instanceof VanillaStudioHooks);
        assertSame(hooks, cache.get());
        cache.onModuleDisabled(new PluginModuleDisabledEvent(badModule, false));
        assertSame(hooks, cache.get());
        cache.onModuleDisabled(new PluginModuleDisabledEvent(nullModule, false));
        assertSame(hooks, cache.get());

        //This event should clear the cache.
        cache.onModuleDisabled(new PluginModuleDisabledEvent(goodModule, false));
        assertSame(studioHooks, cache.get());
        assertSame(studioHooks, cache.get());
        cache.onModuleEnabled(new PluginModuleEnabledEvent(badModule));
        assertSame(studioHooks, cache.get());
        cache.onModuleEnabled(new PluginModuleEnabledEvent(nullModule));
        assertSame(studioHooks, cache.get());

        //This event should clear the cache.
        cache.onModuleEnabled(new PluginModuleEnabledEvent(goodModule));

        try
        {
            cache.get();
            fail("Expected an illegal state exception.");
        }
        catch (IllegalStateException expected)
        {
        }

        try
        {
            cache.get();
            fail("Expected an illegal state exception.");
        }
        catch (IllegalStateException expected)
        {
        }

        //This event should clear the cache.
        cache.clearCache(ClearCacheEvent.INSTANCE);
        assertSame(studioHooks2, cache.get());
        assertSame(studioHooks2, cache.get());
        assertSame(studioHooks2, cache.get());
    }
}
