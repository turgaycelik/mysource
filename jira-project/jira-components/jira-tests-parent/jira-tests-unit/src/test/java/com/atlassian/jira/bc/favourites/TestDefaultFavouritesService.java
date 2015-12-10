package com.atlassian.jira.bc.favourites;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class TestDefaultFavouritesService
{
    private static final Long ENTITY_ID = 999L;

    private DefaultFavouritesService service;
    @Mock private FavouritesManager<SharedEntity> mockFavouritesManager;
    private SharedEntity entity;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        final User user = new MockUser("admin");
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        entity = new SharedEntity.Identifier(ENTITY_ID, SearchRequest.ENTITY_TYPE, user);
        service = new DefaultFavouritesService(mockFavouritesManager);
    }

    @After
    public void tearDown()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testAddFavouriteSuccess() throws Exception
    {
        // Set up
        JiraServiceContext ctx = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());

        // Invoke
        service.addFavourite(ctx, entity);

        // Check
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verify(mockFavouritesManager).addFavourite((ApplicationUser) null, entity);
    }

    @Test
    public void testAddFavouriteInPositionSuccess() throws Exception
    {
        // Set up
        JiraServiceContext ctx = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());

        // Invoke
        service.addFavouriteInPosition(ctx, entity, 1);

        // Check
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verify(mockFavouritesManager).addFavouriteInPosition((ApplicationUser) null, entity, 1);
    }

    @Test
    public void testAddFavouriteInPositionNoPermission() throws PermissionException
    {
        // Set up
        JiraServiceContext ctx = new JiraServiceContextImpl((User) null, new SimpleErrorCollection()) {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        doThrow(new PermissionException("blah"))
                .when(mockFavouritesManager).addFavouriteInPosition((ApplicationUser) null, entity, 1);

        // Invoke
        service.addFavouriteInPosition(ctx, entity, 1);

        // Check
        final ErrorCollection errorCollection = ctx.getErrorCollection();
        assertTrue(errorCollection.hasAnyErrors());
        assertEquals(1, errorCollection.getErrorMessages().size());
        // For some weird reason, IDEA runs in the US locale and so this assertion fails
        assertEquals("You do not have permission to favourite this item.",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testRemoveFavourite()
    {
        // Set up
        JiraServiceContext ctx = new JiraServiceContextImpl((User) null, new SimpleErrorCollection());

        // Invoke
        service.removeFavourite(ctx, entity);

        // Check
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        verify(mockFavouritesManager).removeFavourite((ApplicationUser) null, entity);
    }

    @Test
    public void isFavouriteWhenTheUserIsNotAnonymous() throws PermissionException
    {
        // Set up
        final ApplicationUser loggedInApplicationUser = new MockApplicationUser("test-user");
        when(mockFavouritesManager.isFavourite(loggedInApplicationUser, entity)).thenReturn(true);

        // Invoke and check
        assertTrue(service.isFavourite(loggedInApplicationUser, entity));
    }

    @Test
    public void isNotFavouriteWhenTheUserIsNotAnonymous() throws PermissionException
    {
        // Set up
        final ApplicationUser loggedInApplicationUser = new MockApplicationUser("test-user");
        when(mockFavouritesManager.isFavourite(loggedInApplicationUser, entity)).thenReturn(false);

        // Invoke and check
        assertFalse(service.isFavourite(loggedInApplicationUser, entity));
    }

    @Test
    public void anEntityIsNeverFavouriteForAnonymousUsers() throws PermissionException
    {
        assertFalse(service.isFavourite((ApplicationUser) null, entity));
    }
}
