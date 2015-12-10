package com.atlassian.jira.web.action;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for SafeRedirectCheckerTest.
 *
 * @since v4.3
 */
public class SafeRedirectCheckerTest extends MockControllerTestCase
{
    private final String CANONICAL_BASE_URL = "http://jira.atlassian.com";

    private final String RELATIVE_URL = "./Action.jspa?selectedId=4";
    private final String BROWSE_URL = "/browse/HSP-3";
    private final String RELATIVE_NORMALIZED_URL = "Action.jspa?selectedId=4";
    private final String OFFSITE_HTTP_URL = "http://xyz.com/";
    private final String ISSUE_NAV_RELATIVE_URL = "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+=+HSP&selectedIssueId=10000";
    private final String ABSOLUTE_URL_NO_SCHEME = "//www.google.com";

    @Mock VelocityRequestContext reqContext;
    @Mock VelocityRequestContextFactory factory;
    @Mock ApplicationProperties applicationProperties;

    @Test
    public void testOffsiteRedirectIsNotAllowed() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertFalse(checker.canRedirectTo(OFFSITE_HTTP_URL));
    }

    @Test
    public void testRedirectToSameDomainUsingAbsoluteUrlShouldBeAllowed() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertTrue(checker.canRedirectTo(CANONICAL_BASE_URL + "/foo/bar"));
    }

    @Test
    public void testRedirectToSameDomainUsingRelativeUrlIsAllowed() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertTrue(checker.canRedirectTo(BROWSE_URL));
    }

    @Test
    public void testRedirectToSameDomainUsingNormalisedRelativeUrlIsAllowed() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertTrue(checker.canRedirectTo(RELATIVE_NORMALIZED_URL));
    }

    @Test
    public void testRedirectToSameDomainUsingActionNameIsAllowed() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertTrue(checker.canRedirectTo(RELATIVE_URL));
    }

    @Test
    public void testRedirectToAbsoluteUriUsingPort() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertTrue(checker.canRedirectTo(CANONICAL_BASE_URL + ":80/foo/bar"));
    }

    @Test
    public void testRedirectToIssueNavigatorShouldBeAllowed() throws Exception
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertTrue(checker.canRedirectTo(ISSUE_NAV_RELATIVE_URL));
    }
    
    @Test
    public void testRedirectToAbsoluteUrlsWithNoSchemeIsNotAllowed() 
    {
        SafeRedirectChecker checker = new SafeRedirectChecker(instantiate(RedirectSanitiserImpl.class));
        assertFalse(checker.canRedirectTo(ABSOLUTE_URL_NO_SCHEME));
    }

    /**
     * Sets up the base URL.
     */
    @Before
    public void setUp()
    {
        expect(reqContext.getCanonicalBaseUrl()).andStubReturn(CANONICAL_BASE_URL);
        expect(factory.getJiraVelocityRequestContext()).andStubReturn(reqContext);
        expect(applicationProperties.getEncoding()).andStubReturn("UTF-8");
    }
}
