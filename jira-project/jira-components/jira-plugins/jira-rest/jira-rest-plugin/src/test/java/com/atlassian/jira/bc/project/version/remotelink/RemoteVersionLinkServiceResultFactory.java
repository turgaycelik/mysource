package com.atlassian.jira.bc.project.version.remotelink;

import java.util.List;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.ErrorCollection;

import com.google.common.collect.ImmutableList;

/**
 * To work around the fact that the service results returned by RemoteVersionLinkService
 * intentionally lack public constructors.
 *
 * @since JIRA REST v6.5.1 (JIRA v6.1.1)
 */
public class RemoteVersionLinkServiceResultFactory
{
    public static RemoteVersionLinkService.RemoteVersionLinkResult remoteVersionLinkResult(RemoteVersionLink remoteVersionLink)
    {
        return new RemoteVersionLinkService.RemoteVersionLinkResult(remoteVersionLink);
    }

    public static RemoteVersionLinkService.RemoteVersionLinkResult remoteVersionLinkResult(ErrorCollection errors)
    {
        return new RemoteVersionLinkService.RemoteVersionLinkResult(errors);
    }

    public static RemoteVersionLinkService.RemoteVersionLinkListResult remoteVersionLinkListResult(RemoteVersionLink... remoteVersionLinks)
    {
        return new RemoteVersionLinkService.RemoteVersionLinkListResult(ImmutableList.copyOf(remoteVersionLinks));
    }

    public static RemoteVersionLinkService.RemoteVersionLinkListResult remoteVersionLinkListResult(List<RemoteVersionLink> remoteVersionLinks)
    {
        return new RemoteVersionLinkService.RemoteVersionLinkListResult(remoteVersionLinks);
    }

    public static RemoteVersionLinkService.RemoteVersionLinkListResult remoteVersionLinkListResult(ErrorCollection errors)
    {
        return new RemoteVersionLinkService.RemoteVersionLinkListResult(errors);
    }

    public static RemoteVersionLinkService.PutValidationResult putValidationResult(Version version, String globalId, String json)
    {
        return new RemoteVersionLinkService.PutValidationResult(version, globalId, json);
    }

    public static RemoteVersionLinkService.PutValidationResult putValidationResult(ErrorCollection errors)
    {
        return new RemoteVersionLinkService.PutValidationResult(errors);
    }

    public static RemoteVersionLinkService.DeleteValidationResult deleteValidationResult(Version version, String globalId)
    {
        return new RemoteVersionLinkService.DeleteValidationResult(version, globalId);
    }

    public static RemoteVersionLinkService.DeleteValidationResult deleteValidationResult(ErrorCollection errors)
    {
        return new RemoteVersionLinkService.DeleteValidationResult(errors);
    }

}

