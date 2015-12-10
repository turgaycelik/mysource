package com.atlassian.jira.plugin.link.confluence.service.rpc;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.confluence.Builder;
import com.atlassian.jira.plugin.link.confluence.ConfluencePage;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSearchResult;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSpace;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Component
@ExportAsService
public class ConfluenceRpcServiceImpl implements ConfluenceRpcService
{
    private static final String XMLRPC_URL = "rpc/xmlrpc";
    private static Set<String> SEARCH_TYPES = ImmutableSet.of("page", "blogpost");

    // We need to build the XML payload manually, as we need to attach it to an applinks authenticated request
    private static final String PAGE_XML_TEMPLATE =
            "<?xml version=\"1.0\"?>" +
            "<method>" +
                "<methodName>confluence2.getPage</methodName>" +
                "<params>" +
                    "<param>" +
                        "<value></value>" +
                        "<value>%s</value>" +
                    "</param>" +
                "</params>" +
            "</method >";

    private static final String BLOG_POST_XML_TEMPLATE =
            "<?xml version=\"1.0\"?>" +
            "<method>" +
                "<methodName>confluence2.getBlogEntry</methodName>" +
                "<params>" +
                    "<param>" +
                        "<value></value>" +
                        "<value>%s</value>" +
                    "</param>" +
                "</params>" +
            "</method >";

    private static final String SPACE_XML =
            "<?xml version=\"1.0\"?>" +
            "<method>" +
                "<methodName>confluence2.getSpaces</methodName>" +
                "<params>" +
                    "<param>" +
                        "<value></value>" +
                    "</param>" +
                "</params>" +
            "</method >";

    private static final String SEARCH_XML_TEMPLATE =
            "<?xml version=\"1.0\"?>" +
            "<method>" +
                "<methodName>confluence2.search</methodName>" +
                "<params>" +
                    "<param>" +
                        "<value></value>" +
                        "<value><string>%s</string></value>" +
                        "<value><int>%d</int></value>" +
                    "</param>" +
                "</params>" +
            "</method >";

    private static final String SEARCH_XML_TEMPLATE_WITH_SPACE_PARAM =
            "<?xml version=\"1.0\"?>" +
            "<method>" +
                "<methodName>confluence2.search</methodName>" +
                "<params>" +
                    "<param>" +
                        "<value></value>" +
                        "<value><string>%s</string></value>" +
                        "<value>" +
                            "<struct>" +
                                "<member>" +
                                    "<name>spaceKey</name>" +
                                    "<value><string>%s</string></value>" +
                                "</member>" +
                            "</struct>" +
                        "</value>" +
                        "<value><int>%d</int></value>" +
                    "</param>" +
                "</params>" +
            "</method >";

    private static final Logger log = LoggerFactory.getLogger(ConfluenceRpcServiceImpl.class);


    @Override
    public RemoteResponse<ConfluencePage> getPage(final ApplicationLink applicationLink, final String pageId) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createGetPageRequest(applicationLink, pageId);
        final RemoteResponse<ConfluencePage> response = request.execute(new PageResponseHandler());

        // The getPages RPC method only returns content of type 'page'.
        // If we are looking for a blog post, we need to make a separate call. We can tell its a blog post if there is a
        // ClassCastException error message.
        if (response.containsErrorWithText("ClassCastException", "BlogPost"))
        {
            return getBlogPost(applicationLink, pageId);
        }

        return response;
    }

    private RemoteResponse<ConfluencePage> getBlogPost(final ApplicationLink applicationLink, final String pageId) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createGetBlogPostRequest(applicationLink, pageId);
        return request.execute(new PageResponseHandler());
    }

    @Override
    public RemoteResponse<List<ConfluenceSpace>> getSpaces(final ApplicationLink applicationLink) throws CredentialsRequiredException, ResponseException
    {
        final ApplicationLinkRequest request = createGetSpacesRequest(applicationLink);
        return request.execute(new SpaceResponseHandler());
    }

    @Override
    public RemoteResponse<List<ConfluenceSearchResult>> search(final ApplicationLink applicationLink, final String query, final int maxResults, final @Nullable String spaceKey) throws CredentialsRequiredException, ResponseException
    {
        if (maxResults < 0)
        {
            throw new IllegalArgumentException("maxResults must be positive");
        }
        
        // Since we need to filter on multiple content types, we need to filter the type after we get the results.
        // This means we need to search for more results than we want, to account for losing some when filtering on the content type.
        final ApplicationLinkRequest request = createSearchRequest(applicationLink, query, maxResults * 2, spaceKey);
        return request.execute(new SearchResponseHandler(maxResults));
    }

    private static ApplicationLinkRequest createGetPageRequest(final ApplicationLink applicationLink, final String pageId) throws CredentialsRequiredException
    {
        final String bodyXml = String.format(PAGE_XML_TEMPLATE, pageId);
        return createRequest(applicationLink, bodyXml);
    }

    private static ApplicationLinkRequest createGetBlogPostRequest(final ApplicationLink applicationLink, final String pageId) throws CredentialsRequiredException
    {
        final String bodyXml = String.format(BLOG_POST_XML_TEMPLATE, pageId);
        return createRequest(applicationLink, bodyXml);
    }

    private static ApplicationLinkRequest createGetSpacesRequest(final ApplicationLink applicationLink) throws CredentialsRequiredException
    {
        return createRequest(applicationLink, SPACE_XML);
    }

    private static ApplicationLinkRequest createSearchRequest(final ApplicationLink applicationLink, final String query, final int maxResults, final String spaceKey) throws CredentialsRequiredException
    {
        final String bodyXml;
        if (spaceKey == null)
        {
            bodyXml = String.format(SEARCH_XML_TEMPLATE, query, maxResults);
        }
        else
        {
            bodyXml = String.format(SEARCH_XML_TEMPLATE_WITH_SPACE_PARAM, query, spaceKey, maxResults);
        }

        return createRequest(applicationLink, bodyXml);
    }

    private static ApplicationLinkRequest createRequest(final ApplicationLink applicationLink, final String bodyXml) throws CredentialsRequiredException
    {
        final ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();
        final ApplicationLinkRequest request = requestFactory.createRequest(Request.MethodType.POST, XMLRPC_URL);
        request.setRequestContentType(MediaType.APPLICATION_XML);
        request.setRequestBody(bodyXml);

        return request;
    }

    private static class PageResponseHandler implements ApplicationLinkResponseHandler<RemoteResponse<ConfluencePage>>
    {
        @Override
        public RemoteResponse<ConfluencePage> credentialsRequired(Response response) throws ResponseException
        {
            return RemoteResponse.credentialsRequired(response);
        }

        @Override
        public RemoteResponse<ConfluencePage> handle(final Response response) throws ResponseException
        {
            final RemoteResponse<List<ConfluencePage>> handledResponse = handleResponse(response, new PageResponseSaxHandler());
            final List<ConfluencePage> pages = handledResponse.getEntity();
            final ConfluencePage page = (pages == null) ? null : Iterables.getOnlyElement(pages, null);

            return new RemoteResponse<ConfluencePage>(page, handledResponse.getErrors(), response);
        }
    }

    private static class SpaceResponseHandler implements ApplicationLinkResponseHandler<RemoteResponse<List<ConfluenceSpace>>>
    {
        @Override
        public RemoteResponse<List<ConfluenceSpace>> credentialsRequired(Response response) throws ResponseException
        {
            return RemoteResponse.credentialsRequired(response);
        }

        @Override
        public RemoteResponse<List<ConfluenceSpace>> handle(final Response response) throws ResponseException
        {
            return handleResponse(response, new SpaceResponseSaxHandler());
        }
    }

    private static class SearchResponseHandler implements ApplicationLinkResponseHandler<RemoteResponse<List<ConfluenceSearchResult>>>
    {
        private final int maxResults;

        private SearchResponseHandler(final int maxResults)
        {
            this.maxResults = maxResults;
        }

        @Override
        public RemoteResponse<List<ConfluenceSearchResult>> credentialsRequired(Response response)
                throws ResponseException
        {
            return RemoteResponse.credentialsRequired(response);
        }

        @Override
        public RemoteResponse<List<ConfluenceSearchResult>> handle(final Response response) throws ResponseException
        {
            final RemoteResponse<List<ConfluenceSearchResult>> handledResponse = handleResponse(response, new SearchResponseSaxHandler());
            final List<ConfluenceSearchResult> searchResults = filterResults(handledResponse.getEntity());

            return new RemoteResponse<List<ConfluenceSearchResult>>(searchResults, response);
        }

        private List<ConfluenceSearchResult> filterResults(final List<ConfluenceSearchResult> results)
        {
            final Iterable<ConfluenceSearchResult> iterable = Iterables.filter(results, new Predicate<ConfluenceSearchResult>()
            {
                @Override
                public boolean apply(final @Nullable ConfluenceSearchResult input)
                {
                    return SEARCH_TYPES.contains(input.getType());
                }
            });

            final List<ConfluenceSearchResult> filtered = Lists.newArrayList(iterable);
            if (filtered.size() > maxResults)
            {
                return filtered.subList(0, maxResults);
            }

            return filtered;
        }
    }

    private static <T, B extends Builder<T>> RemoteResponse<List<T>> handleResponse(final Response response, final AbstractConfluenceSaxHandler<T, B> handler) throws ResponseException
    {
        final String responseString = getResponseBodyAsString(response);

        List<T> entities = null;
        ErrorCollection errors = null;
        if (response.isSuccessful() && !StringUtils.isBlank(responseString))
        {
            entities = parseXml(responseString, handler);
            if (handler.hasFault())
            {
                errors = new SimpleErrorCollection();
                errors.addErrorMessage(handler.getFaultString());
            }
        }

        return new RemoteResponse<List<T>>(entities, errors, response);
    }

    private static <T, B extends Builder<T>> List<T> parseXml(final String responseString, final AbstractConfluenceSaxHandler<T, B> handler)
    {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try
        {
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
            final SAXParser saxParser = factory.newSAXParser();
            final InputStream inputStream = new ByteArrayInputStream(responseString.getBytes());
            saxParser.parse(inputStream, handler);

            return handler.getEntities();
        }
        catch (final ParserConfigurationException e)
        {
            throw new RuntimeException("Failed to parse Confluence Remote API response", e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Failed to parse Confluence Remote API response", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to parse Confluence Remote API response", e);
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
