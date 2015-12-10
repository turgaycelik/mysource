package com.atlassian.jira.dashboard;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardNotFoundException;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStore;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.util.concurrent.Function;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static com.atlassian.jira.dashboard.DashboardUtil.toLong;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides CRUD operations for dashboards.  Uses the existing {@link com.atlassian.jira.portal.PortalPageStore} and
 * {@link com.atlassian.jira.portal.PortletConfigurationStore} implementations. Note that this class does not need to do
 * any permission checks, since this is the responsibility of the {@link com.atlassian.jira.dashboard.permission.JiraPermissionService}.
 *
 * @since v4.0
 */
public class JiraDashboardStateStoreManager implements DashboardStateStore
{
    private static final Logger log = LoggerFactory.getLogger(JiraDashboardStateStoreManager.class);

    @VisibleForTesting
    static String getWriteLockName(final DashboardId dashboardId)
    {
        return JiraDashboardStateStoreManager.class.getName() + ".dashboard-" + dashboardId;
    }

    private final ClusterLockService clusterLockService;
    private final PortalPageManager portalPageManager;
    private final PortalPageStore portalPageStore;
    private final PortletConfigurationStore portletConfigurationStore;

    public JiraDashboardStateStoreManager(
            final PortalPageStore portalPageStore, final PortletConfigurationStore portletConfigurationStore,
            final PortalPageManager portalPageManager, final ClusterLockService clusterLockService)
    {
        this.clusterLockService = clusterLockService;
        this.portalPageManager = portalPageManager;
        this.portalPageStore = portalPageStore;
        this.portletConfigurationStore = portletConfigurationStore;
    }

    public DashboardState retrieve(final DashboardId dashboardId)
            throws DashboardNotFoundException, DashboardStateStoreException
    {
        notNull("dashboardId", dashboardId);
        final Long portalPageId = toLong(dashboardId);

        // We read the dashboard under an optimistic lock to ensure we read
        // a consistent state from the DB, not some 'half-written' state.
        Long versionBefore, versionAfter;
        DashboardState dashboardState;
        do
        {
            versionBefore = getPortalPageVersion(portalPageId);
            dashboardState = getDashboardState(dashboardId, portalPageId);
            versionAfter = getPortalPageVersion(portalPageId);
        }
        while (!ObjectUtils.equals(versionBefore, versionAfter));
        return dashboardState;
    }

    @Nullable
    private Long getPortalPageVersion(@Nullable final Long portalPageId)
    {
        if (portalPageId == null)
        {
            return null;
        }
        final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
        return portalPage == null ? null : portalPage.getVersion();
    }

    private DashboardState getDashboardState(final DashboardId dashboardId, final Long portalPageId)
    {
        try
        {
            final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
            if (portalPage == null)
            {
                throw new DashboardNotFoundException(dashboardId);
            }

            final List<List<PortletConfiguration>> pcColumns = portalPageManager.getPortletConfigurations(portalPageId);

            // Convert the JIRA portalPage to a DashboardState/GadgetState class
            final List<List<GadgetState>> dashboardColumns = new ArrayList<List<GadgetState>>();

            for (List<PortletConfiguration> pcColumn : pcColumns)
            {
                //init the column
                final ArrayList<GadgetState> column = new ArrayList<GadgetState>();
                for (PortletConfiguration portletConfiguration : pcColumn)
                {
                    column.add(toGadgetState.get(portletConfiguration));
                }
                dashboardColumns.add(column);
            }

            return dashboard(dashboardId)
                    .title(portalPage.getName())
                    .version(portalPage.getVersion() == null ? 1L : portalPage.getVersion())
                    .columns(dashboardColumns)
                    .layout(portalPage.getLayout())
                    .build();
        }
        catch (DataAccessException e)
        {
            throw new DashboardStateStoreException(
                    "Unknown error occurred while retrieving dashboard with id '" + portalPageId + "'.", e);
        }
    }

    public DashboardState update(final DashboardState dashboardState, final Iterable<DashboardChange> dashboardChanges)
            throws DashboardStateStoreException
    {
        notNull("dashboardState", dashboardState);
        notNull("dashboardChanges", dashboardChanges);

        final DashboardId dashboardId = dashboardState.getId();
        // Writing the dashboard is done under a cluster lock (by dashboardId)
        // to ensure a consistent state is written to the DB by only one thread.
        final Lock writeLock = clusterLockService.getLockForName(getWriteLockName(dashboardId));
        writeLock.lock();
        try
        {
            acquireOptimisticWriteLock(dashboardState);

            // If no specific changes were submitted, persist the entire dashboard state
            if (!dashboardChanges.iterator().hasNext())
            {
                return storeDashboardState(dashboardState);
            }

            new JiraDashboardChangeVisitor(dashboardState, portletConfigurationStore, portalPageStore)
                    .accept(dashboardChanges);

            final DashboardState storedState = retrieve(dashboardId);
            // This should never happen, but if updating via {@link com.atlassian.gadgets.spi.changes.DashboardChange}s
            // doesn't work, we try rewriting the entire dashboard state from scratch.
            if (!storedState.equals(dashboardState))
            {
                log.warn("Stored state for dashboard with id '{}' is not the same as in-memory state."
                        + " Trying to rewrite the entire state...", dashboardId);
                return storeDashboardState(dashboardState);
            }
            return storedState;
        }
        catch (final DataAccessException e)
        {
            throw new DashboardStateStoreException("Error updating dashboard state with id '" + dashboardId + "'.", e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private void acquireOptimisticWriteLock(final DashboardState dashboardState)
    {
        // Optimistic lock solution. The *very first* thing we do is update the dashboard version.
        // If this fails, another thread has updated in the meantime and we throw an exception.
        final DashboardId dashboardId = dashboardState.getId();
        final boolean optimisticLock =
                portalPageStore.updatePortalPageOptimisticLock(toLong(dashboardId), dashboardState.getVersion());
        if (!optimisticLock)
        {
            // looks like the optimistic lock (i.e. version) for this dashboard was already out of date.
            throw new DashboardStateStoreException(
                    "Dashboard with id '" + dashboardId + "' is out of sync with the currently persisted state.");
        }
    }

    public void remove(final DashboardId dashboardId) throws DashboardStateStoreException
    {
        notNull("dashboardId", dashboardId);

        final Long portalPageId = toLong(dashboardId);
        // Removing the dashboard is done under a cluster lock (by dashboardId)
        // to ensure all threads trying to read this dashboard will block
        final Lock writeLock = clusterLockService.getLockForName(getWriteLockName(dashboardId));
        writeLock.lock();
        try
        {
            portalPageManager.delete(portalPageId);
        }
        catch (final DataAccessException e)
        {
            throw new DashboardStateStoreException("Error removing dashboard state with id'" + dashboardId + "'.", e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public DashboardState findDashboardWithGadget(final GadgetId gadgetId) throws DashboardNotFoundException
    {
        notNull("gagdetId", gadgetId);

        try
        {
            final PortletConfiguration portletConfiguration = portletConfigurationStore.getByPortletId(toLong(gadgetId));
            if (portletConfiguration == null)
            {
                throw new DashboardStateStoreException("Gadget with id '" + gadgetId + "' not found!");
            }
            return retrieve(DashboardId.valueOf(Long.toString(portletConfiguration.getDashboardPageId())));
        }
        catch (DataAccessException e)
        {
            throw new DashboardStateStoreException("Error looking up gadget with id '" + gadgetId + "'.", e);
        }
    }

    private DashboardState storeDashboardState(final DashboardState dashboardState)
    {
        notNull("dashboardState", dashboardState);

        final DashboardId dashboardId = dashboardState.getId();
        final long portalPageId = toLong(dashboardId);

        final PortalPage portalPage = portalPageStore.getPortalPage(portalPageId);
        if (portalPage == null)
        {
            throw new DashboardStateStoreException("No portal page found with id '" + portalPageId + "'");
        }
        updatePortalPage(portalPage, dashboardState);

        final Map<Long, PortletConfiguration> oldPortlets = getCurrentPortletConfigurationsMap(portalPageId);
        for (DashboardState.ColumnIndex columnIndex : dashboardState.getLayout().getColumnRange())
        {
            int row = 0;
            for (final GadgetState gadgetState : dashboardState.getGadgetsInColumn(columnIndex))
            {
                final long gadgetId = toLong(gadgetState.getId());
                //update existing portlets
                if (oldPortlets.containsKey(gadgetId))
                {
                    final PortletConfiguration oldPortletConfiguration = oldPortlets.get(gadgetId);
                    oldPortletConfiguration.setColumn(columnIndex.index());
                    oldPortletConfiguration.setRow(row++);
                    oldPortletConfiguration.setColor(gadgetState.getColor());
                    oldPortletConfiguration.setUserPrefs(gadgetState.getUserPrefs());
                    portletConfigurationStore.store(oldPortletConfiguration);
                    oldPortlets.remove(gadgetId);
                }
                else
                {
                    portletConfigurationStore.addGadget(
                            portalPageId, gadgetId, columnIndex.index(), row++, gadgetState.getGadgetSpecUri(), gadgetState.getColor(),
                            gadgetState.getUserPrefs());
                }
            }
        }

        //delete any portlets left over in the oldPortlets map
        for (PortletConfiguration existingPortlet : oldPortlets.values())
        {
            portletConfigurationStore.delete(existingPortlet);
        }

        return retrieve(dashboardId);
    }

    private void updatePortalPage(final PortalPage portalPage, final DashboardState dashboardState)
    {
        //update the portalPageStore's title and layout if they changed.
        if (!portalPage.getLayout().equals(dashboardState.getLayout()) ||
                !StringUtils.equals(portalPage.getName(), dashboardState.getTitle()))
        {
            final PortalPage.Builder builder = PortalPage.portalPage(portalPage);
            builder.name(dashboardState.getTitle());
            builder.layout(dashboardState.getLayout());
            portalPageStore.update(builder.build());
        }
    }

    private Map<Long, PortletConfiguration> getCurrentPortletConfigurationsMap(final Long portalPageId)
    {
        final Map<Long, PortletConfiguration> ret = new HashMap<Long, PortletConfiguration>();
        final List<PortletConfiguration> list = portletConfigurationStore.getByPortalPage(portalPageId);
        for (PortletConfiguration portletConfiguration : list)
        {
            ret.put(portletConfiguration.getId(), portletConfiguration);
        }
        return ret;
    }

    /**
     * Converts a PortletConfiguration to a GadgetState.
     */
    private final Function<PortletConfiguration, GadgetState> toGadgetState = new Function<PortletConfiguration, GadgetState>()
    {
        public GadgetState get(final PortletConfiguration portletConfiguration)
        {
            URI gadgetUri = portletConfiguration.getGadgetURI();
            if (gadgetUri == null)
            {
                gadgetUri = URI.create("/invalid/legacy/portlet/Please_remove_this_gadget_from_your_dashboard!");
            }

            final GadgetId gadgetId = GadgetId.valueOf(portletConfiguration.getId().toString());
            return GadgetState
                    .gadget(gadgetId)
                    .specUri(gadgetUri)
                    .color(portletConfiguration.getColor())
                    .userPrefs(portletConfiguration.getUserPrefs())
                    .build();
        }
    };
}
