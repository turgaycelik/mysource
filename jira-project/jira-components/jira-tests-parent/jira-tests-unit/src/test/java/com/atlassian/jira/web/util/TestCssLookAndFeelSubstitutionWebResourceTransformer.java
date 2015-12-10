package com.atlassian.jira.web.util;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.ui.header.CurrentHeader;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.WebResourceIntegration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestCssLookAndFeelSubstitutionWebResourceTransformer
{
    private MockApplicationProperties properties;
    private LookAndFeelBean lAndF;

    @Before
    public void setUp() throws Exception
    {
        properties = new MockApplicationProperties();
        properties.setString(APKeys.JIRA_LF_LOGO_URL, "http://foo/bar.png");
        properties.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, "#112233");

        final CurrentHeader currentHeader = mock(CurrentHeader.class);

        when(currentHeader.get()).thenReturn(CurrentHeader.Header.CLASSIC);

        lAndF = new LookAndFeelBean(properties){
            protected CurrentHeader getCurrentHeader()
            {
                return currentHeader;
            };
        };

    }

    @After
    public void tearDown() throws Exception
    {
        ExecutingHttpRequest.clear();
    }

    @Test
    public void testSubstitutions() {
        mockRequest("/jira");

        String input = "@logoUrl @topBackgroundColour @topBackgroundColourNoHash @contextPath/foo.png @nochange";
        assertEquals("http://foo/bar.png #112233 112233 /jira/foo.png @nochange", transform(input, lAndF));
    }

    @Test
    public void testChangesToLookAndFeel() {
        mockRequest("/jira");

        String input = "@logoUrl @topBackgroundColour @topBackgroundColourNoHash @contextPath/foo.png @nochange";
        assertEquals("http://foo/bar.png #112233 112233 /jira/foo.png @nochange", transform(input, lAndF));

        properties.setString(APKeys.JIRA_LF_TOP_BGCOLOUR, "#445566");
        assertEquals("http://foo/bar.png #445566 445566 /jira/foo.png @nochange", transform(input, lAndF));
    }

    @Test
    public void testContextPath() {

        String input = "@contextPath/foo.png";

        mockRequest("/jira");
        assertEquals("/jira/foo.png", transform(input, lAndF));

        mockRequest("");
        assertEquals("/foo.png", transform(input, lAndF));
    }

    private void mockRequest(String contextPath)
    {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(contextPath);
        ExecutingHttpRequest.set(request,null);
    }

    private String transform(String input, LookAndFeelBean lAndF)
    {
        DownloadableResource resource = mock(DownloadableResource.class);
        WebResourceIntegration wri = mock(WebResourceIntegration.class);
        when(wri.getSystemBuildNumber()).thenReturn("4321");
        when(wri.getSystemCounter()).thenReturn("9876");
        CssSubstitutionWebResourceTransformer.CssSubstitutionDownloadableResource r = new CssSubstitutionWebResourceTransformer.CssSubstitutionDownloadableResource(resource, lAndF, wri);
        return r.transform(input);
    }

}
