package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

/**
 * @since v6.3
 */
public class LicenseControl extends BackdoorControl<LicenseControl>
{
    LicenseControl(final JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void set(String license)
    {
        createLicenseResource().type(MediaType.TEXT_PLAIN_TYPE).put(license);
    }

    private WebResource createLicenseResource()
    {
        return createResource().path("license");
    }
}
