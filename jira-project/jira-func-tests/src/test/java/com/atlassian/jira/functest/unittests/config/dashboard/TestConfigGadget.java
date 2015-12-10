package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.dashboard.ConfigGadget;
import junit.framework.TestCase;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * Test for {@link com.atlassian.jira.functest.config.dashboard.ConfigGadget}.
 *
 * @since v4.2
 */
public class TestConfigGadget extends TestCase
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_PORTALPAGE = "portalpage";
    private static final String ATTRIBUTE_COLUMN_NUMBER = "columnNumber";
    private static final String ATTRIBUTE_POSITION = "position";
    private static final String ATTRIBUTE_GADGET_XML = "gadgetXml";
    private static final String ATTRIBUTE_PORTLET_ID = "portletId";

    public void testSave() throws Exception
    {
        ConfigGadget gadget = new ConfigGadget().setId(1010L);
        Element testElement = createElement("test");
        gadget.save(testElement);
        assertEquals(gadget, elementToGadget(testElement));

        gadget.setGadgetXml("dksdadskjas").setPortletId("3u4928340923").setDashboard(1901991L)
                .setColumnNumber(1).setRowNumber(46);

        testElement = createElement("test2");
        gadget.save(testElement);
        assertEquals(gadget, elementToGadget(testElement));
    }

    public void testCotrElement() throws Exception
    {
        ConfigGadget gadget = new ConfigGadget().setId(1010L);
        assertEquals(gadget, new ConfigGadget(createElementForGadget(gadget)));

        gadget.setGadgetXml("dksdadskjas").setPortletId("3u4928340923").setDashboard(1901991L)
                .setColumnNumber(1).setRowNumber(46);

        assertEquals(gadget, new ConfigGadget(createElementForGadget(gadget)));

        gadget.setGadgetXml(null);
        assertEquals(gadget, new ConfigGadget(createElementForGadget(gadget)));
    }

    private static ConfigGadget elementToGadget(final Element gadgetElement)
    {
        final ConfigGadget gadget = new ConfigGadget();

        gadget.setId(ConfigXmlUtils.getLongValue(gadgetElement, ATTRIBUTE_ID));
        gadget.setDashboard(ConfigXmlUtils.getLongValue(gadgetElement, ATTRIBUTE_PORTALPAGE));
        gadget.setColumnNumber(ConfigXmlUtils.getIntegerValue(gadgetElement, ATTRIBUTE_COLUMN_NUMBER));
        gadget.setRowNumber(ConfigXmlUtils.getIntegerValue(gadgetElement, ATTRIBUTE_POSITION));
        gadget.setGadgetXml(ConfigXmlUtils.getTextValue(gadgetElement, ATTRIBUTE_GADGET_XML));
        gadget.setPortletId(ConfigXmlUtils.getTextValue(gadgetElement, ATTRIBUTE_PORTLET_ID));

        return gadget;
    }

    private static Element createElementForGadget(ConfigGadget gadget)
    {
        final Element element = createElement("something");

        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, gadget.getId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_PORTALPAGE, gadget.getDashboardId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_COLUMN_NUMBER, gadget.getColumnNumber());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_POSITION, gadget.getRowNumber());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_GADGET_XML, gadget.getGadgetXml());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_PORTLET_ID, gadget.getPortletId());

        return element;
    }

    private static Element createElement(final String name)
    {
        return DocumentFactory.getInstance().createElement(name);
    }
}
