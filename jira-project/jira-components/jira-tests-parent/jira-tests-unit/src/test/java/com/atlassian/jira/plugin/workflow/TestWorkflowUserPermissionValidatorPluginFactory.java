package com.atlassian.jira.plugin.workflow;

import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestWorkflowUserPermissionValidatorPluginFactory extends TestWorkflowPermissionValidatorPluginFactory
{
    @Override
    protected WorkflowUserPermissionValidatorPluginFactory createFactory()
    {
        return new WorkflowUserPermissionValidatorPluginFactory(authenticationContext, permissionManager);
    }

    @Test
    public void getVelocityParamsForInputPutsNullAllowedOptionsIntoTemplateParameters()
    {
        factory.getVelocityParamsForInput(parameters);

        checkNullAllowedOptions();
    }

    @Test
    public void getVelocityParamsForEditPutsAdditionalOptionsIntoTemplateParameters()
    {
        descriptorArgs.put("permissionKey", projectPermission.getKey());
        descriptorArgs.put("vars.key", "variable");
        descriptorArgs.put("nullallowed", "true");

        factory.getVelocityParamsForEdit(parameters, descriptor);

        checkNullAllowedOptions();

        assertThat((String) parameters.get("vars-key"), equalTo("variable"));
        assertThat((String) parameters.get("nullallowed"), equalTo("true"));
    }

    private void checkNullAllowedOptions()
    {
        @SuppressWarnings("unchecked")
        Map<String, String> options = (Map<String, String>) parameters.get("nullallowedoptions");
        assertThat(options, notNullValue());
    }

    @Test
    public void getVelocityParamsForViewPutsAdditionalOptionsIntoTemplateParameters()
    {
        descriptorArgs.put("permissionKey", projectPermission.getKey());
        descriptorArgs.put("vars.key", "variable");
        descriptorArgs.put("nullallowed", "true");

        when(permissionManager.getProjectPermission(new ProjectPermissionKey(projectPermission.getKey()))).thenReturn(some(projectPermission));

        factory.getVelocityParamsForView(parameters, descriptor);

        assertThat((String) parameters.get("vars-key"), equalTo("variable"));
        assertThat((Boolean) parameters.get("nullallowed"), is(true));
    }
}
