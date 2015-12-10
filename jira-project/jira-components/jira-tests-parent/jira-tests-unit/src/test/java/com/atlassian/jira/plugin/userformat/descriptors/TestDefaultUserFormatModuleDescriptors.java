package com.atlassian.jira.plugin.userformat.descriptors;

import java.util.List;

import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertTrue;

/**
 * Holds unit tests for {@link DefaultUserFormatModuleDescriptors}
 *
 * @since v4.4
 */
public class TestDefaultUserFormatModuleDescriptors
{
    private PluginAccessor mockPluginAccesor;

    private ModuleDescriptors.Orderings mockModuleDescriptorOrderings;

    @Before
    public void setUpMocks()
    {
        mockPluginAccesor = createMock(PluginAccessor.class);
        mockModuleDescriptorOrderings = createMock(ModuleDescriptors.Orderings.class);
    }

    @Test
    public void forTypeShouldReturnAnEmptyIterableIfThereAreNoUserFormatModuleDescriptorsForTheSpecifiedType()
    {
        final UserFormatModuleDescriptor moduleDescriptor1 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor1.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor moduleDescriptor2 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor2.getType()).andStubReturn("test-user-format-type");

        expect(mockPluginAccesor.getEnabledModuleDescriptorsByClass(UserFormatModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(moduleDescriptor1, moduleDescriptor2));

        final DefaultUserFormatModuleDescriptors userFormatModuleDescriptors =
                new DefaultUserFormatModuleDescriptors(mockPluginAccesor, mockModuleDescriptorOrderings);

        replay(mockPluginAccesor, mockModuleDescriptorOrderings, moduleDescriptor1, moduleDescriptor2);

        final Iterable<UserFormatModuleDescriptor> descriptorsForTypeResult =
                userFormatModuleDescriptors.forType("non-existing-user-format-type");

        assertTrue(Iterables.isEmpty(descriptorsForTypeResult));

        verify(mockPluginAccesor, mockModuleDescriptorOrderings, moduleDescriptor1, moduleDescriptor2);
    }

    @Test
    public void forTypeShouldReturnAllTheUserFormatModuleDescriptorsThatCanHandleTheSpecifiedTypeWhenThereIsMoreThanOneOfThem()
    {
        final UserFormatModuleDescriptor moduleDescriptor1 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor1.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor moduleDescriptor2 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor2.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor moduleDescriptor3 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor3.getType()).andStubReturn("another-test-user-format-type");

        expect(mockPluginAccesor.getEnabledModuleDescriptorsByClass(UserFormatModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(moduleDescriptor1, moduleDescriptor2, moduleDescriptor3));

        final DefaultUserFormatModuleDescriptors userFormatModuleDescriptors =
                new DefaultUserFormatModuleDescriptors(mockPluginAccesor, mockModuleDescriptorOrderings);

        replay(mockPluginAccesor, mockModuleDescriptorOrderings, moduleDescriptor1, moduleDescriptor2, moduleDescriptor3);

        final Iterable<UserFormatModuleDescriptor> descriptorsForTypeResult =
                userFormatModuleDescriptors.forType("test-user-format-type");

        assertTrue(Iterables.size(descriptorsForTypeResult) == 2);
        assertTrue(Iterables.contains(descriptorsForTypeResult, moduleDescriptor1));
        assertTrue(Iterables.contains(descriptorsForTypeResult, moduleDescriptor2));

        verify(mockPluginAccesor, mockModuleDescriptorOrderings, moduleDescriptor1, moduleDescriptor2, moduleDescriptor3);
    }

    @Test
    public void forTypeShouldReturnOnlyOneUserFormatModuleDescriptorsForTheSpecifiedTypeWhenThereIsOnlyOneDescriptorThatCanHandleThatType()
    {
        final UserFormatModuleDescriptor moduleDescriptor1 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor1.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor moduleDescriptor2 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor2.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor moduleDescriptor3 = createMock(UserFormatModuleDescriptor.class);
        expect(moduleDescriptor3.getType()).andStubReturn("another-test-user-format-type");

        expect(mockPluginAccesor.getEnabledModuleDescriptorsByClass(UserFormatModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(moduleDescriptor1, moduleDescriptor2, moduleDescriptor3));

        final DefaultUserFormatModuleDescriptors userFormatModuleDescriptors =
                new DefaultUserFormatModuleDescriptors(mockPluginAccesor, mockModuleDescriptorOrderings);

        replay(mockPluginAccesor, mockModuleDescriptorOrderings, moduleDescriptor1, moduleDescriptor2, moduleDescriptor3);

        final Iterable<UserFormatModuleDescriptor> descriptorsForTypeResult =
                userFormatModuleDescriptors.forType("another-test-user-format-type");

        assertTrue(Iterables.size(descriptorsForTypeResult) == 1);
        assertTrue(Iterables.contains(descriptorsForTypeResult, moduleDescriptor3));

        verify(mockPluginAccesor, mockModuleDescriptorOrderings, moduleDescriptor1, moduleDescriptor2, moduleDescriptor3);
    }

    @Test
    public void defaultForATypeShouldBeASystemModuleDescriptorWhenThereIsASystemDescriptorAndANonSystemDescriptorThatCanHandleThatType()
    {
        final UserFormatModuleDescriptor systemModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        expect(systemModuleDescriptor.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor userInstalledModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        expect(userInstalledModuleDescriptor.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor anotherModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        expect(anotherModuleDescriptor.getType()).andStubReturn("another-test-user-format-type");

        expect(mockPluginAccesor.getEnabledModuleDescriptorsByClass(UserFormatModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(systemModuleDescriptor, userInstalledModuleDescriptor, anotherModuleDescriptor));

        expect(mockModuleDescriptorOrderings.byOrigin()).
                andStubReturn(
                        buildStubOrderingFromListPosition
                                (
                                        ImmutableList.<ModuleDescriptor>of(systemModuleDescriptor, userInstalledModuleDescriptor)
                                ));

        // Natural Order should not matter in this case.
        expect(mockModuleDescriptorOrderings.natural()).andStubReturn(buildStubAlwaysEqualOrdering());

        final DefaultUserFormatModuleDescriptors userFormatModuleDescriptors =
                new DefaultUserFormatModuleDescriptors(mockPluginAccesor, mockModuleDescriptorOrderings);

        replay(mockPluginAccesor, mockModuleDescriptorOrderings, systemModuleDescriptor, userInstalledModuleDescriptor,
                anotherModuleDescriptor);

        final UserFormatModuleDescriptor defaultDescriptorsForType =
                userFormatModuleDescriptors.defaultFor("test-user-format-type");

        assertTrue(defaultDescriptorsForType.equals(systemModuleDescriptor));

        verify(mockPluginAccesor, mockModuleDescriptorOrderings, systemModuleDescriptor, userInstalledModuleDescriptor,
                anotherModuleDescriptor);
    }

    @Test
    public void defaultForATypeShouldBeTheModuleDescriptorThatIsLowerAccordingToNaturalOrderingWhenTheDescriptorsThatCanHandleThatTypeHaveTheSameOrigin()
    {
        final UserFormatModuleDescriptor abcModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        expect(abcModuleDescriptor.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor xyzInstalledModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        expect(xyzInstalledModuleDescriptor.getType()).andStubReturn("test-user-format-type");

        final UserFormatModuleDescriptor anotherModuleDescriptor = createMock(UserFormatModuleDescriptor.class);
        expect(anotherModuleDescriptor.getType()).andStubReturn("another-test-user-format-type");

        expect(mockPluginAccesor.getEnabledModuleDescriptorsByClass(UserFormatModuleDescriptor.class)).
                andStubReturn(ImmutableList.of(abcModuleDescriptor, xyzInstalledModuleDescriptor, anotherModuleDescriptor));

        // They all come from the same origin
        expect(mockModuleDescriptorOrderings.byOrigin()).andStubReturn(buildStubAlwaysEqualOrdering());

        expect(mockModuleDescriptorOrderings.natural()).
                andStubReturn
                        (
                                buildStubOrderingFromListPosition
                                        (
                                                ImmutableList.<ModuleDescriptor>of(abcModuleDescriptor, xyzInstalledModuleDescriptor)
                                        )
                        );

        final DefaultUserFormatModuleDescriptors userFormatModuleDescriptors =
                new DefaultUserFormatModuleDescriptors(mockPluginAccesor, mockModuleDescriptorOrderings);

        replay(mockPluginAccesor, mockModuleDescriptorOrderings, abcModuleDescriptor, xyzInstalledModuleDescriptor,
                anotherModuleDescriptor);

        final UserFormatModuleDescriptor defaultDescriptorsForType =
                userFormatModuleDescriptors.defaultFor("test-user-format-type");

        assertTrue(defaultDescriptorsForType.equals(abcModuleDescriptor));

        verify(mockPluginAccesor, mockModuleDescriptorOrderings, abcModuleDescriptor, xyzInstalledModuleDescriptor,
                anotherModuleDescriptor);
    }

    private Ordering<ModuleDescriptor> buildStubOrderingFromListPosition(final List<ModuleDescriptor> moduleDescriptorList)
    {
        return new Ordering<ModuleDescriptor>()
        {
            @Override
            public int compare(ModuleDescriptor o1, ModuleDescriptor o2)
            {
                if (moduleDescriptorList.indexOf(o1) < moduleDescriptorList.indexOf(o2))
                {
                    return -1;
                }
                else if(moduleDescriptorList.indexOf(o1) > moduleDescriptorList.indexOf(o2))
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        };
    }

    /**
     * A dummy ordering that considers all objects to be equal.
     * @return Always returns zero.
     */
    private Ordering<ModuleDescriptor> buildStubAlwaysEqualOrdering()
    {
        return new Ordering<ModuleDescriptor>()
        {
            @Override
            public int compare(ModuleDescriptor o1, ModuleDescriptor o2)
            {
                return 0;
            }
        };
    }
}
