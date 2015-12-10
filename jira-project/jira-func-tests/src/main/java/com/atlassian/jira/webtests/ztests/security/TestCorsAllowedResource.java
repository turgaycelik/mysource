package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.plugins.rest.common.security.CorsHeaders;
import junit.framework.Assert;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@WebTest({ Category.FUNC_TEST, Category.SECURITY })
public class TestCorsAllowedResource extends FuncTestCase
{
    private final HttpClient client = new HttpClient();

    private static final String allowedOrigin = "http://localhost:8550/";
    private static final String notAllowedOrigin = "http://localhost:8560/";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestCorsAllowedResource.xml");
    }

    public void testRequestToResourceWithCorsAllowedAnnotationFromAllowedOrigin() throws IOException, URISyntaxException
    {
        final String requestURI = restURI("/rest/api/2/project").toString();
        final GetMethod getRequest = new GetMethod(requestURI);
        getRequest.addRequestHeader(CorsHeaders.ORIGIN.value(), allowedOrigin);

        int statusCode = client.executeMethod(getRequest);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        final Header accessControlResponseHeader = getRequest.getResponseHeader(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());
        Assert.assertEquals(allowedOrigin, accessControlResponseHeader.getValue());
    }

    public void testRequestToResourceWithCorsAllowedAnnotationFromNotAllowedOrigin()
            throws IOException, URISyntaxException
    {
        final String requestURI = restURI("/rest/api/2/project").toString();
        final GetMethod getRequest = new GetMethod(requestURI);
        getRequest.addRequestHeader(CorsHeaders.ORIGIN.value(), notAllowedOrigin);

        int statusCode = client.executeMethod(getRequest);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        final Header accessControlResponseHeader = getRequest.getResponseHeader(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());
        Assert.assertNull(accessControlResponseHeader);
    }

    public void testRequestToResourceWithCorsAllowedAnnotationFromAllowedOriginWithPreflight()
            throws IOException, URISyntaxException
    {
        final String requestURI = restURI("/rest/api/2/project").toString();
        final GetMethod getRequest = new GetMethod(requestURI);
        getRequest.addRequestHeader(CorsHeaders.ORIGIN.value(), allowedOrigin);

        int statusCode = client.executeMethod(getRequest);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        final Header accessControlResponseHeader = getRequest.getResponseHeader(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());

        Assert.assertNotNull(accessControlResponseHeader);
    }

    public void testRequestToResourceWithCorsAllowedAnnotationFromNotAllowedOriginWithPreflight()
            throws IOException, URISyntaxException
    {
        final String requestURI = restURI("/rest/api/2/project").toString();
        final GetMethod getRequest = new GetMethod(requestURI);
        getRequest.addRequestHeader(CorsHeaders.ORIGIN.value(), notAllowedOrigin);

        int statusCode = client.executeMethod(getRequest);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        final Header accessControlResponseHeader = getRequest.getResponseHeader(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());

        Assert.assertNull(accessControlResponseHeader);
    }

    public void testRequestToResourceWithoutCorsAllowedAnnotationFromAllowedOrigin()
            throws IOException, URISyntaxException
    {
        client.getParams().setAuthenticationPreemptive(true);
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));
        final String requestURI = restURI("/rest/internal/1.0/darkFeatures/jira.user.darkfeature.admin").toString();
        final GetMethod getRequest = new GetMethod(requestURI);
        getRequest.addRequestHeader(CorsHeaders.ORIGIN.value(), allowedOrigin);

        int statusCode = client.executeMethod(getRequest);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        final Header accessControlResponseHeader = getRequest.getResponseHeader(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());
        Assert.assertNull(accessControlResponseHeader);
    }

    public void testRequestToResourceWithoutCorsAllowedAnnotationFromNotAllowedOrigin()
            throws IOException, URISyntaxException
    {
        client.getParams().setAuthenticationPreemptive(true);
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));
        final String requestURI = restURI("/rest/internal/1.0/darkFeatures/jira.user.darkfeature.admin").toString();
        final GetMethod getRequest = new GetMethod(requestURI);
        getRequest.addRequestHeader(CorsHeaders.ORIGIN.value(), notAllowedOrigin);

        int statusCode = client.executeMethod(getRequest);
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        final Header accessControlResponseHeader = getRequest.getResponseHeader(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());
        Assert.assertNull(accessControlResponseHeader);
    }

    private URI restURI(final String resourceURI) throws URISyntaxException
    {
        final URI jiraURI = environmentData.getBaseUrl().toURI();
        return UriBuilder.fromUri(jiraURI)
                .path(resourceURI)
                .build();
    }

}
