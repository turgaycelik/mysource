package com.atlassian.jira.plugin.webwork;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.InstallationMode;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Permissions;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;

import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultAutowireCapableWebworkActionRegistry extends MockControllerTestCase
{
    @Test
    public void testRegisterActionInvalidPlugin()
    {
        //a plugin that isn't AutowireCapable.
        final Plugin mockPlugin = mockController.getMock(Plugin.class);

        final ModuleDescriptor<?> mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        mockModuleDescriptor.getPlugin();
        mockController.setReturnValue(mockPlugin);

        mockModuleDescriptor.getPluginKey();
        mockController.setReturnValue("some.plugin.key");

        mockController.replay();
        final DefaultAutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry = new DefaultAutowireCapableWebworkActionRegistry();
        try
        {
            autowireCapabaleWebworkActionRegistry.registerAction("some.action.class", mockModuleDescriptor);
            fail("Should only be able to register AutowireCapablePlugins");
        }
        catch (final IllegalArgumentException e)
        {
            //should have gotten here!
        }
    }

    @Test
    public void testRegisterAction()
    {
        final MockPlugin plugin = new MockPlugin("someplugin");
        final MockPlugin anotherPlugin = new MockPlugin("anotherplugin");

        final ModuleDescriptor<?> mockModuleDescriptor = EasyMock.createMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getPlugin()).andReturn(plugin).times(4);
        expect(mockModuleDescriptor.getCompleteKey()).andReturn("someplugin:module").times(5);

        final ModuleDescriptor<?> mockModuleDescriptor2 = EasyMock.createMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor2.getPlugin()).andReturn(anotherPlugin).times(2);
        expect(mockModuleDescriptor2.getCompleteKey()).andReturn("anotherplugin:module2").times(1);

        EasyMock.replay(mockModuleDescriptor, mockModuleDescriptor2);

        final DefaultAutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry = new DefaultAutowireCapableWebworkActionRegistry();

        autowireCapabaleWebworkActionRegistry.registerAction("some.action.class", mockModuleDescriptor);
        autowireCapabaleWebworkActionRegistry.registerAction("some.action.class2", mockModuleDescriptor);
        autowireCapabaleWebworkActionRegistry.registerAction("other.plugin.action.class", mockModuleDescriptor2);

        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class2"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("other.plugin.action.class"));
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("other.plugin.action.class2"));

        final AutowireCapablePlugin plugin1 = autowireCapabaleWebworkActionRegistry.getPlugin("some.action.class");
        final AutowireCapablePlugin plugin2 = autowireCapabaleWebworkActionRegistry.getPlugin("some.action.class2");
        final AutowireCapablePlugin plugin3 = autowireCapabaleWebworkActionRegistry.getPlugin("other.plugin.action.class");
        final AutowireCapablePlugin plugin4 = autowireCapabaleWebworkActionRegistry.getPlugin("other.plugin.action.class2");

        assertEquals(plugin, plugin1);
        assertEquals(plugin1, plugin2);
        assertEquals(anotherPlugin, plugin3);
        assertNull(plugin4);

        autowireCapabaleWebworkActionRegistry.unregisterPluginModule(mockModuleDescriptor);

        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class2"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("other.plugin.action.class"));
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("other.plugin.action.class2"));

        EasyMock.verify(mockModuleDescriptor, mockModuleDescriptor2);
    }

    @Test
    public void testRegisterNullPlugin()
    {
        final ModuleDescriptor<?> mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);

        mockController.replay();
        final DefaultAutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry = new DefaultAutowireCapableWebworkActionRegistry();

        try
        {
            autowireCapabaleWebworkActionRegistry.registerAction(null, mockModuleDescriptor);
            fail("Should throw exception");
        }
        catch (final IllegalArgumentException e)
        {
            //can't register against null!
        }

        try
        {
            autowireCapabaleWebworkActionRegistry.registerAction("somethign", null);
            fail("Should throw exception");
        }
        catch (final IllegalArgumentException e)
        {
            //can't register against null!
        }
    }

    @Test
    public void testUnregisterNullPlugin()
    {
        final ModuleDescriptor<?> mockModuleDescriptor = mockController.getMock(ModuleDescriptor.class);
        mockModuleDescriptor.getPlugin();
        final MockPlugin plugin = new MockPlugin("someplugin");
        mockController.setReturnValue(plugin);

        mockController.replay();
        final DefaultAutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry = new DefaultAutowireCapableWebworkActionRegistry();

        autowireCapabaleWebworkActionRegistry.registerAction("some.action.class", mockModuleDescriptor);

        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        try
        {
            autowireCapabaleWebworkActionRegistry.unregisterPluginModule(null);
            fail("Should have thrown exception");
        }
        catch (final IllegalArgumentException e)
        {
            //yay
        }
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
    }

    @Test
    public void testUnregisterNonExistentPlugin()
    {
        final MockPlugin plugin = new MockPlugin("someplugin");
        final MockPlugin anotherPlugin = new MockPlugin("anotherplugin");

        final ModuleDescriptor<?> mockModuleDescriptor = EasyMock.createMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getPlugin()).andReturn(plugin).times(1);
        expect(mockModuleDescriptor.getCompleteKey()).andReturn("someplugin:module").times(1);

        final ModuleDescriptor<?> mockModuleDescriptor2 = EasyMock.createMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor2.getCompleteKey()).andReturn("anotherplugin:module2").times(1);

        EasyMock.replay(mockModuleDescriptor, mockModuleDescriptor2);

        final DefaultAutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry = new DefaultAutowireCapableWebworkActionRegistry();

        autowireCapabaleWebworkActionRegistry.registerAction("some.action.class", mockModuleDescriptor);

        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        autowireCapabaleWebworkActionRegistry.unregisterPluginModule(mockModuleDescriptor2);
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        EasyMock.verify(mockModuleDescriptor, mockModuleDescriptor2);
    }


    @Test
    public void shouldCorrectlyRegisterAndUnregisterTwoActionsWithTheSameModuleKey()
    {
        final MockPlugin plugin = new MockPlugin("someplugin");
        final MockPlugin anotherPlugin = new MockPlugin("anotherplugin");

        final ModuleDescriptor<?> mockModuleDescriptor = EasyMock.createMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor.getPlugin()).andReturn(plugin).anyTimes();
        expect(mockModuleDescriptor.getCompleteKey()).andReturn("someplugin:module").anyTimes();

        final ModuleDescriptor<?> mockModuleDescriptor2 = EasyMock.createMock(ModuleDescriptor.class);
        expect(mockModuleDescriptor2.getPlugin()).andReturn(anotherPlugin).anyTimes();
        expect(mockModuleDescriptor2.getCompleteKey()).andReturn("anotherplugin:module").anyTimes();

        EasyMock.replay(mockModuleDescriptor, mockModuleDescriptor2);

        final DefaultAutowireCapableWebworkActionRegistry autowireCapabaleWebworkActionRegistry = new DefaultAutowireCapableWebworkActionRegistry();

        autowireCapabaleWebworkActionRegistry.registerAction("some.action.class", mockModuleDescriptor);
        autowireCapabaleWebworkActionRegistry.registerAction("another.action.class", mockModuleDescriptor2);

        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("another.action.class"));
        autowireCapabaleWebworkActionRegistry.unregisterPluginModule(mockModuleDescriptor);
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("another.action.class"));

        autowireCapabaleWebworkActionRegistry.registerAction("some.action.class", mockModuleDescriptor);
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("another.action.class"));
        autowireCapabaleWebworkActionRegistry.unregisterPluginModule(mockModuleDescriptor2);
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("another.action.class"));
        assertTrue(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));
        autowireCapabaleWebworkActionRegistry.unregisterPluginModule(mockModuleDescriptor);
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("another.action.class"));
        assertFalse(autowireCapabaleWebworkActionRegistry.containsAction("some.action.class"));

    }

    public static class MockPlugin implements Plugin, AutowireCapablePlugin
    {
        String key;

        public MockPlugin(final String key)
        {
            this.key = key;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final MockPlugin that = (MockPlugin) o;

            if (key != null ? !key.equals(that.key) : that.key != null)
            {
                return false;
            }

            return true;
        }

        public int getPluginsVersion()
        {
            return 0;
        }

        public void setPluginsVersion(final int version)
        {}

        public String getName()
        {
            return null;
        }

        public void setName(final String name)
        {}

        public String getI18nNameKey()
        {
            return null;
        }

        public void setI18nNameKey(final String i18nNameKey)
        {}

        public String getKey()
        {
            return key;
        }

        public void setKey(final String aPackage)
        {}

        public void addModuleDescriptor(final ModuleDescriptor<?> moduleDescriptor)
        {}

        public Collection<ModuleDescriptor<?>> getModuleDescriptors()
        {
            return null;
        }

        public ModuleDescriptor<?> getModuleDescriptor(final String key)
        {
            return null;
        }

        public <M> List<ModuleDescriptor<M>> getModuleDescriptorsByModuleClass(final Class<M> moduleClass)
        {
            return null;
        }

        public boolean isEnabledByDefault()
        {
            return false;
        }

        public void setEnabledByDefault(final boolean enabledByDefault)
        {}

        public PluginInformation getPluginInformation()
        {
            return null;
        }

        public void setPluginInformation(final PluginInformation pluginInformation)
        {}

        public void setResources(final Resourced resources)
        {}

        public PluginState getPluginState()
        {
            return null;
        }

        public boolean isEnabled()
        {
            return false;
        }

        public void setEnabled(final boolean enabled)
        {}

        public boolean isSystemPlugin()
        {
            return false;
        }

        public boolean containsSystemModule()
        {
            return false;
        }

        public void setSystemPlugin(final boolean system)
        {}

        public boolean isBundledPlugin()
        {
            return false;
        }

        public Date getDateLoaded()
        {
            return null;
        }

        @Override
        public Date getDateInstalled()
        {
            return null;
        }

        public boolean isUninstallable()
        {
            return false;
        }

        public boolean isDeleteable()
        {
            return false;
        }

        public boolean isDynamicallyLoaded()
        {
            return false;
        }

        public <T> Class<T> loadClass(final String clazz, final Class<?> callingClass) throws ClassNotFoundException
        {
            return null;
        }

        public ClassLoader getClassLoader()
        {
            return null;
        }

        public URL getResource(final String path)
        {
            return null;
        }

        public InputStream getResourceAsStream(final String name)
        {
            return null;
        }

        public void close()
        {}

        public <T> T autowire(final Class<T> clazz)
        {
            return null;
        }

        public <T> T autowire(final Class<T> clazz, final AutowireStrategy autowireStrategy)
        {
            return null;
        }

        public void autowire(final Object instance)
        {}

        public void autowire(final Object instance, final AutowireStrategy autowireStrategy)
        {}

        public List<ResourceDescriptor> getResourceDescriptors()
        {
            return null;
        }

        public List<ResourceDescriptor> getResourceDescriptors(final String type)
        {
            return null;
        }

        public ResourceDescriptor getResourceDescriptor(final String type, final String name)
        {
            return null;
        }

        public ResourceLocation getResourceLocation(final String type, final String name)
        {
            return null;
        }

        public int compareTo(final Plugin o)
        {
            return 0;
        }

        public Set<String> getRequiredPlugins()
        {
            return Collections.emptySet();
        }

        public void disable()
        {}

        public void enable()
        {}

        public void install()
        {}

        public void uninstall()
        {}

        @Override
        public InstallationMode getInstallationMode()
        {
            return InstallationMode.LOCAL;
        }

        @Override
        public Set<String> getActivePermissions()
        {
            return ImmutableSet.of(Permissions.ALL_PERMISSIONS);
        }

        @Override
        public boolean hasAllPermissions()
        {
            return true;
        }
    }
}
