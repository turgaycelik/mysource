package com.atlassian.jira.security.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.UrlBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * @since v4.1.1
 */
public class TestXsrfTokenAppendingResponse extends MockControllerTestCase
{
    private final static String URL = "url";

    private HttpServletRequest mockBaseRequest;
    private HttpServletResponse mockBaseResponse;

    private XsrfTokenAppendingResponse xsrfTokenAppendingResponse;


    @Before
    public void setUp() throws Exception
    {

        mockBaseRequest = getMock(HttpServletRequest.class);
        mockBaseResponse = getMock(HttpServletResponse.class);
        xsrfTokenAppendingResponse = new XsrfTokenAppendingResponse(mockBaseRequest, mockBaseResponse)
        {
            @Override
            UrlBuilder createUrlBuilder(final String url)
            {
                return new UrlBuilder(url,"UTF-8",false);
            }
        };
    }

    @Test
    public void testDelegatesToBaseIfTokenIsNull() throws Exception
    {
        expect(mockBaseRequest.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY)).andReturn(null);
        mockBaseResponse.sendRedirect(URL);
        expectLastCall();

        replay();

        xsrfTokenAppendingResponse.sendRedirect(URL);
    }

    @Test
    public void testAppendsTokenToUrlIfThereIsATokenInTheResponse() throws Exception
    {
        String tokenValue = "token-value";
        expect(mockBaseRequest.getParameter(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY)).andReturn(tokenValue);

        mockBaseResponse.sendRedirect(and(contains(URL), contains(XsrfTokenGenerator.TOKEN_WEB_PARAMETER_KEY + "=" + tokenValue)));        
        expectLastCall();

        replay();

        xsrfTokenAppendingResponse.sendRedirect(URL);
    }
}
