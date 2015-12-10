package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.remote.UnauthorisedAppLink;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseStatusException;

import org.apache.log4j.Logger;

public class RemoteProjectServiceImpl implements RemoteProjectService
{
    private static final Logger log = Logger.getLogger(RemoteProjectServiceImpl.class);
    private final ApplicationLinkService applicationLinkService;

    public RemoteProjectServiceImpl(final ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    @Override
    public RemoteProject getRemoteProject(final ApplicationLink applicationLink, final String key)
            throws CredentialsRequiredException, ResponseException
    {
        ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();
        ApplicationLinkRequest req = requestFactory.createRequest(Request.MethodType.GET, "/rest/api/2/project/" + key);

        final String response;
        try
        {
            response = req.execute();
        }
        catch (ResponseStatusException ex)
        {
            // Check the status code
            int statusCode = ex.getResponse().getStatusCode();
            if (statusCode == 401)
            {
                // need to authenticate - not sure why app links didn't throw CredentialsRequiredException, but it
                // will sometimes do this when you revoke your auth token.
                throw new CredentialsRequiredException(applicationLink.createAuthenticatedRequestFactory(), ex.getMessage());
            }
            if (statusCode == 404)
            {
                // Project Not Found
                return null;
            }
            // Other status codes not handled yet.
            throw ex;
        }
        try
        {
            return RemoteProject.from(applicationLink, new JSONObject(response));
        }
        catch (JSONException ex)
        {
            throw new RuntimeException("Unable to parse project JSON response " + response, ex);
        }
    }

    @Override
    public RemoteProjectsResult findAllRemoteProjects()
    {
        final List<RemoteProject> projects = new LinkedList<RemoteProject>();
        final Collection<UnauthorisedAppLink> unauthorisedAppLinks = new LinkedList<UnauthorisedAppLink>();
        final Collection<String> connectionErrors = new LinkedList<String>();

        // TODO: This should be concurrent
        for (ApplicationLink applicationLink : applicationLinkService.getApplicationLinks())
        {
            log.debug("Gathering projects for " + applicationLink.getName() + "...");
            try
            {
                projects.addAll(getRemoteProjects(applicationLink));
            }
            catch (CredentialsRequiredException ex)
            {
                unauthorisedAppLinks.add(new UnauthorisedAppLink(applicationLink, ex));
            }
            catch (ResponseStatusException ex)
            {
                // Check the status code
                int statusCode = ex.getResponse().getStatusCode();
                if (statusCode == 401)
                {
                    // need to authenticate - not sure why app links didn't throw CredentialsRequiredException, but it
                    // will sometimes do this when you revoke your auth token.
                    unauthorisedAppLinks.add(new UnauthorisedAppLink(applicationLink, applicationLink.createAuthenticatedRequestFactory()));
                }
                else
                {
                    connectionErrors.add("Unexpected error when connecting to '" + applicationLink.getName() + "' (" + ex.getMessage() + ")");
                }
            }
            catch (ResponseException ex)
            {
                connectionErrors.add("Unexpected error when connecting to '" + applicationLink.getName() + "' (" + ex.getMessage() + ")");
            }
        }

        return new RemoteProjectsResult(projects, unauthorisedAppLinks, connectionErrors);
    }

    private Collection<RemoteProject> getRemoteProjects(final ApplicationLink applicationLink)
            throws CredentialsRequiredException, ResponseException
    {
        ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();
        ApplicationLinkRequest req = requestFactory.createRequest(Request.MethodType.GET, "/rest/api/2/project");
        return parseProjects(req.execute(), applicationLink);
    }

    private Collection<RemoteProject> parseProjects(final String response, final ApplicationLink applicationLink)
            throws ResponseException, CredentialsRequiredException
    {
        try
        {
            JSONArray json = new JSONArray(response);
            int length = json.length();
            final Collection<RemoteProject> projects = new ArrayList<RemoteProject>(length);
            for (int i = 0; i < length; i++)
            {
                projects.add(RemoteProject.from(applicationLink, json.getJSONObject(i)));
            }
            return projects;
        }
        catch (JSONException ex)
        {
            throw new RuntimeException("Unable to parse the JSON response for get projects : " + response, ex);
        }
    }
}
