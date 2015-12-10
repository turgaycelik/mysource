package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component
public class ConfluencePageServiceImpl implements ConfluencePageService
{
    @Override
    public RemoteResponse<String> getPageId(final ApplicationLink applicationLink, String pageUrl) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();

        // Strip fragment identifier
        final int hash = pageUrl.indexOf('#');
        if (hash >= 0) {
            pageUrl = pageUrl.substring(0, hash);
        }

        final ApplicationLinkRequest request = requestFactory.createRequest(Request.MethodType.GET, pageUrl);

        request.setFollowRedirects(false);
        return request.execute(new PageResponseHandler(requestFactory));
    }

    private static class PageResponseHandler implements ApplicationLinkResponseHandler<RemoteResponse<String>>
    {
        private static final int MAX_REDIRECTS = 3;

        private static final Logger LOG = LoggerFactory.getLogger(PageResponseHandler.class);

        private static final Pattern JSESSIONID_PATTERN = Pattern.compile("(.*);jsessionid=\\w+(.*)");

        private final ApplicationLinkRequestFactory requestFactory;

        private int redirectCount = 0;

        public PageResponseHandler(ApplicationLinkRequestFactory requestFactory)
        {
            this.requestFactory = requestFactory;
        }

        @Override
        public RemoteResponse<String> credentialsRequired(Response response) throws ResponseException
        {
            return RemoteResponse.credentialsRequired(response);
        }

        @Override
        public RemoteResponse<String> handle(final Response response) throws ResponseException
        {
            // Handle possible redirects
            // TODO: remove this logic when APL-820 has been fixed
            if (response.getStatusCode() >= 300 && response.getStatusCode() < 400)
            {
                redirectCount++;

                final String location = stripJsessionid(response.getHeader("location"));
                if (isNotBlank(location))
                {
                    if (redirectCount <= MAX_REDIRECTS)
                    {
                        try
                        {
                            final ApplicationLinkRequest request = requestFactory.createRequest(Request.MethodType.GET, location);
                            request.setFollowRedirects(false);
                            return request.execute(this);
                        }
                        catch (CredentialsRequiredException e)
                        {
                            return RemoteResponse.credentialsRequired(response);
                        }
                    }
                    else
                    {
                        LOG.warn("Maximum of " + MAX_REDIRECTS + " redirects reached. Not following redirect to '" + location + "' , returning response instead.");
                    }
                }
                else
                {
                    LOG.warn("HTTP response returned redirect code " + response.getStatusCode() + " but did not provide a location header or the location header did not contain a value");
                }
            }

            final String responseString = getResponseBodyAsString(response);

            String pageId = null;
            if (!StringUtils.isBlank(responseString))
            {
                pageId = parsePageSource(responseString);
            }

            return new RemoteResponse<String>(pageId, response);
        }

        private String stripJsessionid(String url)
        {
            final Matcher matcher = JSESSIONID_PATTERN.matcher(url);
            if (matcher.matches())
            {
                return matcher.group(1) + matcher.group(2);
            }
            return url;
        }

        private String parsePageSource(final String responseString)
        {
            final Pattern p = Pattern.compile("<meta name=\"ajs-page-id\" content=\"(\\d+)\">");
            final Matcher m = p.matcher(responseString);
            if (m.find())
            {
                return m.group(1);
            }

            // Page id not found in page source
            return null;
        }
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
