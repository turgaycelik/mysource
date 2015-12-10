package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TestJiraRendererConfiguration extends MockControllerTestCase
{

    private ApplicationProperties mockApplicationProperties;
    private VelocityRequestContextFactory mockVelocityRequestContextFactory;
    private VelocityRequestContext mockVelocityRequestContext;
    private JiraRendererConfiguration jiraRendererConfig;

    private final static String BASE_HTTP_URL = "http://localhost:8080/jira";
    private final static String BASE_HTTPS_URL_NOCONTEXT = "https://localhost:8443";

    @Before
    public void setupMocks()
    {
        mockApplicationProperties = getMock(ApplicationProperties.class);
        mockVelocityRequestContextFactory = getMock(VelocityRequestContextFactory.class);
        mockVelocityRequestContext = getMock(VelocityRequestContext.class);
        jiraRendererConfig = new JiraRendererConfiguration(mockApplicationProperties, mockVelocityRequestContextFactory);
        JiraTestUtil.resetRequestAndResponse();
    }

    @Test
    public void getWebAppContextPathShouldReturnContextPath()
    {
        expect(mockVelocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(mockVelocityRequestContext);
        expect(mockVelocityRequestContext.getBaseUrl()).andReturn(BASE_HTTP_URL);
        replay();
        assertEquals("Base  path should be base url", BASE_HTTP_URL, jiraRendererConfig.getWebAppContextPath());
    }

    @Test
    public void getWebAppContextPathShouldReturnCanonicalPathWithNoContext()
    {
        expect(mockVelocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(mockVelocityRequestContext).times(2);
        expect(mockVelocityRequestContext.getBaseUrl()).andReturn("");
        expect(mockVelocityRequestContext.getCanonicalBaseUrl()).andReturn(BASE_HTTPS_URL_NOCONTEXT);
        replay();
        assertEquals("Base  path should be canonical", BASE_HTTPS_URL_NOCONTEXT, jiraRendererConfig.getWebAppContextPath());
    }
}
