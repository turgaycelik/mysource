package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboard;
import com.atlassian.jira.functest.config.dashboard.ConfigGadget;
import junit.framework.TestCase;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.dashboard.ConfigDashboard}
 *
 * @since v4.2
 */
public class TestConfigDashboard extends TestCase
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_PAGENAME = "pagename";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_SEQUENCE = "sequence";
    private static final String ATTRIBUTE_LAYOUT = "layout";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_FAV_COUNT = "favCount";
    private static final String ADMIN = "admin";

    public void testSave() throws Exception
    {
        Element element = createElement("dontCareReally");
        ConfigDashboard dashboard = new ConfigDashboard().setId(1010L);
        assertTrue(dashboard.save(element));
        assertEquals(dashboard, elementToDashboard(element));


        element = createElement("stillDontCare");
        dashboard = new ConfigDashboard().setId(19393L).setFavouriteCount(10L).setLayout("AA").setVersion(6L)
                .setDescription("Description of some kind").setName("Some name").setOwner(ADMIN).setSequence(383L);
        assertTrue(dashboard.save(element));
        assertEquals(dashboard, elementToDashboard(element));
    }

    public void testSaveNoChange() throws Exception
    {
        ConfigDashboard dashboard = new ConfigDashboard().setId(1010L);
        Element element = dashboardToElement(dashboard);
        assertFalse(dashboard.save(element));
        assertEquals(dashboard, elementToDashboard(element));


        dashboard = new ConfigDashboard().setId(19393L).setFavouriteCount(10L).setLayout("AA").setVersion(6L)
                .setDescription("Description of some kind").setName("Some name").setOwner(ADMIN).setSequence(383L);
        element = dashboardToElement(dashboard);
        assertFalse(dashboard.save(element));
        assertEquals(dashboard, elementToDashboard(element));
    }

    public void testCotrElement() throws Exception
    {
        ConfigDashboard dashboard = new ConfigDashboard().setId(1010L);
        Element element = dashboardToElement(dashboard);
        assertEquals(dashboard, new ConfigDashboard(element));

        dashboard = new ConfigDashboard().setId(19393L).setFavouriteCount(10L).setLayout("AA").setVersion(6L)
                .setDescription("Description of some kind").setName("Some name").setOwner(ADMIN).setSequence(383L);

        element = dashboardToElement(dashboard);
        assertEquals(dashboard, new ConfigDashboard(element));
    }

    public void testIsSystem() throws Exception
    {
        ConfigDashboard dashboard = new ConfigDashboard();
        assertTrue(dashboard.isSystem());
        assertFalse(dashboard.setOwner("notsystem").isSystem());
    }

    public void testReorderGadgetsNoGadgets() throws Exception
    {
        ConfigDashboard dashboard = new ConfigDashboard();
        dashboard.reorderGadgets();
        assertTrue(dashboard.getGadgets().isEmpty());
    }

    public void testReorderGadgets() throws Exception
    {
        final List<ConfigGadget> gadgets = new ArrayList<ConfigGadget>();
        gadgets.add(new ConfigGadget().setId(9L).setColumnNumber(1).setRowNumber(2));
        gadgets.add(new ConfigGadget().setId(2L).setColumnNumber(null).setRowNumber(0));
        gadgets.add(new ConfigGadget().setId(12L).setColumnNumber(3457).setRowNumber(202020));
        gadgets.add(new ConfigGadget().setId(3L).setColumnNumber(null).setRowNumber(0));
        gadgets.add(new ConfigGadget().setId(4L).setColumnNumber(0).setRowNumber(0));
        gadgets.add(new ConfigGadget().setId(8L).setColumnNumber(1).setRowNumber(1));
        gadgets.add(new ConfigGadget().setId(5L).setColumnNumber(0).setRowNumber(1));
        gadgets.add(new ConfigGadget().setId(10L).setColumnNumber(3456).setRowNumber(2));
        gadgets.add(new ConfigGadget().setId(1L));
        gadgets.add(new ConfigGadget().setId(6L).setColumnNumber(0).setRowNumber(27272));
        gadgets.add(new ConfigGadget().setId(7L).setColumnNumber(1).setRowNumber(0));
        gadgets.add(new ConfigGadget().setId(11L).setColumnNumber(3456).setRowNumber(3));

        final List<ConfigGadget> expectedGadgets = new ArrayList<ConfigGadget>();
        expectedGadgets.add(new ConfigGadget().setId(9L).setColumnNumber(1).setRowNumber(2));
        expectedGadgets.add(new ConfigGadget().setId(2L).setColumnNumber(0).setRowNumber(0));
        expectedGadgets.add(new ConfigGadget().setId(12L).setColumnNumber(3).setRowNumber(0));
        expectedGadgets.add(new ConfigGadget().setId(3L).setColumnNumber(0).setRowNumber(1));
        expectedGadgets.add(new ConfigGadget().setId(4L).setColumnNumber(0).setRowNumber(2));
        expectedGadgets.add(new ConfigGadget().setId(8L).setColumnNumber(1).setRowNumber(1));
        expectedGadgets.add(new ConfigGadget().setId(5L).setColumnNumber(0).setRowNumber(3));
        expectedGadgets.add(new ConfigGadget().setId(10L).setColumnNumber(2).setRowNumber(0));
        expectedGadgets.add(new ConfigGadget().setId(1L).setColumnNumber(0).setRowNumber(5));
        expectedGadgets.add(new ConfigGadget().setId(6L).setColumnNumber(0).setRowNumber(4));
        expectedGadgets.add(new ConfigGadget().setId(7L).setColumnNumber(1).setRowNumber(0));
        expectedGadgets.add(new ConfigGadget().setId(11L).setColumnNumber(2).setRowNumber(1));

        final List<ConfigGadget> actualGadgets = new ConfigDashboard().setGadgets(gadgets).reorderGadgets().getGadgets();
        assertEquals(expectedGadgets, actualGadgets);
    }

    private ConfigDashboard elementToDashboard(final Element element)
    {
        final ConfigDashboard dashboard = new ConfigDashboard();
        dashboard.setId(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID));
        dashboard.setOwner(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_USERNAME));
        dashboard.setName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_PAGENAME));
        dashboard.setDescription(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_DESCRIPTION));
        dashboard.setSequence(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_SEQUENCE));
        dashboard.setLayout(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_LAYOUT));
        dashboard.setVersion(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_VERSION));
        dashboard.setFavouriteCount(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_FAV_COUNT));

        return dashboard;
    }

    private Element dashboardToElement(final ConfigDashboard dashboard)
    {
        final Element element = createElement("NoIReallyReallyReallyDontCare");
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, dashboard.getId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_USERNAME, dashboard.getOwner());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_PAGENAME, dashboard.getName());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_DESCRIPTION, dashboard.getDescription());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_SEQUENCE, dashboard.getSequence());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_LAYOUT, dashboard.getLayout());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_VERSION, dashboard.getVersion());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_FAV_COUNT, dashboard.getFavouriteCount());

        return element;
    }

    private static Element createElement(final String name)
    {
        return DocumentFactory.getInstance().createElement(name);
    }
}
