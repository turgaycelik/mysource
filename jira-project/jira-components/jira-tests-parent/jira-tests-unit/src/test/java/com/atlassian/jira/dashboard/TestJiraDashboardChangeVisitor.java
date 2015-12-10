package com.atlassian.jira.dashboard;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.DashboardState;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.spi.DashboardStateStoreException;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;
import com.atlassian.gadgets.dashboard.spi.changes.AddGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.GadgetColorChange;
import com.atlassian.gadgets.dashboard.spi.changes.RemoveGadgetChange;
import com.atlassian.gadgets.dashboard.spi.changes.UpdateLayoutChange;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageStore;
import com.atlassian.jira.portal.PortletConfigurationImpl;
import com.atlassian.jira.portal.PortletConfigurationStore;
import com.atlassian.jira.user.MockApplicationUser;

import org.junit.Test;

import static com.atlassian.gadgets.dashboard.DashboardState.dashboard;
import static org.junit.Assert.fail;

public class TestJiraDashboardChangeVisitor extends MockControllerTestCase
{
    @Test
    public void testGadgetColorChangeNoGadget()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        expect(mockPortletConfigurationStore.getByPortletId(-999L)).andReturn(null);
        final DashboardState updatedState = dashboard(DashboardId.valueOf(Long.toString(10020))).title("My Dashboard").layout(Layout.AAA).build();
        mockController.replay();
        final JiraDashboardChangeVisitor visitor = new JiraDashboardChangeVisitor(updatedState, mockPortletConfigurationStore, null);

        try
        {
            visitor.visit(new GadgetColorChange(GadgetId.valueOf("-999"), Color.color8));
            fail("Should have thrown exception");
        }
        catch (DashboardStateStoreException e)
        {
            //yay
        }
    }

    @Test
    public void testGadgetColorChangeSuccess()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        expect(mockPortletConfigurationStore.getByPortletId(10000L)).andReturn(new PortletConfigurationImpl(10000L, 10020L, 0, 0,null, Color.color1, Collections.<String, String>emptyMap()));
        mockPortletConfigurationStore.updateGadgetColor(10000L, Color.color8);

        final DashboardState updatedState = dashboard(DashboardId.valueOf(Long.toString(10020))).title("My Dashboard").layout(Layout.AAA).build();
        mockController.replay();
        final JiraDashboardChangeVisitor visitor = new JiraDashboardChangeVisitor(updatedState, mockPortletConfigurationStore, null);

        visitor.visit(new GadgetColorChange(GadgetId.valueOf("10000"), Color.color8));
    }

    @Test
    public void testGadgetRemove()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        final PortletConfigurationImpl pcToDelete = new PortletConfigurationImpl(10020L, 10025L, 0, 1, null, Color.color1, Collections.<String, String>emptyMap());
        expect(mockPortletConfigurationStore.getByPortletId(10020L)).andReturn(pcToDelete);
        mockPortletConfigurationStore.delete(pcToDelete);

        mockPortletConfigurationStore.updateGadgetPosition(10000L, 0, 0, 10025L);
        mockPortletConfigurationStore.updateGadgetPosition(10030L, 1, 0, 10025L);

        final List<GadgetState> firstColumn = new ArrayList<GadgetState>();
        firstColumn.add(GadgetState.gadget(GadgetId.valueOf("10000")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        firstColumn.add(GadgetState.gadget(GadgetId.valueOf("10030")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        final List<List<GadgetState>> columns = new ArrayList<List<GadgetState>>();
        columns.add(firstColumn);
        columns.add(Collections.<GadgetState>emptyList());

        final DashboardState updatedState = dashboard(DashboardId.valueOf(Long.toString(10025))).title("My Dashboard").
                layout(Layout.AA).columns(columns).build();
        mockController.replay();
        final JiraDashboardChangeVisitor visitor = new JiraDashboardChangeVisitor(updatedState, mockPortletConfigurationStore, null);

        visitor.visit(new RemoveGadgetChange(GadgetId.valueOf("10020")));
    }

    @Test
    public void testUpdateLayout()
    {
        final PortalPageStore mockPortalPageStore = mockController.getMock(PortalPageStore.class);
        final PortalPage portalPage = PortalPage.id(10025L).name("Test Dashboard").description("").owner(new MockApplicationUser("admin")).favouriteCount(0L).layout(Layout.AAA).version(0L).build();
        expect(mockPortalPageStore.getPortalPage(10025L)).andReturn(portalPage);
        expect(mockPortalPageStore.update(isA(PortalPage.class))).andReturn(portalPage);

        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        expect(mockPortletConfigurationStore.getByPortletId(10000L)).andReturn(new PortletConfigurationImpl(10000L, 10025L, 0, 0, null, Color.color1, Collections.<String, String>emptyMap()));
        expect(mockPortletConfigurationStore.getByPortletId(10030L)).andReturn(new PortletConfigurationImpl(10030L, 10025L, 0, 1, null, Color.color1, Collections.<String, String>emptyMap()));

        //only gadgets in the second column need their position changed.
        expect(mockPortletConfigurationStore.getByPortletId(10040L)).andReturn(new PortletConfigurationImpl(10040L, 10025L, 2, 0, null, Color.color1, Collections.<String, String>emptyMap()));
        mockPortletConfigurationStore.updateGadgetPosition(10040L, 0, 1, 10025L);
        expect(mockPortletConfigurationStore.getByPortletId(10050L)).andReturn(new PortletConfigurationImpl(10050L, 10025L, 2, 1, null, Color.color1, Collections.<String, String>emptyMap()));
        mockPortletConfigurationStore.updateGadgetPosition(10050L, 1, 1, 10025L);
        expect(mockPortletConfigurationStore.getByPortletId(10060L)).andReturn(new PortletConfigurationImpl(10060L, 10025L, 2, 2, null, Color.color1, Collections.<String, String>emptyMap()));
        mockPortletConfigurationStore.updateGadgetPosition(10060L, 2, 1, 10025L);

        final List<GadgetId> firstColumn = new ArrayList<GadgetId>();
        firstColumn.add(GadgetId.valueOf("10000"));
        firstColumn.add(GadgetId.valueOf("10030"));
        final List<GadgetId> secondColumn = new ArrayList<GadgetId>();
        secondColumn.add(GadgetId.valueOf("10040"));
        secondColumn.add(GadgetId.valueOf("10050"));
        secondColumn.add(GadgetId.valueOf("10060"));
        final List<List<GadgetId>> columns = new ArrayList<List<GadgetId>>();
        columns.add(firstColumn);
        columns.add(secondColumn);

        final DashboardState updatedState = dashboard(DashboardId.valueOf(Long.toString(10025))).title("My Dashboard").
                layout(Layout.AA).build();
        mockController.replay();
        final JiraDashboardChangeVisitor visitor = new JiraDashboardChangeVisitor(updatedState, mockPortletConfigurationStore, mockPortalPageStore);

        visitor.visit(new UpdateLayoutChange(Layout.AA, new GadgetLayout(columns)));
    }

    @Test
    public void testAddGadget()
    {
        final PortletConfigurationStore mockPortletConfigurationStore = mockController.getMock(PortletConfigurationStore.class);
        expect(mockPortletConfigurationStore.addGadget(10025L, 10070L, 1, 1, URI.create("http://example.gadet/spec2.xml"),
                Color.color1, Collections.<String, String>emptyMap())).andReturn(null);
        mockPortletConfigurationStore.updateGadgetPosition(10050L, 2, 1, 10025L);
        mockPortletConfigurationStore.updateGadgetPosition(10060L, 3, 1, 10025L);

        final List<GadgetState> firstColumn = new ArrayList<GadgetState>();
        firstColumn.add(GadgetState.gadget(GadgetId.valueOf("10000")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        firstColumn.add(GadgetState.gadget(GadgetId.valueOf("10030")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        final List<GadgetState> secondColumn = new ArrayList<GadgetState>();
        secondColumn.add(GadgetState.gadget(GadgetId.valueOf("10040")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        secondColumn.add(GadgetState.gadget(GadgetId.valueOf("10070")).specUri(URI.create("http://example.gadet/spec2.xml")).build());
        secondColumn.add(GadgetState.gadget(GadgetId.valueOf("10050")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        secondColumn.add(GadgetState.gadget(GadgetId.valueOf("10060")).specUri(URI.create("http://example.gadet/spec1.xml")).build());
        final List<List<GadgetState>> columns = new ArrayList<List<GadgetState>>();
        columns.add(firstColumn);
        columns.add(secondColumn);

        final DashboardState updatedState = dashboard(DashboardId.valueOf(Long.toString(10025))).title("My Dashboard").
                layout(Layout.AA).columns(columns).build();
        mockController.replay();
        final JiraDashboardChangeVisitor visitor = new JiraDashboardChangeVisitor(updatedState, mockPortletConfigurationStore, null);

        final GadgetState state = GadgetState.gadget(GadgetId.valueOf("10070")).specUri(URI.create("http://example.gadet/spec2.xml")).color(Color.color1).build();
        visitor.visit(new AddGadgetChange(state, DashboardState.ColumnIndex.ONE, 1));
    }
}
