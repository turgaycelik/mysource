package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.testkit.client.FieldConfigurationControl;
import com.atlassian.jira.testkit.client.JIRAEnvironmentData;

/**
 * See FieldConfigurationBackdoorExt for the code this plugs into at the back-end.
 *
 * @since v5.2
 */
public class FieldConfigurationControlExt extends FieldConfigurationControl
{
    public FieldConfigurationControlExt(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Copies an existing field configuration. The new field configuration will have the name
     * <code>"Copy of " + original name</code> unless specified.
     *
     * @param name the name of the existing field configuration
     * @param copyName the name of the new field configuration
     */
    public void copyFieldConfiguration(String name, String copyName)
    {
        get(createResource().path("fieldConfiguration/copy")
                .queryParam("name", name)
                .queryParam("copyName", copyName)
        );
    }

    public void setFieldRenderer(String fieldConfirationName, String fieldId, String renderer) {
        get(createResource().path("fieldConfiguration/renderer")
                .queryParam("fieldConfigurationName", fieldConfirationName)
                .queryParam("fieldId", fieldId)
                .queryParam("renderer", renderer));
    }

    @Override
    protected String getRestModulePath()
    {
        return FunctTestConstants.FUNC_TEST_PLUGIN_REST_PATH;
    }
}
