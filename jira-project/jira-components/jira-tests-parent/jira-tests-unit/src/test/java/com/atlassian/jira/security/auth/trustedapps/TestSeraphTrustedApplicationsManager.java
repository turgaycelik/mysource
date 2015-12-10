package com.atlassian.jira.security.auth.trustedapps;

import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestSeraphTrustedApplicationsManager
{
    private JiraAuthenticationContext jiraAuthContext;

    // This is necessary because I don't think we can mock two interfaces using Easymock
    public interface TrustedCurrentApplication extends CurrentApplication, TrustedApplication
    {
    }

    @Test
    public void testReturnsInfoIfFound() throws Exception
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "appId", "TheApp", 10000);
        final CurrentApplication mockCurrentApplication = createMock(TrustedCurrentApplication.class);
        final TrustedApplicationsManager manager = new SeraphTrustedApplicationsManager(new MockTrustedApplicationManager(info),
            new MockCurrentApplicationFactory(mockCurrentApplication), jiraAuthContext);
        replay(mockCurrentApplication);

        final TrustedApplication trustedApplication = manager.getTrustedApplication("appId");
        assertNotNull(trustedApplication);
        assertTrue(((TrustedApplicationInfo) trustedApplication).isValidKey());
    }

    @Test
    public void testReturnsNullIfNotFound() throws Exception
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "appId", "TheApp", 10000);
        final CurrentApplication mockCurrentApplication = createMock(TrustedCurrentApplication.class);
        expect(mockCurrentApplication.getID()).andReturn("thecurrentapp");
        final TrustedApplicationsManager manager = new SeraphTrustedApplicationsManager(new MockTrustedApplicationManager(info),
            new MockCurrentApplicationFactory(mockCurrentApplication), jiraAuthContext);
        replay(mockCurrentApplication);

        final TrustedApplication trustedApplication = manager.getTrustedApplication("nonExistantId");
        assertNull(trustedApplication);
        verify(mockCurrentApplication);
    }

    @Test
    public void testReturnsCurrentApp() throws Exception
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "appId", "TheApp", 10000);
        final CurrentApplication mockCurrentApplication = createMock(TrustedCurrentApplication.class);
        expect(mockCurrentApplication.getID()).andReturn("thecurrentapp");
        final TrustedApplicationsManager manager = new SeraphTrustedApplicationsManager(new MockTrustedApplicationManager(info),
            new MockCurrentApplicationFactory(mockCurrentApplication), jiraAuthContext);
        replay(mockCurrentApplication);

        final TrustedApplication trustedApplication = manager.getTrustedApplication("thecurrentapp");
        assertSame(mockCurrentApplication, trustedApplication);
        verify(mockCurrentApplication);
    }

    // JRA-24743, configured trusted apps *must* override current app, for Studio
    @Test
    public void testConfiguredTrustedAppsOverrideCurrentApp()
    {
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "appId", "TheApp", 10000);
        final CurrentApplication mockCurrentApplication = createMock(TrustedCurrentApplication.class);
        expect(mockCurrentApplication.getID()).andReturn("appId");
        final TrustedApplicationsManager manager = new SeraphTrustedApplicationsManager(new MockTrustedApplicationManager(info),
            new MockCurrentApplicationFactory(mockCurrentApplication), jiraAuthContext);
        replay(mockCurrentApplication);

        final TrustedApplication trustedApplication = manager.getTrustedApplication("appId");
        assertTrue(((TrustedApplicationInfo) trustedApplication).isValidKey());
        assertNotSame(mockCurrentApplication, trustedApplication);
        assertEquals(10000, ((TrustedApplicationInfo) trustedApplication).getTimeout());
    }

    @Test
    public void testReturnsNullIfInvalidKey() throws Exception
    {
        final CurrentApplication mockCurrentApplication = createMock(CurrentApplication.class);
        expect(mockCurrentApplication.getID()).andReturn("thecurrentapp");
        final TrustedApplicationInfo info = new MockTrustedApplicationInfo(1, "appId", "TheApp", 10000)
        {
            @Override
            public boolean isValidKey()
            {
                return false;
            }

            @Override
            public PublicKey getPublicKey()
            {
                return new KeyFactory.InvalidPublicKey(new RuntimeException("bad key"));
            }
        };
        final TrustedApplicationsManager manager = new SeraphTrustedApplicationsManager(new MockTrustedApplicationManager(info),
            new MockCurrentApplicationFactory(mockCurrentApplication), jiraAuthContext);
        replay(mockCurrentApplication);

        final TrustedApplication trustedApplication = manager.getTrustedApplication("appId");
        assertNull(trustedApplication);
        verify(mockCurrentApplication);
    }

    @Test
    public void testUsesApplicationFactory() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean(false);
        final TrustedApplicationsManager manager = new SeraphTrustedApplicationsManager(new MockTrustedApplicationManager(),
            new CurrentApplicationFactory()
            {
                public CurrentApplication getCurrentApplication()
                {
                    called.set(true);
                    return null;
                }
            }, jiraAuthContext);
        assertNull(manager.getCurrentApplication());
        assertTrue(called.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionFailsIfManagerNull()
    {
        new SeraphTrustedApplicationsManager(null, createMock(CurrentApplicationFactory.class), jiraAuthContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionFailsIfFactoryNull()
    {
        new SeraphTrustedApplicationsManager(createMock(TrustedApplicationManager.class), null, jiraAuthContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorShouldThrowIllegalArgumentExceptionIfJiraAuthContextIsNull()
    {
        new SeraphTrustedApplicationsManager(createMock(TrustedApplicationManager.class), createMock(CurrentApplicationFactory.class), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionFailsIfAllNull()
    {
        new SeraphTrustedApplicationsManager(null, null, null);
    }

    @Before
    @SuppressWarnings("deprecation")
    public void prepareJiraAuthContext() throws Exception
    {
        jiraAuthContext = createNiceMock(JiraAuthenticationContext.class);
        expect(jiraAuthContext.getLoggedInUser()).andReturn(new MockUser("admin", "admin", "admin@example.com")).anyTimes();
    }

    public static class MockCurrentApplicationFactory implements CurrentApplicationFactory
    {
        private CurrentApplication application;

        public MockCurrentApplicationFactory(CurrentApplication application)
        {
            this.application = application;
        }

        @Override
        public CurrentApplication getCurrentApplication()
        {
            return application;
        }
    }
}
