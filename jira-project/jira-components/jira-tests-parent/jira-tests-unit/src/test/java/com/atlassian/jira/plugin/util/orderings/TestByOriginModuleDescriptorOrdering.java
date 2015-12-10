package com.atlassian.jira.plugin.util.orderings;

import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.metadata.PluginMetadataManager;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

/**
 * Responsible for holding tests for {@link ByOriginModuleDescriptorOrdering}
 *
 * @since v4.4
 */
public class TestByOriginModuleDescriptorOrdering
{
    PluginMetadataManager mockPluginMetadataManager;

    @Before
    public void setUpMockComponents()
    {
        mockPluginMetadataManager = createMock(PluginMetadataManager.class);
    }

    @Test
    public void descriptorsComingFromASystemProvidedPluginShouldBeComparedAsEqual()
    {
        final ModuleDescriptor systemProvidedModuleDescriptor1 = createMock(ModuleDescriptor.class);
        final ModuleDescriptor systemProvidedModuleDescriptor2 = createMock(ModuleDescriptor.class);

        expect(systemProvidedModuleDescriptor1.getPlugin()).andStubReturn(createEmptyMockPlugin());
        expect(systemProvidedModuleDescriptor2.getPlugin()).andStubReturn(createEmptyMockPlugin());

        expectAnyPluginToBeSystemProvided();

        final ByOriginModuleDescriptorOrdering byOriginModuleDescriptorOrdering =
                new ByOriginModuleDescriptorOrdering(mockPluginMetadataManager);

        replay(systemProvidedModuleDescriptor1, systemProvidedModuleDescriptor2, mockPluginMetadataManager);

        assertTrue(byOriginModuleDescriptorOrdering.compare(systemProvidedModuleDescriptor1, systemProvidedModuleDescriptor2) == 0);
        assertTrue(byOriginModuleDescriptorOrdering.compare(systemProvidedModuleDescriptor2, systemProvidedModuleDescriptor1) == 0);
    }

    @Test
    public void descriptorsComingFromAUserInstalledPluginShouldBeComparedAsEqual()
    {
        final ModuleDescriptor userInstalledModuleDescriptor1 = createMock(ModuleDescriptor.class);
        final ModuleDescriptor userInstalledModuleDescriptor2 = createMock(ModuleDescriptor.class);

        expect(userInstalledModuleDescriptor1.getPlugin()).andStubReturn(createEmptyMockPlugin());
        expect(userInstalledModuleDescriptor2.getPlugin()).andStubReturn(createEmptyMockPlugin());

        expectAnyPluginToBeUserInstalled();

        final ByOriginModuleDescriptorOrdering byOriginModuleDescriptorOrdering =
                new ByOriginModuleDescriptorOrdering(mockPluginMetadataManager);

        replay(userInstalledModuleDescriptor1, userInstalledModuleDescriptor2, mockPluginMetadataManager);

        assertTrue(byOriginModuleDescriptorOrdering.compare(userInstalledModuleDescriptor1, userInstalledModuleDescriptor2) == 0);
        assertTrue(byOriginModuleDescriptorOrdering.compare(userInstalledModuleDescriptor2, userInstalledModuleDescriptor1) == 0);
    }

    @Test
    public void descriptorsComingFromAUserInstalledPluginShouldBeGreaterThanDescriptorsComingFromASystemProvidedPlugin()
    {
        final ModuleDescriptor userInstalledModuleDescriptor = createMock(ModuleDescriptor.class);
        final Plugin userInstalledPlugin =
                new MockPlugin("User Installed Plugin", "user-installed-plugin", new PluginInformation());
        expect(userInstalledModuleDescriptor.getPlugin()).andStubReturn(userInstalledPlugin);

        final ModuleDescriptor systemProvidedModuleDescriptor = createMock(ModuleDescriptor.class);
        final Plugin systemProvidedPlugin =
                new MockPlugin("System Provided Plugin", "system-provided-plugin", new PluginInformation());
        expect(systemProvidedModuleDescriptor.getPlugin()).andStubReturn(systemProvidedPlugin);

        expect(mockPluginMetadataManager.isUserInstalled(userInstalledPlugin)).andStubReturn(true);
        expect(mockPluginMetadataManager.isUserInstalled(systemProvidedPlugin)).andStubReturn(false);

        final ByOriginModuleDescriptorOrdering byOriginModuleDescriptorOrdering =
                new ByOriginModuleDescriptorOrdering(mockPluginMetadataManager);

        replay(userInstalledModuleDescriptor, systemProvidedModuleDescriptor, mockPluginMetadataManager);

        assertTrue(byOriginModuleDescriptorOrdering.compare(userInstalledModuleDescriptor, systemProvidedModuleDescriptor) >= 1);
        assertTrue(byOriginModuleDescriptorOrdering.compare(systemProvidedModuleDescriptor, userInstalledModuleDescriptor) <= -1);
    }

    private void expectAnyPluginToBeSystemProvided()
    {
        expect(mockPluginMetadataManager.isUserInstalled(EasyMock.<MockPlugin>anyObject())).andStubReturn(false);
    }

    private void expectAnyPluginToBeUserInstalled()
    {
        expect(mockPluginMetadataManager.isUserInstalled(EasyMock.<MockPlugin>anyObject())).andStubReturn(true);
    }

    private Plugin createEmptyMockPlugin()
    {
        return new MockPlugin("Empty Mock Plugin", "mock-plugin", new PluginInformation());
    }
}
