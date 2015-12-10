package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.query.QueryImpl;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Loaded.LOADED;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Loaded.UNLOADED;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Modified.CLEAN;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Modified.DIRTY;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Valid.ERROR;
import static com.atlassian.jira.web.action.filter.TestFilterOperationsBean.Valid.OKAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link TestFilterOperationsBean}.
 *
 * @since v4.0
 */
public class TestFilterOperationsBean
{
    private static final String TEST_USERNAME = "testUser";

    static enum Valid { OKAY, ERROR }
    static enum Loaded { UNLOADED, LOADED }
    static enum Modified { CLEAN, DIRTY }

    private MockUserManager userManager;

    @Before
    public void setUp() throws Exception
    {
        userManager = new MockUserManager();
        new MockComponentWorker().addMock(UserManager.class, userManager).init();
    }

    @Test
    public void testCreate()
    {
        final SearchRequest searchRequest = new SearchRequest(null, (ApplicationUser) null, "v5.1 Bugs", "Bugs not done in v5.1");
        final ApplicationUser user = new MockApplicationUser("fred");
        userManager.addUser(user);

        FilterOperationsBean bean;

        // null SR owner : ownFilter
        bean = FilterOperationsBean.create(searchRequest, true, user, true);
        assertTrue(bean.isShowSaveNew());

        // SR owner = loggedInUser (case-insensitive) : ownFilter
        searchRequest.setOwner(user);
        bean = FilterOperationsBean.create(searchRequest, true, user, true);
        assertTrue(bean.isShowSaveNew());

        // SR owner != loggedInUser : !ownFilter
        searchRequest.setOwner(user);
        final MockApplicationUser barney = new MockApplicationUser("barney");
        userManager.addUser(barney);
        bean = FilterOperationsBean.create(searchRequest, true, barney, true);

        assertFalse(bean.isShowSaveNew());

        // loggedInUser = null : !ownFilter
        // JRADEV-12517: don't throw NPE
        searchRequest.setOwner(user);
        bean = FilterOperationsBean.create(searchRequest, true, null, true);
        assertFalse(bean.isShowSaveNew());
    }

    /**
     * Make sure the constructor returns false.
     */
    @Test
    public void testConstructor()
    {
        FilterOperationsBean fob = new FilterOperationsBean();
        assertFalse(fob.isShowEdit());
        assertFalse(fob.isShowReload());
        assertFalse(fob.isShowSave());
        assertFalse(fob.isShowSaveNew());
        assertFalse(fob.isShowSaveAs());
        assertFalse(fob.isShowViewSubscriptions());
    }

    /**
     * Make sure no request results in no operations.
     */
    @Test
    public void testSearchRequestNull()
    {
        FilterOperationsBean none = new FilterOperationsBean();

        assertNullFilterOperations(none, OKAY);
        assertNullFilterOperations(none, ERROR);

    }

    /**
     * Make sure the operations are correct when the user is the owner.
     */
    @Test
    public void testOwner()
    {
        FilterOperationsBean fob = new FilterOperationsBean();
        fob.setShowReload(true);
        fob.setShowInvalid(true);
        fob.setShowEdit(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, LOADED, DIRTY);
        fob = new FilterOperationsBean();
        fob.setShowInvalid(true);
        fob.setShowEdit(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, LOADED, CLEAN);

        fob = new FilterOperationsBean();
        fob.setShowInvalid(true);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, UNLOADED, DIRTY);
        assertFilterOperationsBeanStateSameOwner(fob, ERROR, UNLOADED, CLEAN);

        fob = new FilterOperationsBean();
        fob.setShowReload(true);
        fob.setShowEdit(true);
        fob.setShowSave(true);
        fob.setShowSaveAs(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, LOADED, DIRTY);

        fob = new FilterOperationsBean();
        fob.setShowEdit(true);
        fob.setShowSaveAs(true);
        fob.setShowViewSubscriptions(true);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, LOADED, CLEAN);

        fob = new FilterOperationsBean();
        fob.setShowSaveNew(true);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, UNLOADED, DIRTY);
        assertFilterOperationsBeanStateSameOwner(fob, OKAY, UNLOADED, CLEAN);

    }

    /**
     * Make sure the operations are correct when the user is not the owner.
     */
    @Test
    public void testNotOwner()
    {
        FilterOperationsBean reaload = new FilterOperationsBean();
        reaload.setShowReload(true);
        reaload.setShowInvalid(true);

        FilterOperationsBean none = new FilterOperationsBean();
        none.setShowInvalid(true);

        assertFilterOperationsBeanStateDifferentOwner(reaload, ERROR, LOADED, DIRTY);
        assertFilterOperationsBeanStateDifferentOwner(none, ERROR, UNLOADED, DIRTY);

        reaload = new FilterOperationsBean();
        reaload.setShowInvalid(true);

        assertFilterOperationsBeanStateDifferentOwner(reaload, ERROR, LOADED, CLEAN);
        assertFilterOperationsBeanStateDifferentOwner(none, ERROR, UNLOADED, CLEAN);

        none = new FilterOperationsBean();
        reaload = new FilterOperationsBean();
        reaload.setShowReload(true);
        assertFilterOperationsBeanStateDifferentOwner(reaload, OKAY, LOADED, DIRTY);
        assertFilterOperationsBeanStateDifferentOwner(none, OKAY, UNLOADED, DIRTY);

        new FilterOperationsBean();
        assertFilterOperationsBeanStateDifferentOwner(none, OKAY, LOADED, CLEAN);
        assertFilterOperationsBeanStateDifferentOwner(none, OKAY, UNLOADED, CLEAN);
    }

    private void assertNullFilterOperations(final FilterOperationsBean expectedState, final Valid hasErrors)
    {
        assertEquals(expectedState, FilterOperationsBean.create(null, hasErrors == ERROR, new MockApplicationUser(TEST_USERNAME), false));
    }

    private void assertFilterOperationsBeanStateSameOwner(final FilterOperationsBean expectedState, final Valid hasErrors, final Loaded isLoaded, final Modified wasModified)
    {
        assertFilterOperationsBeanState(expectedState, hasErrors, isLoaded, wasModified, new MockApplicationUser(TEST_USERNAME));
    }

    private void assertFilterOperationsBeanStateDifferentOwner(final FilterOperationsBean expectedState, final Valid hasErrors, final Loaded isLoaded, final Modified wasModified)
    {
        final MockApplicationUser user = new MockApplicationUser(TEST_USERNAME + "NotMe");
        userManager.addUser(user);
        assertFilterOperationsBeanState(expectedState, hasErrors, isLoaded, wasModified, user);
    }

    private void assertFilterOperationsBeanState(final FilterOperationsBean expectedState, final Valid validFilter, final Loaded isLoaded, final Modified wasModified, final ApplicationUser user)
    {
        SearchRequest sr = new SearchRequest(new QueryImpl(), user, "test", "desc", ((Loaded.LOADED == isLoaded) ? 123L : null), 0L);
        sr.setModified(wasModified == DIRTY);

        assertEquals(expectedState, FilterOperationsBean.create(sr, validFilter == OKAY, new MockApplicationUser(TEST_USERNAME), false));
    }
}
