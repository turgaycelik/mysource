package com.atlassian.sal.jira.executor;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.tenancy.TenantContext;
import com.atlassian.jira.tenancy.TenantImpl;
import com.atlassian.jira.util.velocity.SimpleVelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.tenancy.api.Tenant;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.easymock.EasyMockAnnotations.initMocks;
import static com.atlassian.jira.easymock.EasyMockAnnotations.replayMocks;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.verify;

public class TestJiraThreadLocalContextManager
{


    @Mock
    JiraAuthenticationContextImpl mockAuthenticationContext;


    @Mock
    VelocityRequestContextFactory mockVelocityRequestContextFactory;

    @Mock
    TenantContext mockTenantContext;

    @Test
    public void testGetThreadLocalContext() throws InterruptedException
    {
        final SimpleVelocityRequestContext requestContext = new SimpleVelocityRequestContext("someurl");
        final Tenant tenant = new TenantImpl("BaseTenant");

        expect(mockAuthenticationContext.getLoggedInUser()).andReturn(null);
        expect(mockTenantContext.getCurrentTenant()).andReturn(tenant);
        expect(mockVelocityRequestContextFactory.getJiraVelocityRequestContext()).andReturn(requestContext);
        replayMocks(this);

        JiraThreadLocalContextManager manager = new JiraThreadLocalContextManager(mockAuthenticationContext, mockVelocityRequestContextFactory, mockTenantContext);
        Object ctx = manager.getThreadLocalContext();
        Assert.assertNotNull(ctx);
        JiraThreadLocalContextManager.JiraThreadLocalContext context = (JiraThreadLocalContextManager.JiraThreadLocalContext) ctx;
        Assert.assertNull(context.getUser());
        Assert.assertSame(requestContext, context.getVelocityRequestContext());
        Assert.assertSame(tenant, context.getTenant());

        verify( mockAuthenticationContext, mockVelocityRequestContextFactory, mockTenantContext);
    }

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
    }
}
