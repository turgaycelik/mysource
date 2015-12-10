package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;

/**
 * @since v6.3
 */
@Path ("license")
@AnonymousAllowed
public class LicenseBackdoor
{
    private final JiraLicenseManager licenseManager;

    public LicenseBackdoor(final JiraLicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    @PUT
    public Response setLicense(String license)
    {
        licenseManager.setLicense(license);
        return Response.ok()
                .cacheControl(never())
                .build();
    }
}
