package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.json.JSONTokener;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class for making REST calls to JIRA servers.
 * Supported REST API versions are:
 *     - 2.0alpha1
 *     - 2
 *
 * @since v5.0
 */
@Component
@ExportAsService
public class RemoteJiraRestServiceImpl implements RemoteJiraRestService
{
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String REST_BASE_URL = "rest/api/";
    private static final String ISSUE_RESOURCE = "issue";
    private static final String REMOTE_LINK_RESOURCE = "remotelink";

    private final InternalHostApplication internalHostApplication;
    private final RequestFactory<Request<?, Response>> requestFactory;

    @Autowired
    public RemoteJiraRestServiceImpl(
            @ComponentImport final InternalHostApplication internalHostApplication,
            @ComponentImport final RequestFactory<Request<?, Response>> requestFactory)
    {
        this.internalHostApplication = internalHostApplication;
        this.requestFactory = requestFactory;
    }

    public RemoteResponse<RemoteJiraIssue> getIssue(final ApplicationLink applicationLink, final String issueIdOrKey, final RestVersion restVersion) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createGetIssueRequest(applicationLink, issueIdOrKey, restVersion);
        final ResponseToIssueConverter jsonConverter = new ResponseToIssueConverter(applicationLink.getDisplayUrl().toASCIIString(), restVersion);
        return request.execute(new RestResponseHandler<RemoteJiraIssue>(jsonConverter, restVersion));
    }

    public RemoteResponse<RemoteJiraIssue> getIssue(final String baseUri, final String issueIdOrKey, final RestVersion restVersion) throws CredentialsRequiredException, ResponseException
    {
        final Request<?, Response> request = createGetIssueRequest(baseUri, issueIdOrKey, restVersion);
        final ResponseToIssueConverter jsonConverter = new ResponseToIssueConverter(baseUri, restVersion);
        return request.executeAndReturn(new RestResponseHandler<RemoteJiraIssue>(jsonConverter, restVersion));
    }

    public RemoteResponse<JSONObject> createRemoteIssueLink(final ApplicationLink applicationLink, final String remoteIssueKey, final Issue issue, final String relationship, final RestVersion restVersion) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createCreateRemoteIssueLinkRequest(applicationLink, remoteIssueKey, restVersion);
        request.setRequestContentType(JSON_CONTENT_TYPE);
        request.setRequestBody(getJsonForCreateRemoteIssueLink(issue, relationship));
        return request.execute(new RestResponseHandler<JSONObject>(new ResponseToJsonConverter(), restVersion));
    }

    @Override
    public RemoteResponse<String> requestResource(final ApplicationLink applicationLink, final String resourcePath, final Map<String, String> params, final RestVersion restVersion) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createGetRestResourceRequest(applicationLink, resourcePath, params, restVersion);
        return request.execute(new RestResponseHandler<String>(new ResponseToStringConverter(), restVersion));
    }

    @Override
    public RemoteResponse<String> requestURL(final ApplicationLink applicationLink, final String url, final Map<String, String> params) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createGetURLRequest(applicationLink, url, params);
        return request.execute(new RestResponseHandler<String>(new ResponseToStringConverter(), null));
    }

    private String getJsonForCreateRemoteIssueLink(final Issue issue, final String relationship)
    {
        try
        {
            final JSONObject json = new JSONObject();

            final String globalId = RemoteJiraGlobalIdFactoryImpl.encode(internalHostApplication.getId(), issue.getId());
            json.put("globalId", globalId);

            final JSONObject application = new JSONObject();
            application.put("type", "com.atlassian.jira");
            application.put("name", internalHostApplication.getName());
            json.put("application", application);

            json.put("relationship", relationship);

            // Only store the bare minimum information, the rest will be shown using the renderer plugin
            final JSONObject object = new JSONObject();
            object.put("url", buildIssueUrl(internalHostApplication.getBaseUrl().toASCIIString(), issue.getKey()));
            object.put("title", issue.getKey());
            json.put("object", object);

            return json.toString();
        }
        catch (final JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String buildIssueUrl(final String baseUri, final String issueKey)
    {
        return new UrlBuilder(baseUri)
                .addPathUnsafe("browse")
                .addPath(issueKey)
                .asUrlString();
    }

    private static ApplicationLinkRequest createGetIssueRequest(final ApplicationLink applicationLink, final String issueKey, final RestVersion restVersion) throws CredentialsRequiredException
    {
        final String restUrl = new UrlBuilder(REST_BASE_URL)
                                .addPathUnsafe(restVersion.toString())
                                .addPathUnsafe(ISSUE_RESOURCE)
                                .addPath(issueKey)
                                .asUrlString();
        final ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();
        return requestFactory.createRequest(Request.MethodType.GET, restUrl);
    }

    private Request<?, Response> createGetIssueRequest(final String baseUri, final String issueKey, final RestVersion restVersion) throws CredentialsRequiredException
    {
        final String restUrl = new UrlBuilder(baseUri)
                                .addPathUnsafe(REST_BASE_URL)
                                .addPathUnsafe(restVersion.toString())
                                .addPathUnsafe(ISSUE_RESOURCE)
                                .addPath(issueKey)
                                .asUrlString();
        return requestFactory.createRequest(Request.MethodType.GET, restUrl);
    }

    private static ApplicationLinkRequest createGetRestResourceRequest(final ApplicationLink applicationLink, final String resourcePath, final Map<String, String> params, final RestVersion restVersion) throws CredentialsRequiredException
    {
        final UrlBuilder urlBuilder = new UrlBuilder(REST_BASE_URL)
                                .addPathUnsafe(restVersion.toString())
                                .addPathUnsafe(resourcePath);

        return createGetURLRequest(applicationLink, urlBuilder.asUrlString(), params);
    }

    private static ApplicationLinkRequest createGetURLRequest(final ApplicationLink applicationLink, final String url, final Map<String, String> params) throws CredentialsRequiredException
    {
        final UrlBuilder urlBuilder = new UrlBuilder(url);

        if (params != null)
        {
            for (final Map.Entry<String, String> param : params.entrySet())
            {
                urlBuilder.addParameter(param.getKey(), param.getValue());
            }
        }

        final String restUrl = urlBuilder.asUrlString();
        final ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();
        return requestFactory.createRequest(Request.MethodType.GET, restUrl);
    }

    private static ApplicationLinkRequest createCreateRemoteIssueLinkRequest(final ApplicationLink applicationLink, final String issueKey, final RestVersion restVersion) throws CredentialsRequiredException
    {
        final UrlBuilder urlBuilder = new UrlBuilder(REST_BASE_URL)
                .addPathUnsafe(restVersion.toString())
                .addPathUnsafe(ISSUE_RESOURCE)
                .addPath(issueKey)
                .addPathUnsafe(REMOTE_LINK_RESOURCE);
        final String restUrl = urlBuilder.asUrlString();
        final ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();
        return requestFactory.createRequest(Request.MethodType.POST, restUrl);
    }

    private static ErrorCollection convertResponseToErrorCollection(final JSONObject json, final RestVersion restVersion) throws JSONException
    {
        final ErrorCollection errors = new SimpleErrorCollection();

        switch (restVersion)
        {
            case VERSION_2_0alpha1:
            case VERSION_2:
            {
                final JSONArray errorMessages = json.getJSONArray("errorMessages");
                for (int i = 0; i < errorMessages.length(); i++)
                {
                    errors.addErrorMessage(errorMessages.getString(i));
                }

                final JSONObject errorsMap = json.getJSONObject("errors");
                final Iterator<String> keys = errorsMap.keys();
                while (keys.hasNext())
                {
                    final String key = keys.next();
                    errors.addError(key, errorsMap.getString(key));
                }

                break;
            }
            default:
            {
                throw new UnsupportedOperationException("Unsupported REST version: " + restVersion);
            }
        }

        return errors;
    }

    private static class RestResponseHandler<T> implements ApplicationLinkResponseHandler<RemoteResponse<T>>
    {
        private final ResponseConverter<T> responseConverter;
        private final RestVersion restVersion;

        public RestResponseHandler(final ResponseConverter<T> responseConverter, final RestVersion restVersion)
        {
            this.responseConverter = responseConverter;
            this.restVersion = restVersion;
        }

        @Override
        public RemoteResponse<T> credentialsRequired(Response response) throws ResponseException
        {
            return RemoteResponse.credentialsRequired(response);
        }

        @Override
        public RemoteResponse<T> handle(final Response response) throws ResponseException
        {
            final String responseString = getResponseBodyAsString(response);

            T entity = null;
            ErrorCollection errors = null;
            if (!StringUtils.isBlank(responseString))
            {
                if (response.isSuccessful())
                {
                    entity = responseConverter.convert(responseString);
                }
                else if (restVersion != null)
                {
                    // If it is a REST request, try to convert the response into an ErrorCollection
                    try
                    {
                        final JSONObject json = new JSONObject(new JSONTokener(responseString));
                        errors = convertResponseToErrorCollection(json, restVersion);
                    }
                    catch (final JSONException e)
                    {
                        // We don't want to throw a runtime exception here, as the response may not be JSON
                        // e.g. in the case of the remote server being down
                    }
                }
            }

            return new RemoteResponse<T>(entity, errors, response);
        }
    }

    private interface ResponseConverter<T>
    {
        T convert(String responseString);
    }

    private static class ResponseToJsonConverter implements ResponseConverter<JSONObject>
    {
        @Override
        public JSONObject convert(final String responseString)
        {
            try
            {
                return new JSONObject(new JSONTokener(responseString));
            }
            catch (JSONException e)
            {
                throw new RuntimeException("Failed to parse remote JIRA response", e);
            }
        }
    }

    private static class ResponseToStringConverter implements ResponseConverter<String>
    {
        @Override
        public String convert(final String responseString)
        {
            return responseString;
        }
    }

    private static class ResponseToIssueConverter implements ResponseConverter<RemoteJiraIssue>
    {
        private final String baseUri;
        private final RestVersion restVersion;

        private ResponseToIssueConverter(final String baseUri, final RestVersion restVersion)
        {
            this.baseUri = baseUri;
            this.restVersion = restVersion;
        }

        @Override
        public RemoteJiraIssue convert(final String responseString)
        {
            final RemoteJiraIssueBuilder builder = new RemoteJiraIssueBuilder();

            try
            {
                final JSONObject json = new JSONObject(new JSONTokener(responseString));
                final JSONObject fields = json.getJSONObject("fields");

                switch (restVersion)
                {
                    case VERSION_2:
                    {
                        builder.id(json.getLong("id"));
                        builder.summary(fields.getString("summary"));

                        final String key = json.getString("key");
                        builder.key(key);
                        builder.browseUrl(buildIssueUrl(baseUri, key));

                        final JSONObject issueType = fields.getJSONObject("issuetype");
                        builder.iconUrl(issueType.getString("iconUrl"));
                        builder.iconTitle(appendNameAndDescription(issueType));

                        final JSONObject status = fields.getJSONObject("status");
                        builder.statusIconUrl(status.getString("iconUrl"));
                        builder.statusName(status.getString("name"));
                        builder.statusDescription(status.optString("description"));
                        builder.statusIconTitle(appendNameAndDescription(status));

                        if (status.has("statusCategory"))
                        {
                            final JSONObject statusCategory = status.getJSONObject("statusCategory");
                            builder.statusCategoryKey(statusCategory.getString("key"));
                            builder.statusCategoryColorName(statusCategory.getString("colorName"));
                        }

                        builder.resolved(!fields.isNull("resolution"));
                        break;
                    }
                    case VERSION_2_0alpha1:
                    {
                        throw new UnsupportedOperationException("Currently only REST API version 2 is supported");
                    }
                }
            }
            catch (JSONException e)
            {
                throw new RuntimeException("Failed to parse remote JIRA response", e);
            }

            return builder.build();
        }
    }

    private static String appendNameAndDescription(final JSONObject json) throws JSONException
    {
        final String name = json.getString("name");
        final String description = json.optString("description");

        if (StringUtils.isBlank(description))
        {
            return name;
        }

        return name + " - " + description;
    }

    private static String getResponseBodyAsString(final Response response) throws ResponseException
    {
        // Avoids a warning in the logs about buffering a response of unknown length.
        // TODO: This is a DoS vector, but considered relatively safe for now, as remote JIRA must be applinked.
        final InputStream responseBodyStream = response.getResponseBodyAsStream();
        final String charset = getCharset(response);
        try
        {
            return IOUtil.toString(responseBodyStream, charset);
        }
        catch (final IOException exception)
        {
            throw new ResponseException("Failed to read remote JIRA issue", exception);
        }
    }

    private static String getCharset(final Response response)
    {
        final String DEFAULT = "UTF-8";
        final String contentTypeString = response.getHeader("Content-Type");
        if (contentTypeString == null) {
            return DEFAULT;
        }
        final ContentType contentType;
        try
        {
            contentType = new ContentType(contentTypeString);
        }
        catch (final ParseException exception)
        {
            return DEFAULT;
        }
        return StringUtils.defaultIfEmpty(contentType.getParameter("charset"), DEFAULT);
    }
}
