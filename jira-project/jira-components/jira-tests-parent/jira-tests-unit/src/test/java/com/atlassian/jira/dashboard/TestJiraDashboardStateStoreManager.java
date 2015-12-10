package com.atlassian.jira.dashboard;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationImpl;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.user.MockApplicationUser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.ObjectUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.dashboard.Color.color1;
import static com.atlassian.gadgets.dashboard.Color.color2;
import static com.atlassian.gadgets.dashboard.Color.color5;
import static com.atlassian.gadgets.dashboard.Color.color6;
import static com.atlassian.gadgets.dashboard.Color.color8;
import static com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex.ONE;
import static com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex.ZERO;
import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static com.atlassian.gadgets.dashboard.Layout.AA;
import static com.atlassian.gadgets.dashboard.Layout.AAA;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestJiraDashboardStateStoreManager
{
    private static final long PORTAL_PAGE_ID = 10020;
    private static final long PORTLET_CONFIGURATION_ID = 10019;
    private static final long PORTLET_ID_1 = 10011;
    private static final long PORTLET_ID_2 = 10012;
    private static final long PORTLET_ID_3 = 10231;

    private static final DashboardId DASHBOARD_ID = DashboardId.valueOf(Long.toString(PORTAL_PAGE_ID));

    private static final Map<String, String> EMPTY_STRING_MAP = emptyMap();
    private static final Map<String, String> PREFS = ImmutableMap.of("pref1", "value1", "pref2", "value2");

    private static final MockApplicationUser ADMIN = new MockApplicationUser("admin");

    private static final String DASHBOARD_TITLE = "My Dashboard";
    private static final String PORTAL_NAME = "Test Dashboard";
    private static final String WRITE_LOCK_NAME = JiraDashboardStateStoreManager.getWriteLockName(DASHBOARD_ID);

    private static final PortalPage PORTAL_PAGE = PortalPage
            .id(PORTAL_PAGE_ID)
            .name(PORTAL_NAME)
            .description("")
            .owner(ADMIN)
            .favouriteCount(0L)
            .layout(AA)
            .version(0L)
            .build();

    private static final URI GOOGLE_URI = URI.create("http://www.google.com/");
    private static final URI MSN_URI = URI.create("http://www.msn.com/");
    private static final URI NEW_GADGET_URI = URI.create("http://www.newgadget.com/");
    private static final URI SAMPLE_URI = URI.create("/gadgets/sample.xml");

    private static GadgetId asGadgetId(final long portletId)
    {
        return GadgetId.valueOf(Long.toString(portletId)); // GadgetId is final => can't be mocked
    }

    private static void assertGadgetState(final GadgetState actual, final long expectedPortletId,
            final Color expectedColor, final URI expectedGadgetSpecUri, final Map<String, String> expectedUserPrefs)
    {
        assertEquals(asGadgetId(expectedPortletId), actual.getId());
        assertEquals(expectedColor, actual.getColor());
        assertEquals(expectedGadgetSpecUri, actual.getGadgetSpecUri());
        assertEquals(expectedUserPrefs, actual.getUserPrefs());
    }

    private static GadgetState buildGadget(final long portletId, final URI specUri, final Color color)
    {
        return GadgetState.gadget(asGadgetId(portletId)).specUri(specUri).color(color).build();
    }

    @InjectMocks private JiraDashboardStateStoreManager dashboardStateStoreManager;

    @Mock private ClusterLock mockClusterLock;
    @Mock private ClusterLockService mockClusterLockService;
    @Mock private PortalPageManager mockPortalPageManager;
    @Mock private PortalPageStore mockPortalPageStore;
    @Mock private PortletConfigurationStore mockPortletConfigurationStore;

    @Before
    public void setUp()
    {
        when(mockClusterLockService.getLockForName(WRITE_LOCK_NAME)).thenReturn(mockClusterLock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRetrieveNullId()
    {
        dashboardStateStoreManager.retrieve(null);
    }

    @Test(expected = DashboardNotFoundException.class)
    public void testRetrieveDashboardNotFound()
    {
        // Set up
        when(mockPortalPageStore.getPortalPage(PORTAL_PAGE_ID)).thenReturn(null);

        // Invoke
        dashboardStateStoreManager.retrieve(DASHBOARD_ID);
    }

    @Test
    public void testRetrieveSuccess()
    {
        // Set up
        when(mockPortalPageStore.getPortalPage(PORTAL_PAGE_ID)).thenReturn(PORTAL_PAGE);
        when(mockPortalPageManager.getPortletConfigurations(PORTAL_PAGE_ID)).thenReturn(getSortedPortletConfigurationMocks());

        // Invoke
        final DashboardState state = dashboardStateStoreManager.retrieve(DASHBOARD_ID);

        // Check
        assertEquals(DASHBOARD_ID, state.getId());
        assertEquals(PORTAL_NAME, state.getTitle());
        assertEquals(AA, state.getLayout());
        final Iterable<GadgetState> firstColumn = state.getGadgetsInColumn(ZERO);
        final Iterator<GadgetState> firstColumnIterator = firstColumn.iterator();
        final GadgetState firstRow = firstColumnIterator.next();
        final GadgetState secondRow = firstColumnIterator.next();
        assertFalse(firstColumnIterator.hasNext());
        final Iterable<GadgetState> secondColumn = state.getGadgetsInColumn(ONE);
        final Iterator<GadgetState> secondColumnIterator = secondColumn.iterator();
        final GadgetState secondColfirstRow = secondColumnIterator.next();
        assertFalse(secondColumnIterator.hasNext());
        assertGadgetState(firstRow, PORTLET_ID_1, color1, GOOGLE_URI, EMPTY_STRING_MAP);
        assertGadgetState(secondRow, PORTLET_ID_2, color2, SAMPLE_URI, PREFS);
        assertGadgetState(secondColfirstRow, PORTLET_ID_3, color5, MSN_URI, EMPTY_STRING_MAP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNullDashboardState()
    {
        // Invoke
        dashboardStateStoreManager.update(null, Collections.<DashboardChange>emptyList());

        // Check
        verifyNoMoreInteractions(mockClusterLock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNullChanges()
    {
        // Set up
        final DashboardState dashboardState = dashboard(DASHBOARD_ID).title(DASHBOARD_TITLE).build();

        // Invoke
        dashboardStateStoreManager.update(dashboardState, null);

        // Check
        verifyNoMoreInteractions(mockClusterLock);
    }

    @Test(expected = DashboardStateStoreException.class)
    public void testUpdateDashboardThatDoesNotExist()
    {
        // Set up
        final DashboardState dashboardState = dashboard(DASHBOARD_ID).title(DASHBOARD_TITLE).build();
        when(mockPortalPageStore.getPortalPage(PORTAL_PAGE_ID)).thenReturn(null);

        // Invoke
        dashboardStateStoreManager.update(dashboardState, Collections.<DashboardChange>emptyList());

        // Check
        verifyWriteLockedAndUnlocked();
    }

    private void verifyWriteLockedAndUnlocked()
    {
        verify(mockClusterLock).lock();
        verify(mockClusterLock).unlock();
    }

    @Test
    public void testStoreDashboardWithPortalPageUpdate()
    {
        when(mockPortalPageStore.updatePortalPageOptimisticLock(PORTAL_PAGE_ID, 0L)).thenReturn(true);

        final PortalPage portalPage = PortalPage
                .id(PORTAL_PAGE_ID)
                .name(PORTAL_NAME)
                .description("")
                .owner(ADMIN)
                .favouriteCount(0L)
                .layout(AA)
                .version(0L)
                .build();
        when(mockPortalPageStore.getPortalPage(PORTAL_PAGE_ID)).thenReturn(portalPage);

        final PortalPage newPortalPage = PortalPage
                .id(PORTAL_PAGE_ID)
                .name(DASHBOARD_TITLE)
                .description("")
                .owner(ADMIN)
                .favouriteCount(0L)
                .layout(AAA)
                .version(0L)
                .build();
        when(mockPortletConfigurationStore.getByPortalPage(PORTAL_PAGE_ID)).thenReturn(getPortletConfigurationMocks());
        when(mockPortletConfigurationStore.addGadget(
                PORTAL_PAGE_ID, PORTLET_CONFIGURATION_ID, 1, 1, NEW_GADGET_URI, color2, EMPTY_STRING_MAP))
                .thenReturn(null);

        // Define the new dashboard
        final GadgetState gadget1 = buildGadget(PORTLET_ID_1, GOOGLE_URI, color6);
        final GadgetState gadget3 = buildGadget(PORTLET_CONFIGURATION_ID, NEW_GADGET_URI, color2);
        final GadgetState gadget4 = buildGadget(PORTLET_ID_3, MSN_URI, color5);

        final List<GadgetState> col1 = Collections.emptyList();
        final List<GadgetState> col2 = ImmutableList.of(gadget1, gadget3);
        final List<GadgetState> col3 = ImmutableList.of(gadget4);
        final List<List<GadgetState>> columns = ImmutableList.of(col1, col2, col3);
        final DashboardState state = dashboard(DASHBOARD_ID).title(DASHBOARD_TITLE).layout(AAA).columns(columns).build();

        final AtomicBoolean retrieveCalled = new AtomicBoolean(false);
        final JiraDashboardStateStoreManager stateStore = new JiraDashboardStateStoreManager(
                mockPortalPageStore, mockPortletConfigurationStore, mockPortalPageManager, mockClusterLockService)
        {
            @Override
            public DashboardState retrieve(final DashboardId dashboardId)
                    throws DashboardNotFoundException, DashboardStateStoreException
            {
                retrieveCalled.set(true);
                return state;
            }
        };

        // Invoke
        final DashboardState newState = stateStore.update(state, Collections.<DashboardChange>emptyList());

        // Check
        assertEquals(state, newState);
        assertTrue("com.atlassian.jira.dashboard.JiraDashboardStateStoreManager.retrieve should have been called",
                retrieveCalled.get());
        verify(mockPortalPageStore).update(newPortalPage);
        verify(mockPortletConfigurationStore).store(eqPortletConfiguration(
                new PortletConfigurationImpl(PORTLET_ID_1, PORTAL_PAGE_ID, 1, 0, GOOGLE_URI, color6, EMPTY_STRING_MAP)));
        verify(mockPortletConfigurationStore).store(eqPortletConfiguration(
                new PortletConfigurationImpl(PORTLET_ID_3, PORTAL_PAGE_ID, 2, 0, MSN_URI, color5, EMPTY_STRING_MAP)));
        verify(mockPortletConfigurationStore).delete(eqPortletConfiguration(
                new PortletConfigurationImpl(PORTLET_ID_2, PORTAL_PAGE_ID, 0, 1, SAMPLE_URI, color2, PREFS)));
    }

    @Test(expected = DashboardStateStoreException.class)
    public void testFindGagdetByIdDoesntExist()
    {
        // Set up
        final long portletId = 1;
        when(mockPortletConfigurationStore.getByPortletId(portletId)).thenReturn(null);
        final GadgetId gadgetId = asGadgetId(portletId);

        // Invoke
        dashboardStateStoreManager.findDashboardWithGadget(gadgetId);
    }

    @Test(expected = DashboardNotFoundException.class)
    public void testFindGagdetByIdDashboardDoesntExist()
    {
        // Set up
        final PortletConfiguration portletConfiguration = new PortletConfigurationImpl(
                10027L, PORTAL_PAGE_ID, 0, 0, null, color8, EMPTY_STRING_MAP);
        final long portletId = 1;
        final GadgetId gadgetId = asGadgetId(portletId);

        when(mockPortletConfigurationStore.getByPortletId(portletId)).thenReturn(portletConfiguration);
        when(mockPortalPageStore.getPortalPage(PORTAL_PAGE_ID)).thenReturn(null);

        // Invoke
        dashboardStateStoreManager.findDashboardWithGadget(gadgetId);
    }

    @Test
    public void testFindGagdetByIdSuccess()
    {
        final PortletConfiguration portletConfiguration = new PortletConfigurationImpl(
                10027L, PORTAL_PAGE_ID, 0, 0, null, color8, EMPTY_STRING_MAP);
        final long portletId = 1;
        final DashboardState state = dashboard(DASHBOARD_ID).title(DASHBOARD_TITLE).layout(AAA).build();
        final GadgetId gadgetId = asGadgetId(portletId);

        when(mockPortletConfigurationStore.getByPortletId(portletId)).thenReturn(portletConfiguration);

        final JiraDashboardStateStoreManager stateStore = new JiraDashboardStateStoreManager(
                mockPortalPageStore, mockPortletConfigurationStore, mockPortalPageManager, mockClusterLockService)
        {
            @Override
            public DashboardState retrieve(final DashboardId dashboardId)
                    throws DashboardNotFoundException, DashboardStateStoreException
            {
                return state;
            }
        };

        // Invoke
        final DashboardState retrievedState = stateStore.findDashboardWithGadget(gadgetId);

        // Check
        assertEquals(state, retrievedState);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDashboardId()
    {
        dashboardStateStoreManager.remove(null);
    }

    @Test
    public void testRemoveDashboard()
    {
        // Invoke
        dashboardStateStoreManager.remove(DASHBOARD_ID);

        // Check
        verify(mockPortalPageManager).delete(PORTAL_PAGE_ID);
        verifyWriteLockedAndUnlocked();
    }

    private List<PortletConfiguration> getPortletConfigurationMocks()
    {
        return ImmutableList.<PortletConfiguration>of(
                new PortletConfigurationImpl(PORTLET_ID_1, PORTAL_PAGE_ID, 0, 0, GOOGLE_URI, color1, EMPTY_STRING_MAP),
                new PortletConfigurationImpl(PORTLET_ID_2, PORTAL_PAGE_ID, 0, 1, SAMPLE_URI, color2, PREFS),
                new PortletConfigurationImpl(PORTLET_ID_3, PORTAL_PAGE_ID, 1, 1, MSN_URI, color5, EMPTY_STRING_MAP)
        );
    }

    private List<List<PortletConfiguration>> getSortedPortletConfigurationMocks()
    {
        final List<List<PortletConfiguration>> outerList = ImmutableList.<List<PortletConfiguration>>of(
            new ArrayList<PortletConfiguration>(),
            new ArrayList<PortletConfiguration>()
        );
        for (final PortletConfiguration pc : getPortletConfigurationMocks())
        {
            outerList.get(pc.getColumn()).add(pc);
        }
        return outerList;
    }

    private static PortletConfiguration eqPortletConfiguration(final PortletConfiguration expected)
    {
        return argThat(new PortletConfigurationMatcher(expected));
    }

    private static class PortletConfigurationMatcher extends TypeSafeMatcher<PortletConfiguration>
    {
        private final PortletConfiguration expected;

        PortletConfigurationMatcher(final PortletConfiguration expected)
        {
            this.expected = expected;
        }

        @Override
        protected boolean matchesSafely(final PortletConfiguration other)
        {
            return other.getId().equals(expected.getId()) &&
                    other.getColor().equals(expected.getColor()) &&
                    other.getColumn().equals(expected.getColumn()) &&
                    other.getRow().equals(expected.getRow()) &&
                    other.getUserPrefs().equals(expected.getUserPrefs()) &&
                    other.getDashboardPageId().equals(expected.getDashboardPageId()) &&
                    ObjectUtils.equals(other.getGadgetURI(), expected.getGadgetURI());
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("eqPortletConfiguration(").appendValue(expected);
        }
    }
}
