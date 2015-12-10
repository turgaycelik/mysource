package com.atlassian.jira.plugin.link.remotejira.rest.resource;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.remotejira.RemoteJiraRestService;
import com.atlassian.jira.plugin.link.remotejira.RemoteJiraRestService.RestVersion;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * A remote JIRA-related REST resource for remote issue links.
 *
 * @since v5.0
 */
@AnonymousAllowed
@Path ("remoteJira")
public class RemoteJiraRestResource
{
    private final static Logger LOG = LoggerFactory.getLogger(RemoteJiraRestResource.class);

    private final ApplicationLinkService applicationLinkService;
    private final RemoteJiraRestService remoteJiraRestService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final I18nHelper.BeanFactory beanFactory;

    public RemoteJiraRestResource(
            @ComponentImport final ApplicationLinkService applicationLinkService,
            final RemoteJiraRestService remoteJiraRestService,
            @ComponentImport final JiraAuthenticationContext jiraAuthenticationContext,
            @ComponentImport final I18nHelper.BeanFactory beanFactory)
    {
        this.applicationLinkService = applicationLinkService;
        this.remoteJiraRestService = remoteJiraRestService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.beanFactory = beanFactory;
    }

    @GET
    @Path ("/picker")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getPicker(@QueryParam("appId") final String appId,
                              @QueryParam("query") final String query,
                              @QueryParam("currentJQL") final String currentJQL,
                              @QueryParam("currentIssueKey") final String currentIssueKey,
                              @QueryParam("currentProjectId") final String currentProjectId,
                              @QueryParam("showSubTasks") final boolean showSubTasks,
                              @QueryParam("showSubTaskParent") final boolean showSubTaskParent)
    {
        final ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<String, String>();
        if (query != null)
        {
            mapBuilder.put("query", query);
        }
        if (currentJQL != null)
        {
            mapBuilder.put("currentJQL", currentJQL);
        }
        if (currentIssueKey != null)
        {
            mapBuilder.put("currentIssueKey", currentIssueKey);
        }
        if (currentProjectId != null)
        {
            mapBuilder.put("currentProjectId", currentProjectId);
        }
        mapBuilder.put("showSubTasks", String.valueOf(showSubTasks));
        mapBuilder.put("showSubTaskParent", String.valueOf(showSubTaskParent));

        return invokeRestRequest(appId, "issues/picker", mapBuilder.build(), RestVersion.VERSION_1);
    }

    @GET
    @Path ("/search")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getSearch(@QueryParam("appId") final String appId,
                              @QueryParam("jql") final String jql,
                              @QueryParam("maxResults") Integer maxResults)
    {
        final ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<String, String>();
        mapBuilder.put("jql", jql);
        if (maxResults != null)
        {
            mapBuilder.put("maxResults", maxResults.toString());
        }
        return invokeRestRequest(appId, "search", mapBuilder.build(), RestVersion.VERSION_2);
    }

    @GET
    @Path ("/autocomplete")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getSearchAutoComplete(@QueryParam("appId") final String appId,
                                          @QueryParam("fieldName") final String fieldName,
                                          @QueryParam("fieldValue") final String fieldValue,
                                          @QueryParam("predicateName") final String predicateName,
                                          @QueryParam("predicateValue") final String predicateValue)
    {
        final ImmutableMap.Builder<String, String> mapBuilder = new ImmutableMap.Builder<String, String>();
        if (fieldName != null)
        {
            mapBuilder.put("fieldName", fieldName);
        }
        if (fieldValue != null)
        {
            mapBuilder.put("fieldValue", fieldValue);
        }
        if (predicateName != null)
        {
            mapBuilder.put("predicateName", predicateName);
        }
        if (predicateValue != null)
        {
            mapBuilder.put("predicateValue", predicateValue);
        }

        return invokeRestRequest(appId, "jql/autocomplete", mapBuilder.build(), RestVersion.VERSION_1);
    }

    @GET
    @Path ("/autocompletedata")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getSearchAutoCompleteData(@QueryParam("appId") final String appId)
    {
        final Map<String, String> params = Collections.emptyMap();
        return invokeRestRequest(appId, "jql/autocompletedata", params, RestVersion.VERSION_2);
    }

    @GET
    @Path ("/autocompletedata/legacy")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getSearchAutoCompleteDataPre51(@QueryParam("appId") final String appId)
    {
        // JIRA instances older than v5.1 will not have the autocompletedata REST endpoint.
        // We can get the autocomplete data by parsing the issue navigator page.
        final Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("navType", "advanced")
                .build();
        return invokeURL(appId, "secure/IssueNavigator.jspa", params, new AutoCompleteDataLegacyConverter());
    }

    private Response invokeRestRequest(final String appId, final String resourcePath, final Map<String, String> params, final RestVersion restVersion)
    {
        final I18nHelper i18n = getI18n(jiraAuthenticationContext.getLoggedInUser());

        final ApplicationLink appLink = getJiraAppLink(appId);
        if (appLink == null)
        {
            final ErrorCollection errors = ErrorCollection.of(i18n.getText("linkjiraissue.error.applink.not.found"));
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).cacheControl(never()).build();
        }

        try
        {
            final RemoteResponse<String> response = remoteJiraRestService.requestResource(appLink, resourcePath, params, restVersion);
            if (!response.isSuccessful())
            {
                return handleUnsuccessfulResponse(response);
            }

            // Pass on the result from the remote JIRA resource as the response from this resource
            return Response.ok(response.getEntity()).cacheControl(never()).build();
        }
        catch (CredentialsRequiredException e)
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(never()).build();
        }
        catch (ResponseException e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).cacheControl(never()).build();
        }
    }

    private Response invokeURL(final String appId, final String url, final Map<String, String> params, final ResponseConverter responseConverter)
    {
        final I18nHelper i18n = getI18n(jiraAuthenticationContext.getLoggedInUser());

        final ApplicationLink appLink = getJiraAppLink(appId);
        if (appLink == null)
        {
            final ErrorCollection errors = ErrorCollection.of(i18n.getText("linkjiraissue.error.applink.not.found"));
            return Response.status(Response.Status.BAD_REQUEST).entity(errors).cacheControl(never()).build();
        }

        try
        {
            final RemoteResponse<String> response = remoteJiraRestService.requestURL(appLink, url, params);
            if (!response.isSuccessful())
            {
                return handleUnsuccessfulResponse(response);
            }

            // Pass on the result from the remote JIRA resource as the response from this resource
            final String entity = (responseConverter == null) ? response.getEntity() : responseConverter.convert(response.getEntity());
            return Response.ok(entity).cacheControl(never()).build();
        }
        catch (CredentialsRequiredException e)
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(never()).build();
        }
        catch (ResponseException e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).cacheControl(never()).build();
        }
    }

    private ApplicationLink getJiraAppLink(final String appId)
    {
        for (final ApplicationLink appLink : applicationLinkService.getApplicationLinks(JiraApplicationType.class))
        {
            if (appLink.getId().get().equals(appId))
            {
                return appLink;
            }
        }

        return null;
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

    private interface ResponseConverter
    {
        String convert(String responseString);
    }

    private static class AutoCompleteDataLegacyConverter implements ResponseConverter
    {
        @Override
        public String convert(final String responseString)
        {
            // The JSON data is stored inside divs with the ids: jqlFieldz, jqlFunctionNamez and jqlReservedWordz
            final int options = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
            final Pattern fieldsPattern = Pattern.compile("<div[^>]*?jqlFieldz[^>]*?>(.*?)</div>", options);
            final Pattern functionsPattern = Pattern.compile("<div[^>]*?jqlFunctionNamez[^>]*?>(.*?)</div>", options);
            final Pattern reservedWordsPattern = Pattern.compile("<div[^>]*?jqlReservedWordz[^>]*?>(.*?)</div>", options);

            return
            "{" +
                "\"visibleFieldNames\": " + find(fieldsPattern, responseString) + "," +
                "\"visibleFunctionNames\": " + find(functionsPattern, responseString) + "," +
                "\"jqlReservedWords\": " + find(reservedWordsPattern, responseString) +
            "}";
        }

        private String find(final Pattern p, final String s)
        {
            final Matcher m = p.matcher(s);
            if (m.find())
            {
                return StringEscapeUtils.unescapeHtml(m.group(1));
            }

            // Return an empty list if we didn't find it
            return "[]";
        }
    }
}
