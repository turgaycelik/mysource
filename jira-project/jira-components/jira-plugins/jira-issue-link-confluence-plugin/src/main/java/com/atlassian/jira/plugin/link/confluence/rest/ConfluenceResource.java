package com.atlassian.jira.plugin.link.confluence.rest;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSearchResult;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSearchResult.ConfluenceSearchResultBuilder;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSpace;
import com.atlassian.jira.plugin.link.confluence.rest.ConfluenceSearchResponseBean.Result;
import com.atlassian.jira.plugin.link.confluence.rest.ConfluenceSpaceResponseBean.Space;
import com.atlassian.jira.plugin.link.confluence.service.rpc.ConfluenceRpcService;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrlSwapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * A REST resource to access Confluence's remote API. This is needed to perform authenticated requests.
 *
 * @since v5.0
 */
@AnonymousAllowed
@Path ("confluence")
public class ConfluenceResource
{
    private final ConfluenceRpcService confluenceRpcService;
    private final ApplicationLinkService applicationLinkService;
    private final I18nHelper.BeanFactory beanFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ConfluenceResource(
            final ConfluenceRpcService confluenceRpcService,
            @ComponentImport final ApplicationLinkService applicationLinkService,
            @ComponentImport final I18nHelper.BeanFactory beanFactory,
            @ComponentImport final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.confluenceRpcService = confluenceRpcService;
        this.applicationLinkService = applicationLinkService;
        this.beanFactory = beanFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @GET
    @Path ("/space")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getSpaces(@QueryParam("appId") final String appId)
    {
        final I18nHelper i18n = getI18n(jiraAuthenticationContext.getLoggedInUser());

        final ApplicationLink appLink = getConfluenceAppLink(appId);
        if (appLink == null)
        {
            final ErrorCollection errors = ErrorCollection.of(i18n.getText("addconfluencelink.search.applink.not.found"));
            return Response.status(Status.BAD_REQUEST).entity(errors).cacheControl(never()).build();
        }

        try
        {
            final RemoteResponse<List<ConfluenceSpace>> response = confluenceRpcService.getSpaces(appLink);
            if (!response.isSuccessful())
            {
                return handleUnsuccessfulResponse(response);
            }

            final ConfluenceSpaceResponseBean bean = convertSpacesToBean(response.getEntity());
            return Response.ok(bean).cacheControl(never()).build();
        }
        catch (CredentialsRequiredException e)
        {
            return Response.status(Status.UNAUTHORIZED).cacheControl(never()).build();
        }
        catch (ResponseException e)
        {
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(never()).build();
        }
    }

    @GET
    @Path ("/search")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getSiteSearch(
            @QueryParam ("query") final String query,
            @QueryParam("appId") final String appId,
            @QueryParam("maxResults") final Integer maxResults,
            @QueryParam("spaceKey") final String spaceKey)
    {
        final I18nHelper i18n = getI18n(jiraAuthenticationContext.getLoggedInUser());

        final ApplicationLink appLink = getConfluenceAppLink(appId);
        if (appLink == null)
        {
            final ErrorCollection errors = ErrorCollection.of(i18n.getText("addconfluencelink.search.applink.not.found"));
            return Response.status(Status.BAD_REQUEST).entity(errors).cacheControl(never()).build();
        }

        try
        {
            final RemoteResponse<List<ConfluenceSearchResult>> response = confluenceRpcService.search(appLink, query, maxResults, spaceKey);
            if (!response.isSuccessful())
            {
                return handleUnsuccessfulResponse(response);
            }

            final List<ConfluenceSearchResult> results = convertToDisplayUrl(response.getEntity(), appLink);
            final ConfluenceSearchResponseBean bean = convertSearchResultsToBean(results);
            return Response.ok(bean).cacheControl(never()).build();
        }
        catch (CredentialsRequiredException e)
        {
            return Response.status(Status.UNAUTHORIZED).cacheControl(never()).build();
        }
        catch (ResponseException e)
        {
            return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(never()).build();
        }
    }

    @GET
    @Path ("/applink")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getApplicationLinks()
    {
        final ConfluenceApplicationLinksBean bean = new ConfluenceApplicationLinksBean(getConfluenceAppLinks());
        return Response.ok(bean).cacheControl(never()).build();
    }

    private ApplicationLink getConfluenceAppLink(final String appId)
    {
        for (final ApplicationLink appLink : getConfluenceAppLinks())
        {
            if (appLink.getId().get().equals(appId))
            {
                return appLink;
            }
        }

        return null;
    }

    private Iterable<ApplicationLink> getConfluenceAppLinks()
    {
        return applicationLinkService.getApplicationLinks(ConfluenceApplicationType.class);
    }

    private ConfluenceSpaceResponseBean convertSpacesToBean(final List<ConfluenceSpace> spaces)
    {
        if (spaces == null)
        {
            return new ConfluenceSpaceResponseBean(Collections.<Space>emptyList());
        }

        final List<Space> spaceBeans = new ArrayList<Space>(spaces.size());

        for (final ConfluenceSpace space : spaces)
        {
            spaceBeans.add(new Space(space.getKey(), space.getName(), space.getType(), space.getUrl()));
        }

        return new ConfluenceSpaceResponseBean(spaceBeans);
    }

    private ConfluenceSearchResponseBean convertSearchResultsToBean(final List<ConfluenceSearchResult> searchResults)
    {
        if (searchResults == null)
        {
            return new ConfluenceSearchResponseBean(Collections.<Result>emptyList());
        }

        final List<Result> results = new ArrayList<Result>(searchResults.size());

        for (final ConfluenceSearchResult result : searchResults)
        {
            results.add(new Result(result.getId(), result.getType(), result.getTitle(), result.getExcerpt(), result.getUrl()));
        }

        return new ConfluenceSearchResponseBean(results);
    }

    private List<ConfluenceSearchResult> convertToDisplayUrl(final List<ConfluenceSearchResult> results, final ApplicationLink appLink)
    {
        return Lists.transform(results, new Function<ConfluenceSearchResult, ConfluenceSearchResult>()
        {
            @Override
            public ConfluenceSearchResult apply(final @Nullable ConfluenceSearchResult from)
            {
                return new ConfluenceSearchResultBuilder(from)
                        .url(BaseUrlSwapper.swapRpcUrlToDisplayUrl(from.getUrl(), appLink))
                        .build();
            }
        });
    }

    private Response handleUnsuccessfulResponse(final RemoteResponse<?> response)
    {
        final ErrorCollection errors = ErrorCollection.of(response.getStatusText());
        if (response.hasErrors())
        {
            errors.addErrorCollection(response.getErrors());
        }
        return Response.status(response.getStatusCode()).entity(errors).cacheControl(never()).build();
    }

    private I18nHelper getI18n(User user)
    {
        return beanFactory.getInstance(user);
    }
}
