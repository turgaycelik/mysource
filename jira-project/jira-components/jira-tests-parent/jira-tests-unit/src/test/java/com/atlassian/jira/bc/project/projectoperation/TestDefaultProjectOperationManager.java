package com.atlassian.jira.bc.project.projectoperation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation;
import com.atlassian.jira.plugin.projectoperation.ProjectOperationModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.PluginAccessor;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v3.12
 */
public class TestDefaultProjectOperationManager
{

    private com.atlassian.crowd.embedded.api.User loggedInUser;

    @Before
    public void setUp() throws Exception
    {
        loggedInUser = null;
    }

    @Test
    public void getVisibleProjectOperationsReturnsAnEmptyListGivenThatThereAreNoProjectOperationModuleDescriptors()
    {
        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        when(mockPluginAccessor.getEnabledModuleDescriptorsByClass(ProjectOperationModuleDescriptor.class)).
                thenReturn(Collections.<ProjectOperationModuleDescriptor>emptyList());

        ProjectOperationManager projectOperationManager = new DefaultProjectOperationManager(mockPluginAccessor);

        final Collection operationDescriptors = projectOperationManager.getVisibleProjectOperations(null, loggedInUser);

        assertNotNull(operationDescriptors);
        assertEquals(0, operationDescriptors.size());
    }

    @Test
    public void getVisibleProjectOperationsReturnsAnEmptyListGivenThatAllTheAvailableOperationsAreNotVisible()
    {
        final PluggableProjectOperation mockPluggableProjectOperation = mock(PluggableProjectOperation.class);
        when(mockPluggableProjectOperation.showOperation(any(Project.class), any(User.class))).
                thenReturn(false);

        final ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor =
                Stubs.ProjectOperation.descriptorFor(mockPluggableProjectOperation).withOrder(0).get();

        final PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);

        when(mockPluginAccessor.getEnabledModuleDescriptorsByClass(ProjectOperationModuleDescriptor.class)).
                thenReturn(ImmutableList.of(mockProjectOperationModuleDescriptor));

        final ProjectOperationManager projectOperationManager = new DefaultProjectOperationManager(mockPluginAccessor);

        final List<PluggableProjectOperation> operationDescriptors =
                projectOperationManager.getVisibleProjectOperations(null, loggedInUser);

        assertNotNull(operationDescriptors);
        assertEquals(0, operationDescriptors.size());
    }

    @Test
    public void testGetViewableProjectOperationDescriptorsWithPermission()
    {
        PluggableProjectOperation projectOperation = mock(PluggableProjectOperation.class);
        when(projectOperation.showOperation(any(Project.class), any(User.class))).thenReturn(true);

        PluggableProjectOperation projectOperation1 = mock(PluggableProjectOperation.class);
        when(projectOperation1.showOperation(any(Project.class), any(User.class))).thenReturn(true);

        PluggableProjectOperation projectOperation2 = mock(PluggableProjectOperation.class);
        when(projectOperation2.showOperation(any(Project.class), any(User.class))).thenReturn(true);

        ProjectOperationModuleDescriptor projectOperationModuleDescriptor =
                Stubs.ProjectOperation.descriptorFor(projectOperation).withOrder(0).get();
        ProjectOperationModuleDescriptor projectOperationModuleDescriptor1 =
                Stubs.ProjectOperation.descriptorFor(projectOperation1).withOrder(1).get();
        ProjectOperationModuleDescriptor projectOperationModuleDescriptor2 =
                Stubs.ProjectOperation.descriptorFor(projectOperation2).withOrder(2).get();

        PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        when(mockPluginAccessor.getEnabledModuleDescriptorsByClass(ProjectOperationModuleDescriptor.class)).
                thenReturn
                        (
                                ImmutableList.of
                                        (
                                                projectOperationModuleDescriptor1, projectOperationModuleDescriptor2,
                                                projectOperationModuleDescriptor
                                        )
                        );

        ProjectOperationManager vp = new DefaultProjectOperationManager(mockPluginAccessor);

        List<PluggableProjectOperation> operationDescriptors = vp.getVisibleProjectOperations(null, loggedInUser);
        assertNotNull(operationDescriptors);
        assertEquals(3, operationDescriptors.size());
        //assert the right order is returned.
        assertEquals(projectOperation, operationDescriptors.get(0));
        assertEquals(projectOperation1, operationDescriptors.get(1));
        assertEquals(projectOperation2, operationDescriptors.get(2));
    }

    /**
     * Test that moduels that throw exceptions dring load are ignored
     */
    @Test
    public void testLoadProjectOperationDescriptorsWithExceptions()
    {
        PluggableProjectOperation projectOperation = mock(PluggableProjectOperation.class);
        when(projectOperation.showOperation(any(Project.class), any(User.class))).thenReturn(true);

        PluggableProjectOperation projectOperation1 = mock(PluggableProjectOperation.class);
        when(projectOperation1.showOperation(any(Project.class), any(User.class))).thenReturn(true);

        PluggableProjectOperation projectOperation2 = mock(PluggableProjectOperation.class);
        when(projectOperation2.showOperation(any(Project.class), any(User.class))).thenReturn(true);

        ProjectOperationModuleDescriptor projectOperationModuleDescriptor =
                Stubs.ProjectOperation.descriptorFor(projectOperation).withOrder(0).get();

        ProjectOperationModuleDescriptor projectOperationModuleDescriptor1 =
                Stubs.ProjectOperation.descriptorFor(projectOperation1).withOrder(1).throwExceptionOnGetModule(true).get();

        ProjectOperationModuleDescriptor projectOperationModuleDescriptor2 =
                Stubs.ProjectOperation.descriptorFor(projectOperation2).withOrder(2).get();

        PluginAccessor mockPluginAccessor = mock(PluginAccessor.class);
        when(mockPluginAccessor.getEnabledModuleDescriptorsByClass(ProjectOperationModuleDescriptor.class)).
                thenReturn
                        (
                                ImmutableList.of
                                        (
                                                projectOperationModuleDescriptor1, projectOperationModuleDescriptor2,
                                                projectOperationModuleDescriptor
                                        )
                        );


        ProjectOperationManager vp = new DefaultProjectOperationManager(mockPluginAccessor);

        List operationDescriptors = vp.getVisibleProjectOperations(null, loggedInUser);
        assertNotNull(operationDescriptors);
        assertEquals(2, operationDescriptors.size());
        //assert the right order is returned. and the exception throwing module is rejected
        assertEquals(projectOperation, operationDescriptors.get(0));
        assertEquals(projectOperation2, operationDescriptors.get(1));
    }

    private ProjectOperationModuleDescriptor getMockProjectOperationModuleDescriptor(PluggableProjectOperation pluggableProjectOperation, int order)
    {
        return getMockProjectOperationModuleDescriptor(pluggableProjectOperation, order, false);
    }

    private ProjectOperationModuleDescriptor getMockProjectOperationModuleDescriptor(PluggableProjectOperation pluggableProjectOperation, int order, boolean throwExceptionOnGetModule)
    {
        final ProjectOperationModuleDescriptor descriptor = mock(ProjectOperationModuleDescriptor.class);

        when(descriptor.getOrder()).thenReturn(order);

        if (throwExceptionOnGetModule)
        {
            when(descriptor.getModule()).thenThrow(new RuntimeException("You asked for it!"));
            when(descriptor.getCompleteKey()).thenReturn("Bad news bear!");
        }
        else
        {
            when(descriptor.getModule()).thenReturn(pluggableProjectOperation);
        }
        return descriptor;
    }

    static class Stubs
    {
        static class ProjectOperation
        {
            static Builder descriptorFor(final PluggableProjectOperation operation)
            {
                return new Builder(operation);
            }

            static class Builder
            {
                private final PluggableProjectOperation operation;
                private int order;
                private boolean shouldThrowOnGetModule = false;

                public Builder(PluggableProjectOperation operation)
                {
                    this.operation = operation;
                }

                public Builder withOrder(int order)
                {
                    this.order = order;
                    return this;
                }

                public Builder throwExceptionOnGetModule(boolean shouldThrowOnGetModule)
                {
                    this.shouldThrowOnGetModule = shouldThrowOnGetModule;
                    return this;
                }

                public ProjectOperationModuleDescriptor get()
                {
                    final ProjectOperationModuleDescriptor descriptor = mock(ProjectOperationModuleDescriptor.class);

                    when(descriptor.getOrder()).thenReturn(order);

                    if (shouldThrowOnGetModule)
                    {
                        when(descriptor.getModule()).thenThrow(new RuntimeException("You asked for it!"));
                        when(descriptor.getCompleteKey()).thenReturn("Bad news bear!");
                    }
                    else
                    {
                        when(descriptor.getModule()).thenReturn(operation);
                    }
                    return descriptor;
                }
            }
        }
    }
}
