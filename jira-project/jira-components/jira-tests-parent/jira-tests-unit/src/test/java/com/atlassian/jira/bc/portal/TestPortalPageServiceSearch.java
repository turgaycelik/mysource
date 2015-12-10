package com.atlassian.jira.bc.portal;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.search.PrivateShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MockCloseableIterable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Test the searching methods on the {@link DefaultPortalPageService}.
 *
 * @since v3.13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPortalPageServiceSearch
{
    private static final String USERNAME_FRED = "fred";

    @Mock
    private FavouritesManager<PortalPage> favouritesManager;
    @Mock
    private PortalPageManager portalPageManager;
    @Mock
    private UserUtil userUtil;
    @Mock
    private ShareTypeValidatorUtils shareTypeValidatorUtils;
    @Mock
    private PermissionManager permissionManager;
    private ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");
    }

    /**
     * Make sure a simple search gets passed through to the manager.
     */
    @Test
    public void testSearchParameters()
    {
        final SharedEntitySearchParametersBuilder parameters = new SharedEntitySearchParametersBuilder();
        final SharedEntitySearchParameters sharedEntitySearchParameters = parameters.toSearchParameters();

        final SharedEntitySearchResult<PortalPage> expectedResult = new SharedEntitySearchResult<PortalPage>(new MockCloseableIterable<PortalPage>(Collections.<PortalPage>emptyList()), true, 100);

        when(portalPageManager.search(sharedEntitySearchParameters, user, 0, 10)).thenReturn(expectedResult);

        final JiraServiceContext ctx = createContext();
        final PortalPageService service = createPortalPageService();
        final SharedEntitySearchResult actualResult = service.search(ctx, sharedEntitySearchParameters, 0, 10);

        assertSame(expectedResult, actualResult);
    }

    /**
     * Make sure an anonymous search gets passed through to the manager.
     */
    @Test
    public void testSearchNullUser()
    {
        final SharedEntitySearchParametersBuilder parameters = new SharedEntitySearchParametersBuilder();
        final SharedEntitySearchParameters sharedEntitySearchParameters = parameters.toSearchParameters();

        final SharedEntitySearchResult<PortalPage> expectedResult = new SharedEntitySearchResult<PortalPage>(new MockCloseableIterable<PortalPage>(Collections.<PortalPage>emptyList()), true, 0);

        when(portalPageManager.search(sharedEntitySearchParameters, (ApplicationUser) null, 0, 10)).thenReturn(expectedResult);

        final JiraServiceContext ctx = createContext(null);
        final PortalPageService service = createPortalPageService();
        final SharedEntitySearchResult actualResult = service.search(ctx, sharedEntitySearchParameters, 0, 10);

        assertSame(expectedResult, actualResult);
    }

    /**
     * Null parameters should result in error.
     */
    @Test
    public void testSearchParametersNullParameters()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), null, 0, 10);
            fail("Should not accept null parameters.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    /**
     * Invalid pageOffset should result in an error.
     */
    @Test
    public void testSearchInvalidPosition()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), -1, 10);
            fail("Should not accept invalid position.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    /**
     * Width of zero should result in error.
     */
    @Test
    public void testSearchZeroWidth()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, 0);
            fail("Should not accept zero width.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    /**
     * Negative width should result in an error.
     */
    @Test
    public void testSearchInvalidWidth()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(createContext(), new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, -1);
            fail("Should not accept invalid width.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    /**
     * Null service context should result in an error.
     */
    @Test
    public void testSearchNullCtx()
    {
        final PortalPageService service = createPortalPageService();

        try
        {
            service.search(null, new SharedEntitySearchParametersBuilder().toSearchParameters(), 0, 10);
            fail("Should not accept null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    /**
     * Make the shares passed are validated.
     */
    @Test
    public void testValidateSearchCallsThroughToValidators()
    {
        final JiraServiceContext ctx = createContext(null);

        when(shareTypeValidatorUtils.isValidSearchParameter(ctx, PrivateShareTypeSearchParameter.PRIVATE_PARAMETER)).thenReturn(true);

        final SharedEntitySearchParametersBuilder searchTemplate = new SharedEntitySearchParametersBuilder();
        searchTemplate.setShareTypeParameter(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);

        final PortalPageService service = createPortalPageService();
        service.validateForSearch(ctx, searchTemplate.toSearchParameters());
    }

    private JiraServiceContext createContext()
    {
        return createContext(user.getDirectoryUser());
    }

    private JiraServiceContext createContext(final User user)
    {
        return new MockJiraServiceContext(user);
    }

    private PortalPageService createPortalPageService()
    {
        return new DefaultPortalPageService(portalPageManager, shareTypeValidatorUtils, favouritesManager, permissionManager, userUtil);
    }
}
