package com.atlassian.jira.web.action;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.util.velocity.MockVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for SafeRedirectCheckerTest.
 *
 * @since v4.3
 */
public class TestRedirectSanitiserImpl
{
    private final String CANONICAL_BASE_URL = "http://issues.example.com/jira";

    private final String RELATIVE_URL = "./Action.jspa?selectedId=4";
    private final String BROWSE_URL = "/browse/HSP-3";
    private final String RELATIVE_NORMALIZED_URL = "Action.jspa?selectedId=4";
    private final String ISSUE_NAV_RELATIVE_URL = "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+=+HSP&selectedIssueId=10000";
    private final String ABSOLUTE_URL_NO_SCHEME = "//www.google.com";
    
    private final VelocityRequestContextFactory mockVelocityRequestContextFactory = new MockVelocityRequestContextFactory(CANONICAL_BASE_URL);

    @Mock VelocityRequestContext reqContext;
    @Mock VelocityRequestContextFactory factory;
    @Mock ApplicationProperties applicationProperties;

    @Test
    public void testOffsiteRedirectIsNotAllowed() throws Exception
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertFalse(redirectSanitiser.canRedirectTo("http://xyz.com/"));
        assertFalse(redirectSanitiser.canRedirectTo("https://xyz.com/"));
        assertFalse(redirectSanitiser.canRedirectTo("//xyz.com/"));
    }

    @Test
    public void testRedirectToSameDomainUsingAbsoluteUrlShouldBeAllowed() throws Exception
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertTrue(redirectSanitiser.canRedirectTo(CANONICAL_BASE_URL + "/foo/bar"));
    }

    @Test
    public void testRedirectToSameDomainUsingRelativeUrlIsAllowed() throws Exception
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertTrue(redirectSanitiser.canRedirectTo(BROWSE_URL));
    }

    @Test
    public void testRedirectToSameDomainUsingNormalisedRelativeUrlIsAllowed() throws Exception
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertTrue(redirectSanitiser.canRedirectTo(RELATIVE_NORMALIZED_URL));
    }

    @Test
    public void testRedirectToSameDomainUsingActionNameIsAllowed() throws Exception
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertTrue(redirectSanitiser.canRedirectTo(RELATIVE_URL));
    }

    @Test
    public void testRedirectToIssueNavigatorShouldBeAllowed() throws Exception
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertTrue(redirectSanitiser.canRedirectTo(ISSUE_NAV_RELATIVE_URL));
    }
    
    @Test
    public void testRedirectToAbsoluteUrlsWithNoSchemeIsNotAllowed() 
    {
        RedirectSanitiserImpl redirectSanitiser = new RedirectSanitiserImpl(mockVelocityRequestContextFactory);
        assertFalse(redirectSanitiser.canRedirectTo(ABSOLUTE_URL_NO_SCHEME));
    }
}
