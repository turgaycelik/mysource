package com.atlassian.jira.sal;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.sal.api.UrlMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestJiraApplicationProperties
{
    private JiraApplicationProperties jiraApplicationProperties;

    @Mock private VelocityRequestContextFactory velocityRequestContextFactory;
    @Mock private JiraHome jiraHome;
    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private BuildUtilsInfo buildUtilsInfo;

    @Before
    public void setUp() throws Exception
    {
        jiraApplicationProperties = new JiraApplicationProperties(velocityRequestContextFactory, jiraHome, mockApplicationProperties, buildUtilsInfo);
    }

    @Test
    public void testGetBaseUrlCanonical() throws Exception
    {
        assertUrl("https://www.myjira.com/jira").withRequest().forMode(UrlMode.CANONICAL).now();
    }

    @Test
    public void testGetBaseUrlCanonical_noRequest() throws Exception
    {
        assertUrl("https://www.myjira.com/jira").forMode(UrlMode.CANONICAL).now();
    }

    @Test
    public void testGetBaseUrlAbsolute() throws Exception
    {
        assertUrl("http://my.server.com:8080/myjira").withRequest().forMode(UrlMode.ABSOLUTE).now();
    }

    @Test
    public void testGetBaseUrlAbsolute_noRequest() throws Exception
    {
        assertUrl("https://www.myjira.com/jira").forMode(UrlMode.ABSOLUTE).now();
    }

    @Test
    public void testGetBaseUrlRelative() throws Exception
    {
        assertUrl("/myjira").withRequest().forMode(UrlMode.RELATIVE).now();
    }

    @Test
    public void testGetBaseUrlRelative_noRequest() throws Exception
    {
        assertUrl("/jira").forMode(UrlMode.RELATIVE).now();
    }

    @Test
    public void testGetBaseUrlRelativeCanonical() throws Exception
    {
        assertUrl("/jira").withRequest().forMode(UrlMode.RELATIVE_CANONICAL).now();
    }

    @Test
    public void testGetBaseUrlRelativeCanonical_noRequest() throws Exception
    {
        assertUrl("/jira").forMode(UrlMode.RELATIVE_CANONICAL).now();
    }

    @Test
    public void testGetBaseUrlAuto() throws Exception
    {
        assertUrl("/myjira").withRequest().forMode(UrlMode.AUTO).now();
    }

    @Test
    public void testGetBaseUrlAuto_noRequest() throws Exception
    {
        assertUrl("https://www.myjira.com/jira").forMode(UrlMode.AUTO).now();
    }

    private void setRequestUrl()
    {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("my.server.com");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("/myjira");
        final HttpServletResponse response = mock(HttpServletResponse.class);

        ExecutingHttpRequest.set(request, response);
    }

    private Fixture assertUrl(String url)
    {
        return new Fixture(url);
    }

    private class Fixture
    {
        private String expectedUrl;
        private boolean withRequest = false;
        private UrlMode mode;

        public Fixture(String url)
        {
            this.expectedUrl = url;
        }

        public Fixture withRequest()
        {
            withRequest = true;
            return this;
        }

        public Fixture forMode(UrlMode mode)
        {
            this.mode = mode;
            return this;
        }

        public void now()
        {
            when(mockApplicationProperties.getText(APKeys.JIRA_BASEURL)).thenReturn("https://www.myjira.com/jira");
            if (withRequest)
            {
                setRequestUrl();
            }
            else
            {
                ExecutingHttpRequest.clear();
            }
            final String url = jiraApplicationProperties.getBaseUrl(mode);
            assertThat(url, is(expectedUrl));
        }
    }
}
