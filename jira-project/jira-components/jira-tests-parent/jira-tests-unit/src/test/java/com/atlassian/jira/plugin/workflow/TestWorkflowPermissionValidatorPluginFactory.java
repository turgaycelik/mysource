package com.atlassian.jira.plugin.workflow;

import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestWorkflowPermissionValidatorPluginFactory extends AbstractTestWorkflowPermissionPluginFactory<WorkflowPermissionValidatorPluginFactory, ValidatorDescriptor>
{
    @Override
    protected WorkflowPermissionValidatorPluginFactory createFactory()
    {
        return new WorkflowPermissionValidatorPluginFactory(authenticationContext, permissionManager);
    }

    @Override
    protected ValidatorDescriptor createDescriptor()
    {
        ValidatorDescriptor descriptor = mock(ValidatorDescriptor.class);
        when(descriptor.getArgs()).thenReturn(descriptorArgs);
        return descriptor;
    }

    @Test
    public void beforeSaveOnEditClearsLegacyPermissionArgumentFromDescriptor()
    {
        descriptorArgs.put("permission", "legacy permission");

        factory.beforeSaveOnEdit(descriptor);

        assertThat(descriptorArgs.containsKey("permission"), is(false));
    }
}
