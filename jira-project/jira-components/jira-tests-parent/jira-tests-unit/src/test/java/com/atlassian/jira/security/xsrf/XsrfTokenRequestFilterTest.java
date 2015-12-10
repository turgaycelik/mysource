package com.atlassian.jira.security.xsrf;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;

import org.junit.Test;

/**
 */
public class XsrfTokenRequestFilterTest extends MockControllerTestCase
{
    @Test
    public void testDoFilter() throws IOException, ServletException
    {
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest();

        final XsrfTokenGenerator xsrfTokenGenerator = mockController.getMock(XsrfTokenGenerator.class);
        xsrfTokenGenerator.generateToken(request);
        mockController.setReturnValue("abc1234");

        XsrfTokenAdditionRequestFilter filter = new AlreadySetupXsrfTokenAdditionRequestFilter(xsrfTokenGenerator);
        mockController.replay();

        filter.doFilter(request, null, filterChain);
    }


    @Test
    public void testDoFilter_NoPrincipal() throws IOException, ServletException
    {
        MockFilterChain filterChain = new MockFilterChain();
        MockHttpServletRequest request = new MockHttpServletRequest()
        {
            @Override
            public Principal getUserPrincipal()
            {
                return null;
            }
        };

        final XsrfTokenGenerator xsrfTokenGenerator = mockController.getMock(XsrfTokenGenerator.class);
        xsrfTokenGenerator.generateToken(request);
        mockController.setReturnValue("abc1234");

        XsrfTokenAdditionRequestFilter filter = new AlreadySetupXsrfTokenAdditionRequestFilter(xsrfTokenGenerator);
        mockController.replay();

        filter.doFilter(request, null, filterChain);
    }

    private static class AlreadySetupXsrfTokenAdditionRequestFilter extends XsrfTokenAdditionRequestFilter
    {

        private final MockApplicationProperties mockApplicationProperties;
        private final XsrfTokenGenerator xsrfTokenGenerator;

        public AlreadySetupXsrfTokenAdditionRequestFilter(XsrfTokenGenerator xsrfTokenGenerator)
        {
            this.xsrfTokenGenerator = xsrfTokenGenerator;
            mockApplicationProperties = new MockApplicationProperties();
            mockApplicationProperties.setString(APKeys.JIRA_SETUP, "true");

        }

        @Override
        XsrfTokenGenerator getXsrfTokenGenerator()
        {
            return xsrfTokenGenerator;
        }

        @Override
        protected ApplicationProperties getJiraApplicationProperties()
        {
            return mockApplicationProperties;
        }
    }
}
