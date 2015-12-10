package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboard;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboardManager;
import com.atlassian.jira.functest.config.dashboard.ConfigGadget;
import com.atlassian.jira.functest.config.dashboard.ConfigGadgetManager;
import com.atlassian.jira.functest.config.dashboard.DefaultConfigDashboardManager;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntity;
import com.atlassian.jira.functest.config.sharing.ConfigSharedEntityCleaner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.LogicalOperator;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Test the {@link com.atlassian.jira.functest.config.dashboard.DefaultConfigDashboardManager}.
 *
 * @since v4.2
 */
public class TestDefaultConfigDashboardManager extends TestCase
{
    private final static String ELEMENT_DASHBOARD = "PortalPage";
    private static final String ADMIN = "admin";

    public void testLoadDashboardsNoId() throws Exception
    {
        final IMocksControl iMocksControl = EasyMock.createControl();

        //This dashboard has no ID. It should fail.
        ConfigDashboard one = new ConfigDashboard().setName("jack");
        ConfigDashboard two = new ConfigDashboard().setId(19L);

        ConfigDashboardManager manager = new DefaultConfigDashboardManager(createDocument(one, two),
                iMocksControl.createMock(ConfigGadgetManager.class), iMocksControl.createMock(ConfigSharedEntityCleaner.class));

        iMocksControl.replay();
        try
        {
            manager.loadDashboards();
            fail("Expected to fail because a gadget did not have an id.");
        }
        catch (ConfigException expected)
        {
        }

        iMocksControl.verify();
    }

    public void testLoadDashboardsNoGadgets() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);

        //We expect no gadgets.
        EasyMock.expect(configGadgetManager.loadGadgets()).andReturn(Collections.<ConfigGadget>emptyList());

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        ConfigDashboardManager manager = new DefaultConfigDashboardManager(createDocument(one, two),
                configGadgetManager, mocks.createMock(ConfigSharedEntityCleaner.class));

        mocks.replay();

        final List<ConfigDashboard> dashboardList = manager.loadDashboards();
        assertEquals(Arrays.asList(one, two), dashboardList);

        mocks.verify();
    }

    public void testLoadDashboardsWithGadgets() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        ConfigDashboard three = new ConfigDashboard().setId(20L).setName("Nick\"s Dashboard");

        ConfigGadget gadget = new ConfigGadget().setDashboard(one.getId()).setId(15L);
        ConfigGadget gadget2 = new ConfigGadget().setDashboard(one.getId()).setId(16L);
        ConfigGadget gadget3 = new ConfigGadget().setDashboard(three.getId()).setId(16L);

        EasyMock.expect(configGadgetManager.loadGadgets()).andReturn(Arrays.asList(gadget, gadget2, gadget3));

        ConfigDashboardManager manager = new DefaultConfigDashboardManager(createDocument(one, two, three),
                configGadgetManager, mocks.createMock(ConfigSharedEntityCleaner.class));

        one.setGadgets(Arrays.asList(gadget, gadget2));
        three.setGadgets(Arrays.asList(gadget3));

        mocks.replay();

        final List<ConfigDashboard> dashboardList = manager.loadDashboards();
        assertEquals(Arrays.asList(one, two, three), dashboardList);

        mocks.verify();
    }

    public void testSaveDashboardsFailsForNew() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);
        final ConfigSharedEntityCleaner entityCleaner = mocks.createMock(ConfigSharedEntityCleaner.class);

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        ConfigDashboardManager manager = new DefaultConfigDashboardManager(createDocument(one),
                configGadgetManager, entityCleaner);

        mocks.replay();

        try
        {
            manager.saveDashboards(Arrays.asList(one, two));
            fail("Should not be able to add new dashboards - I'm too lazy to implement it.");
        }
        catch (ConfigException expected)
        {
        }

        mocks.verify();
    }

    public void testSaveDashboardsUpdateAndDeleteWithNoGadgets() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);
        final ConfigSharedEntityCleaner entityCleaner = mocks.createMock(ConfigSharedEntityCleaner.class);

        //We still have not gadgets yet.
        EasyMock.expect(configGadgetManager.saveGadgets(Collections.<ConfigGadget>emptyList())).andReturn(true);

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        //Dashboard to delete.
        ConfigDashboard three = new ConfigDashboard().setId(20L).setName("Nick\"s Dashboard");

        EasyMock.expect(entityCleaner.clean(three)).andReturn(true);

        final Document document = createDocument(one, two, three);
        ConfigDashboardManager manager = new DefaultConfigDashboardManager(document,
                configGadgetManager, entityCleaner);

        mocks.replay();

        two.setSequence(1L).setName("My Dashboard").setLayout("BBA");

        assertTrue(manager.saveDashboards(Arrays.asList(one, two)));
        assertEquals(Arrays.asList(one, two), readDashboards(document, Collections.<ConfigGadget>emptyList()));

        mocks.verify();
    }

    public void testSaveDashboardsNoChange() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);
        final ConfigSharedEntityCleaner entityCleaner = mocks.createMock(ConfigSharedEntityCleaner.class);

        //We still have not gadgets yet.
        EasyMock.expect(configGadgetManager.saveGadgets(Collections.<ConfigGadget>emptyList())).andReturn(false);

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        ConfigDashboard three = new ConfigDashboard().setId(20L).setName("Nick\"s Dashboard");

        final Document document = createDocument(one, two, three);
        ConfigDashboardManager manager = new DefaultConfigDashboardManager(document,
                configGadgetManager, entityCleaner);

        mocks.replay();

        assertFalse(manager.saveDashboards(Arrays.asList(one, two, three)));
        assertEquals(Arrays.asList(one, two, three), readDashboards(document, Collections.<ConfigGadget>emptyList()));

        mocks.verify();
    }

    public void testSaveDashboardsGadgetsOnlyChange() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);
        final ConfigSharedEntityCleaner entityCleaner = mocks.createMock(ConfigSharedEntityCleaner.class);

        //We still have not gadgets yet.
        EasyMock.expect(configGadgetManager.saveGadgets(Collections.<ConfigGadget>emptyList())).andReturn(true);

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        ConfigDashboard three = new ConfigDashboard().setId(20L).setName("Nick\"s Dashboard");

        final Document document = createDocument(one, two, three);
        ConfigDashboardManager manager = new DefaultConfigDashboardManager(document,
                configGadgetManager, entityCleaner);

        mocks.replay();

        assertTrue(manager.saveDashboards(Arrays.asList(one, two, three)));
        assertEquals(Arrays.asList(one, two, three), readDashboards(document, Collections.<ConfigGadget>emptyList()));

        mocks.verify();
    }

    public void testSaveDashboardsUpdateAndDeleteWithGadgetChanges() throws Exception
    {
        final IMocksControl mocks = EasyMock.createControl();
        final ConfigGadgetManager configGadgetManager = mocks.createMock(ConfigGadgetManager.class);
        final ConfigSharedEntityCleaner entityCleaner = mocks.createMock(ConfigSharedEntityCleaner.class);

        ConfigDashboard one = new ConfigDashboard().setId(1038383L).setName("System Dashboard")
                .setDescription("System dashboard").setFavouriteCount(5L).setLayout("AA").setSequence(5L).setVersion(2992L);

        ConfigDashboard two = new ConfigDashboard().setId(19L).setName("Test Dashboard").setFavouriteCount(39L)
                .setLayout("AAB").setSequence(-128283L).setOwner(ADMIN).setVersion(2929292927373873L);

        ConfigDashboard three = new ConfigDashboard().setId(20L).setName("Nick\"s Dashboard");

        //All the gagets are on the first dashboard in the config.
        ConfigGadget gadget1 = new ConfigGadget().setId(18L).setDashboard(one.getId());
        ConfigGadget gadget2 = new ConfigGadget().setId(19L).setDashboard(one.getId());
        ConfigGadget gadget3 = new ConfigGadget().setId(20L).setDashboard(one.getId());
        ConfigGadget gadget4 = new ConfigGadget().setId(21L).setDashboard(one.getId());

        //We have moved a couple of gadgets onto other dashboards.
        one.setGadgets(Arrays.asList(gadget1, gadget2));
        two.setGadgets(Arrays.asList(gadget3));
        three.getGadgets().add(gadget4);

        final Document document = createDocument(one, two, three);
        ConfigDashboardManager manager = new DefaultConfigDashboardManager(document,
                configGadgetManager, entityCleaner);

        //These are the expected gadgets. We expect their parent id to change.
        List<ConfigGadget> expectedGadget = Arrays.asList(new ConfigGadget().setId(gadget1.getId()).setDashboard(one.getId()),
                new ConfigGadget().setId(gadget2.getId()).setDashboard(one.getId()),
                new ConfigGadget().setId(gadget3.getId()).setDashboard(two.getId()));

        EasyMock.expect(configGadgetManager.saveGadgets(expectedGadget)).andReturn(true);

        //We expect this method to be called. We are only concerned that the shared entity has the correct id and type
        //rather than being a particular type.
        EasyMock.expect(entityCleaner.clean(EasyMock.cmp(three, new ConfigSharedEntityComparator(), LogicalOperator.EQUAL))).andReturn(false);

        mocks.replay();

        two.setName("Changed the name").setFavouriteCount(101010L);

        assertTrue(manager.saveDashboards(Arrays.asList(one, two)));
        assertEquals(Arrays.asList(one, two), readDashboards(document, expectedGadget));

        mocks.verify();
    }

    private static Document createDocument(ConfigDashboard... dashboards)
    {
        Element element = createRootElement();
        for (ConfigDashboard gadget : dashboards)
        {
            addDashboardToElement(gadget, element);
        }
        return element.getDocument();
    }

    private static Element addDashboardToElement(final ConfigDashboard dashboard, final Element parent)
    {
        final Element element = parent.addElement(ELEMENT_DASHBOARD);
        dashboard.save(element);
        return element;
    }

    private static List<ConfigDashboard> readDashboards(final Document parent, final Collection<? extends ConfigGadget> gadgets)
    {
        Multimap<Long, ConfigGadget> gadgetMap = ArrayListMultimap.create();
        for (ConfigGadget gadget : gadgets)
        {
            gadgetMap.put(gadget.getDashboardId(), gadget);
        }

        @SuppressWarnings ({ "unchecked" }) final List<Element> elements = parent.getRootElement().elements(ELEMENT_DASHBOARD);
        List<ConfigDashboard> dashboards = new ArrayList<ConfigDashboard>(elements.size());
        for (Element element : elements)
        {
            final ConfigDashboard dashboard = new ConfigDashboard(element);
            dashboard.setGadgets(gadgetMap.get(dashboard.getId()));
            dashboards.add(dashboard);
        }

        return dashboards;
    }

    private static Element createRootElement()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement("entity-engine-xml");
    }

    private static class ConfigSharedEntityComparator implements Comparator<ConfigSharedEntity>
    {
        public int compare(final ConfigSharedEntity o1, final ConfigSharedEntity o2)
        {
            int cmp = o1.getEntityType().compareTo(o2.getEntityType());
            if (cmp == 0)
            {
                cmp = o1.getId().compareTo(o2.getId());
            }
            return cmp;
        }
    }
}
