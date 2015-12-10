package com.atlassian.jira.remote;

import javax.annotation.concurrent.Immutable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.AuthorisationURIGenerator;

/**
 * Represents an ApplicationLink that we cannot make requests to until the user authenticates themselves (does the OAuth dance).
 *
 * @see com.atlassian.jira.project.RemoteProjectService.RemoteProjectsResult
 */
@ExperimentalApi
@Immutable
public final class UnauthorisedAppLink
{
    private final ApplicationLink applicationLink;
    private final AuthorisationURIGenerator authorisationURIGenerator;

    public UnauthorisedAppLink(final ApplicationLink applicationLink, final AuthorisationURIGenerator authorisationURIGenerator)
    {
        this.applicationLink = applicationLink;
        this.authorisationURIGenerator = authorisationURIGenerator;
    }

    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }

    public AuthorisationURIGenerator getAuthorisationURIGenerator()
    {
        return authorisationURIGenerator;
    }
}
