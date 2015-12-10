package com.atlassian.jira.web.debug;


import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.junit.rules.InitMockitoMocks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ClearDomainFromCookiesHttpServletResponseTest
{
    @Rule
    public TestRule initMock = new InitMockitoMocks(this);

    @Mock
    private HttpServletResponse delegate;

    private ClearDomainFromCookiesHttpServletResponse testedResponse;

    @Before
    public void setUp()
    {
        testedResponse = new ClearDomainFromCookiesHttpServletResponse(delegate);
    }

    @Test
    public void shouldRemoveDomainFromCookieString()
    {
        testedResponse.setHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/; Domain=.testing.jira-dev.com; Secure; HTTPOnly");
        Mockito.verify(delegate).setHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/; Secure; HTTPOnly");
    }

    @Test
    public void shouldRemoveDomainFromCookieStringOnAddHeader()
    {
        testedResponse.addHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/; Domain=.testing.jira-dev.com; Secure; HTTPOnly");
        Mockito.verify(delegate).addHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/; Secure; HTTPOnly");
    }

    @Test
    public void shouldRemoveDomainFromCookieStringIfNameWithLowerCase()
    {
        testedResponse.setHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/; domain=.testing.jira-dev.com; Secure; HTTPOnly");
        Mockito.verify(delegate).setHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/; Secure; HTTPOnly");
    }

    @Test
    public void shouldNotTouchStringIfDomainNotPresent()
    {
        testedResponse.setHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/;  Secure; HTTPOnly");
        Mockito.verify(delegate).setHeader("Set-Cookie", "studio.crowd.tokenkey=aaaDomain; Path=/;  Secure; HTTPOnly");
    }

    @Test
    public void shouldNotTouchValueIfNotSetCookieHeader()
    {
        testedResponse.setHeader("Set-Others", "studio.crowd.tokenkey=aaaDomain; Path=/; Domain=.testing.jira-dev.com; Secure; HTTPOnly");
        Mockito.verify(delegate).setHeader("Set-Others", "studio.crowd.tokenkey=aaaDomain; Path=/; Domain=.testing.jira-dev.com; Secure; HTTPOnly");
    }

    @Test
    public void shouldWorkWithNullHeaderValue()
    {
        testedResponse.setHeader("Set-Others", null);
        Mockito.verify(delegate).setHeader("Set-Others", null);
    }
}
