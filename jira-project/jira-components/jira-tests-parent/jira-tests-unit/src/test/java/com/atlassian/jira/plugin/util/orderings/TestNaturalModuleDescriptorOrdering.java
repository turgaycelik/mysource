package com.atlassian.jira.plugin.util.orderings;

import com.atlassian.plugin.ModuleDescriptor;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

/**
 * Responsible for holding tests of {@link NaturalModuleDescriptorOrdering}
 *
 * @since v4.4
 */
public class TestNaturalModuleDescriptorOrdering
{
    @Test
    public void testCompareReturnsZeroForModulesWithTheSameCompleteKey() throws Exception
    {
        final ModuleDescriptor mockDescriptor1 = createMock(ModuleDescriptor.class);
        final ModuleDescriptor mockDescriptor2 = createMock(ModuleDescriptor.class);

        expect(mockDescriptor1.getCompleteKey()).andStubReturn("mock-plugin:descriptor");
        expect(mockDescriptor2.getCompleteKey()).andStubReturn("mock-plugin:descriptor");

        replay(mockDescriptor1, mockDescriptor2);

        final NaturalModuleDescriptorOrdering naturalModuleDescriptorOrdering = new NaturalModuleDescriptorOrdering();

        assertTrue(naturalModuleDescriptorOrdering.compare(mockDescriptor1, mockDescriptor2) == 0);
        assertTrue(naturalModuleDescriptorOrdering.compare(mockDescriptor2, mockDescriptor1) == 0);
    }

    @Test
    public void testCompareFollowsAlphabeticalOrderWhenComparingModulesWithTheSamePluginKey() throws Exception
    {
        final ModuleDescriptor mockDescriptor1 = createMock(ModuleDescriptor.class);
        final ModuleDescriptor mockDescriptor2 = createMock(ModuleDescriptor.class);

        expect(mockDescriptor1.getCompleteKey()).andStubReturn("mock-plugin:a-lower-descriptor");
        expect(mockDescriptor2.getCompleteKey()).andStubReturn("mock-plugin:b-upper-cdescriptor");

        replay(mockDescriptor1, mockDescriptor2);

        final NaturalModuleDescriptorOrdering naturalModuleDescriptorOrdering = new NaturalModuleDescriptorOrdering();

        assertTrue(naturalModuleDescriptorOrdering.compare(mockDescriptor1, mockDescriptor2) <= -1);
        assertTrue(naturalModuleDescriptorOrdering.compare(mockDescriptor2, mockDescriptor1) >= 1);
    }

    @Test
    public void testCompareFollowsAlphabeticalOrderWhenComparingModulesWithDifferentPluginKeys() throws Exception
    {
        final ModuleDescriptor mockDescriptor1 = createMock(ModuleDescriptor.class);
        final ModuleDescriptor mockDescriptor2 = createMock(ModuleDescriptor.class);

        expect(mockDescriptor1.getCompleteKey()).andStubReturn("d-mock-plugin:descriptor");
        expect(mockDescriptor2.getCompleteKey()).andStubReturn("f-mock-plugin:descriptor");

        replay(mockDescriptor1, mockDescriptor2);

        final NaturalModuleDescriptorOrdering naturalModuleDescriptorOrdering = new NaturalModuleDescriptorOrdering();

        assertTrue(naturalModuleDescriptorOrdering.compare(mockDescriptor1, mockDescriptor2) <= -1);
        assertTrue(naturalModuleDescriptorOrdering.compare(mockDescriptor2, mockDescriptor1) >= 1);
    }
}
