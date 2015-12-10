package com.atlassian.jira.security;

import java.security.Principal;
import java.util.List;

import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockFilterConfig;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.jira.mock.servlet.MockServletContext;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.seraph.SecurityService;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.config.ConfigurationException;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.config.SecurityConfigImpl;
import com.atlassian.seraph.util.SecurityUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class TestJiraSecurityFilter
{
    private JiraSecurityFilter filter = new MockJiraSecurityFilter();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockHttpSession session = new MockHttpSession();
    private MockHttpServletRequest request = new MockHttpServletRequest(session);

    private MockFilterChain chain = new MockFilterChain();
    private MockFilterConfig filterConfig = new MockFilterConfig();

    private MockServletContext servletContext = new MockServletContext();
    private SecurityConfig securityConfig;

    @Mock
    private SecurityService securityService;

    @Mock
    private RoleMapper roleMapper;

    @Rule
    public MockitoContainer initMockitoMocks = MockitoMocksInContainer.rule(this);

    @Before
    public void setUp() throws Exception
    {
        securityConfig = new MockSecurityConfig();

        SecurityUtils.disableSeraphFiltering(request);

        filterConfig.setServletContext(servletContext);

        filter.init(filterConfig, false);
    }

    @Test
    public void testGetLoginUrlRedirectingToUserRole() throws Exception
    {
        when(securityService.getRequiredRoles(request)).thenReturn(ImmutableSet.of("sysadmin"));

        filter.doFilter(request, response, chain);

        assertThat(response.getRedirect(), StringContains.containsString("user_role=SYSADMIN"));
    }

    @Test
    public void testGetLoginUrlBlankMissingRoles() throws Exception
    {
        when(securityService.getRequiredRoles(request)).thenReturn(ImmutableSet.of("admin"));
        when(roleMapper.hasRole(any(Principal.class), eq(request), eq("admin"))).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertNull("Filter should not redirect anywhere", response.getRedirect());
    }


    @Test
    public void testGetLoginUrlNotRedirecting() throws Exception
    {
        filter.doFilter(request, response, chain);

        assertNull("Filter should not redirect anywhere", response.getRedirect());
    }

    private class MockSecurityConfig extends SecurityConfigImpl
    {
        public MockSecurityConfig() throws ConfigurationException
        {
            super(null);
        }

        @Override
        public List<SecurityService> getServices()
        {
            return ImmutableList.of(securityService);
        }

        @Override
        public RoleMapper getRoleMapper()
        {
            return roleMapper;
        }
    }

    private class MockJiraSecurityFilter extends JiraSecurityFilter
    {
        @Override
        protected SecurityConfig getSecurityConfig()
        {
            return securityConfig;
        }
    }
}
