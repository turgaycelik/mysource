package com.atlassian.jira.dashboard;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.gadgets.dashboard.spi.changes.AddGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.DashboardChange;
import com.atlassian.gadgets.dashboard.spi.changes.GadgetColorChange;
import com.atlassian.gadgets.dashboard.spi.changes.RemoveGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateGadgetUserPrefsChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateLayoutChange;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletConfigurationStore;

import static com.atlassian.jira.dashboard.DashboardUtil.toLong;

/**
 * Implements the Dashboard plugin DashboardChange.Visitor.  This class essentially handles fine grained update
 * operations of the dashboard, such that the entire dashboard state doesn't have to be rewritten to the database on
 * every update.
 *
 * @since v4.0
 */
class JiraDashboardChangeVisitor implements DashboardChange.Visitor
{
    private final DashboardState updatedState;
    private final PortletConfigurationStore portletConfigurationStore;
    private final PortalPageStore portalPageStore;

    JiraDashboardChangeVisitor(final DashboardState updatedState, final PortletConfigurationStore portletConfigurationStore,
            final PortalPageStore portalPageStore)
    {
        this.updatedState = updatedState;
        this.portletConfigurationStore = portletConfigurationStore;
        this.portalPageStore = portalPageStore;
    }

    /**
     * This method can be used to submit an iterable of {@link com.atlassian.gadgets.dashboard.spi.changes.DashboardChange} to
     * be visited by 'this' visitor, rather than having to iterate over them manually.
     *
     * @param changes A list of dashboardchanges to visit by this visitor.
     */
    public void accept(Iterable<DashboardChange> changes)
    {
        for (DashboardChange change : changes)
        {
            change.accept(this);
        }
    }

    public void visit(final AddGadgetChange addGadgetChange)
    {
        //We'll skip all gadgets until we get to the one that's being added.  For all following gadgets we'll update the
        //row position.
        final DashboardState.ColumnIndex column = addGadgetChange.getColumnIndex();
        final Iterable<GadgetState> gadgets = updatedState.getGadgetsInColumn(column);
        final GadgetState newGadgetState = addGadgetChange.getState();
        final GadgetId newGadgetId = newGadgetState.getId();
        int row = 0;
        boolean skipGadget = true;
        for (GadgetState gadget : gadgets)
        {
            final boolean isAddedGadget = gadget.getId().equals(newGadgetId);
            if (isAddedGadget)
            {
                skipGadget = false;
                final Long portalPageId = toLong(updatedState.getId());

                portletConfigurationStore.addGadget(
                        portalPageId, toLong(newGadgetId), column.index(), row, newGadgetState.getGadgetSpecUri(),
                        newGadgetState.getColor(), newGadgetState.getUserPrefs());
            }
            else if (!skipGadget)
            {
                portletConfigurationStore.updateGadgetPosition(toLong(gadget.getId()), row, column.index(), toLong(updatedState.getId()));
            }
            row++;
        }
    }

    public void visit(final GadgetColorChange gadgetColorChange)
    {
        final Long gadgetId = toLong(gadgetColorChange.getGadgetId());
        final PortletConfiguration portletConfiguration = portletConfigurationStore.getByPortletId(gadgetId);
        if (portletConfiguration != null)
        {
            portletConfigurationStore.updateGadgetColor(gadgetId, gadgetColorChange.getColor());
        }
        else
        {
            throw new DashboardStateStoreException("Gadget with id '" + gadgetColorChange.getGadgetId() + "' not found for color change.");
        }
    }

    public void visit(final RemoveGadgetChange removeGadgetChange)
    {
        final PortletConfiguration gadgetToRemove = portletConfigurationStore.getByPortletId(toLong(removeGadgetChange.getGadgetId()));
        final Iterable<GadgetState> gadgets = updatedState.getGadgetsInColumn(DashboardState.ColumnIndex.from(gadgetToRemove.getColumn()));
        portletConfigurationStore.delete(gadgetToRemove);
        int row = 0;
        for (GadgetState gadget : gadgets)
        {
            portletConfigurationStore.updateGadgetPosition(toLong(gadget.getId()), row, gadgetToRemove.getColumn(), toLong(updatedState.getId()));
            row++;
        }
    }

    public void visit(final UpdateGadgetUserPrefsChange updateGadgetUserPrefsChange)
    {
        portletConfigurationStore.updateUserPrefs(toLong(updateGadgetUserPrefsChange.getGadgetId()), updateGadgetUserPrefsChange.getPrefValues());
    }

    public void visit(final UpdateLayoutChange updateLayoutChange)
    {
        final Long dashboardId = toLong(updatedState.getId());
        final PortalPage portalPage = portalPageStore.getPortalPage(dashboardId);
        if (!portalPage.getLayout().equals(updateLayoutChange.getLayout()))
        {
            portalPageStore.update(PortalPage.portalPage(portalPage).layout(updateLayoutChange.getLayout()).build());
        }

        final GadgetLayout newLayout = updateLayoutChange.getGadgetLayout();
        for (int column = 0; column < newLayout.getNumberOfColumns(); column++)
        {
            int row = 0;
            final Iterable<GadgetId> gadgets = newLayout.getGadgetsInColumn(column);
            for (GadgetId gadgetId : gadgets)
            {
                final Long gadgetIdLong = toLong(gadgetId);
                final PortletConfiguration portletConfiguration = portletConfigurationStore.getByPortletId(gadgetIdLong);
                if (portletConfiguration.getColumn() != column || portletConfiguration.getRow() != row)
                {
                    portletConfigurationStore.updateGadgetPosition(gadgetIdLong, row, column, dashboardId);
                }
                row++;
            }
        }
    }
}