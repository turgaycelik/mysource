package com.atlassian.jira.rest.v2.issue.version;

import java.net.URI;

import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLink;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.PutValidationResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.RemoteVersionLinkListResult;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.RemoteVersionLinkResult;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.entity.AbstractRemoteEntityLinkResource;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import static com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.DeleteValidationResult;


/**
 * Responsible for handling REST calls relating to remote issue links.
 *
 * @since JIRA REST v6.5.1  (in JIRA 6.1.1)
 */
public class RemoteVersionLinkResource extends AbstractRemoteEntityLinkResource<Version,RemoteVersionLink>
{
    private final RemoteVersionLinkService remoteVersionLinkService;



    public RemoteVersionLinkResource(final I18nHelper i18n,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final RemoteVersionLinkService remoteVersionLinkService,
            final JsonEntityPropertyManager jsonEntityPropertyManager,
            final ContextUriInfo contextUriInfo)
    {
        super(i18n, jiraAuthenticationContext, jsonEntityPropertyManager, contextUriInfo);
        this.remoteVersionLinkService = remoteVersionLinkService;
    }



    public Response getRemoteVersionLink(long versionId, String globalId)
    {
        final RemoteVersionLinkResult result = remoteVersionLinkService
                .getRemoteVersionLinkByVersionIdAndGlobalId(getUser(), versionId, globalId);
        return toResponse(valid(result).getRemoteVersionLink());
    }

    public Response getRemoteVersionLinksByVersionId(long versionId)
    {
        final RemoteVersionLinkListResult result = remoteVersionLinkService
                .getRemoteVersionLinksByVersionId(getUser(), versionId);
        return toResponse(valid(result).getRemoteVersionLinks());
    }

    public Response getRemoteVersionLinksByGlobalId(String globalId)
    {
        final RemoteVersionLinkListResult result = remoteVersionLinkService
                .getRemoteVersionLinksByGlobalId(getUser(), globalId);
        return toResponse(valid(result).getRemoteVersionLinks());
    }

    public Response putRemoteVersionLink(long versionId, String globalId, String json)
    {
        final PutValidationResult validation = remoteVersionLinkService
                .validatePut(getUser(), versionId, globalId, json);
        final RemoteVersionLinkResult result = remoteVersionLinkService.put(getUser(), valid(validation));
        return toSuccessfulPostResponse(valid(result).getRemoteVersionLink());
    }

    public Response deleteRemoteVersionLinksByVersionId(long versionId)
    {
        final DeleteValidationResult validation = remoteVersionLinkService
                .validateDeleteByVersionId(getUser(), versionId);
        remoteVersionLinkService.delete(getUser(), valid(validation));
        return toSuccessfulDeleteResponse();
    }

    public Response deleteRemoteVersionLink(long versionId, String globalId)
    {
        final DeleteValidationResult validation = remoteVersionLinkService
                .validateDelete(getUser(), versionId, globalId);
        remoteVersionLinkService.delete(getUser(), valid(validation));
        return toSuccessfulDeleteResponse();
    }


    @Override
    protected URI createSelfLink(final RemoteVersionLink link)
    {
        return contextUriInfo.getBaseUriBuilder()
                .path(VersionResource.class)
                .path(String.valueOf(link.getEntityId()))
                .path("remotelink")
                .path(link.getGlobalId())
                .build();
    }
}
