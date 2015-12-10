package com.atlassian.jira.rest.auth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestJiraRestAuthSeaphFilter
{
    public static final String AUTH_TYPE_ATTRIBUTE = "os_authTypeDefault";
    public static final String AUTH_VALUE_NONE = "none";

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private FilterChain chain;

    private JiraRestSeraphAuthFilter restSeraphAuthFilter;

    @Before
    public void setup()
    {
        restSeraphAuthFilter = new JiraRestSeraphAuthFilter();
    }

    @Test
    public void testAttributeAlreadySet() throws IOException, ServletException
    {
        when(httpServletRequest.getServletPath()).thenReturn("/rest/something/rest/auth");
        when(httpServletRequest.getAttribute(AUTH_TYPE_ATTRIBUTE)).thenReturn("other");

        restSeraphAuthFilter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(httpServletRequest, never()).setAttribute(anyString(), anyString());
    }

    @Test
    public void testAttributeNotSet() throws IOException, ServletException
    {
        when(httpServletRequest.getServletPath()).thenReturn("/rest/auth/something");

        restSeraphAuthFilter.doFilter(httpServletRequest, httpServletResponse, chain);

        verify(httpServletRequest).setAttribute(AUTH_TYPE_ATTRIBUTE, AUTH_VALUE_NONE);
    }
}
