package com.atlassian.jira.security.xsrf;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.bc.license.JiraServerIdProvider;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.velocity.HttpSessionBackedVelocityRequestSession;
import com.atlassian.jira.util.velocity.SimpleVelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.ExecutingHttpRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class SimpleXsrfTokenGeneratorTest extends MockControllerTestCase
{
    private Map<String, String> cookieMap;
    private HttpServletRequest servletRequest;
    private JiraAuthenticationContext authenticationContext;
    private MockUser user;
    private JiraServerIdProvider serverIdProvider;

    @Before
    public void setUp() throws Exception
    {
        authenticationContext = getMock(JiraAuthenticationContext.class);
        serverIdProvider = getMock(JiraServerIdProvider.class);
        user = new MockUser("fred");

        cookieMap = new HashMap<String, String>();
        servletRequest = newServletRequest(cookieMap);
        HttpServletResponse servletResponse = newServletResponse(cookieMap);
        ExecutingHttpRequest.set(servletRequest, servletResponse);
    }

    @After
    public void tearDown() throws Exception
    {
        ExecutingHttpRequest.clear();
    }

    @Test
    public void testGenerateTokenFromHttpRequest()
    {
        expect(authenticationContext.getLoggedInUser()).andStubReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        String tokenGen1 = xsrfTokenGenerator.generateToken(servletRequest);
        // the second time around we should have one and hence not get another
        String tokenGen2 = xsrfTokenGenerator.generateToken(servletRequest);

        assertTokensAreKosher(xsrfTokenGenerator, tokenGen1, tokenGen2);

    }

    @Test
    public void testGenerateTokenFromHttpRequestNoCreate()
    {
        expect(authenticationContext.getLoggedInUser()).andStubReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        String token = xsrfTokenGenerator.generateToken(servletRequest, false);

        assertNull(token);
    }

    @Test
    public void testGenerateTokenFromThreadLocalHttpRequest()
    {
        expect(authenticationContext.getLoggedInUser()).andStubReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        String tokenGen1 = xsrfTokenGenerator.generateToken();
        // the second time around we should have one and hence not get another
        String tokenGen2 = xsrfTokenGenerator.generateToken();

        assertTokensAreKosher(xsrfTokenGenerator, tokenGen1, tokenGen2);
    }

    @Test
    public void testGenerateTokenFromThreadLocalHttpRequestNoCreate()
    {
        expect(authenticationContext.getLoggedInUser()).andStubReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        String token = xsrfTokenGenerator.generateToken(false);

        assertNull(token);
    }

    @Test
    public void testGenerateTokenFromVelocityContext()
    {
        expect(authenticationContext.getLoggedInUser()).andStubReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);

        VelocityRequestContext context = new SimpleVelocityRequestContext(null, null, null, new HttpSessionBackedVelocityRequestSession(servletRequest));
        String tokenGen1 = xsrfTokenGenerator.generateToken(context);
        // the second time around we should have one and hence not get another
        String tokenGen2 = xsrfTokenGenerator.generateToken(context);

        assertTokensAreKosher(xsrfTokenGenerator, tokenGen1, tokenGen2);
    }

    @Test
    public void testGenerateTokenWithNullRequest()
    {
        ExecutingHttpRequest.clear();
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        
        final String token = xsrfTokenGenerator.generateToken();
        assertNull(token);
    }

    @Test
    public void testGetXsrfTokenName()
    {
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        assertEquals("atlassian.xsrf.token", xsrfTokenGenerator.getXsrfTokenName());
    }

    @Test
    public void testValidateToken()
    {
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");

        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        String tokenGen1 = xsrfTokenGenerator.generateToken(servletRequest);

        assertFalse(xsrfTokenGenerator.validateToken(servletRequest, tokenGen1 + "1"));
        assertFalse(xsrfTokenGenerator.validateToken(servletRequest, tokenGen1 + "abc"));
        assertFalse(xsrfTokenGenerator.validateToken(servletRequest, "abc123"));
        assertFalse(xsrfTokenGenerator.validateToken(servletRequest, null));
        assertFalse(xsrfTokenGenerator.validateToken(servletRequest, ""));

        assertTrue(xsrfTokenGenerator.validateToken(servletRequest, tokenGen1));
    }

    @Test
    public void testWasGeneratedByAuthLoggedIn()
    {
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");

        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        final String token = xsrfTokenGenerator.generateToken();
        assertTrue(xsrfTokenGenerator.generatedByAuthenticatedUser(token));
    }

    @Test
    public void testWasGeneratedByAuthLoggedOut()
    {
        expect(authenticationContext.getLoggedInUser()).andReturn(null);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");

        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        final String token = xsrfTokenGenerator.generateToken();
        assertFalse(xsrfTokenGenerator.generatedByAuthenticatedUser(token));
    }

    @Test
    public void getGetTokenReturnsNull()
    {
        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);

        assertNull(xsrfTokenGenerator.getToken(servletRequest));
    }

    @Test
    public void getGetTokenReturnsLastGenerated()
    {
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        expect(serverIdProvider.getServerId()).andStubReturn("F*CK3R");

        final XsrfTokenGenerator xsrfTokenGenerator = instantiate(SimpleXsrfTokenGenerator.class);
        final String token = xsrfTokenGenerator.generateToken();

        assertEquals(token, xsrfTokenGenerator.getToken(servletRequest));
    }

    private void assertTokensAreKosher(XsrfTokenGenerator xsrfTokenGenerator, String tokenGen1, String tokenGen2)
    {
        assertNotNull(tokenGen1);

        Object token1 = cookieMap.get(xsrfTokenGenerator.getXsrfTokenName());
        assertNotNull(token1);
        assertEquals(tokenGen1, token1);

        assertNotNull(tokenGen2);
        assertEquals(tokenGen1, tokenGen2);

        Object token2 = cookieMap.get(xsrfTokenGenerator.getXsrfTokenName());
        assertNotNull(token2);
        assertEquals(tokenGen1, token2);
        assertEquals(tokenGen2, token2);
    }


    private MockHttpServletRequest newServletRequest(final Map<String, String> cookieMap)
    {
        return new MockHttpServletRequest()
        {
            @Override
            public Cookie[] getCookies()
            {
                return this.toCookies(cookieMap);
            }
        };
    }

    private HttpServletResponse newServletResponse(final Map<String, String> cookieMap)
    {
        return new MockHttpServletResponse()
        {
            @Override
            public void addCookie(Cookie cookie)
            {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        };
    }
}
