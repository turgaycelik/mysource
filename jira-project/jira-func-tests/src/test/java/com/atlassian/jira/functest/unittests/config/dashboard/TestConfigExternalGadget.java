package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.dashboard.ConfigExternalGadget;
import junit.framework.TestCase;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

/**
 * Simple test of {@link com.atlassian.jira.functest.unittests.config.dashboard.TestConfigExternalGadget}.
 *
 * @since v4.2
 */
public class TestConfigExternalGadget extends TestCase
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_GADGET_XML = "gadgetXml";

    public void testSave() throws Exception
    {
        ConfigExternalGadget gadget = new ConfigExternalGadget().setId(1010L);
        Element testElement = createElement("test");
        gadget.save(testElement);
        assertEquals(gadget, elementToGadget(testElement));

        gadget.setGadgetXml("dksdadskjas");

        testElement = createElement("test2");
        gadget.save(testElement);
        assertEquals(gadget, elementToGadget(testElement));
    }

    public void testCotrElement() throws Exception
    {
        ConfigExternalGadget gadget = new ConfigExternalGadget().setId(1010L);
        assertEquals(gadget, new ConfigExternalGadget(createElementForGadget(gadget)));

        gadget.setGadgetXml("dksdadskjas");
        assertEquals(gadget, new ConfigExternalGadget(createElementForGadget(gadget)));

        gadget.setGadgetXml(null);
        assertEquals(gadget, new ConfigExternalGadget(createElementForGadget(gadget)));
    }

    private static ConfigExternalGadget elementToGadget(final Element gadgetElement)
    {
        final ConfigExternalGadget gadget = new ConfigExternalGadget();

        gadget.setId(ConfigXmlUtils.getLongValue(gadgetElement, ATTRIBUTE_ID));
        gadget.setGadgetXml(ConfigXmlUtils.getTextValue(gadgetElement, ATTRIBUTE_GADGET_XML));

        return gadget;
    }

    private static Element createElementForGadget(ConfigExternalGadget gadget)
    {
        final Element element = createElement("something");

        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, gadget.getId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_GADGET_XML, gadget.getGadgetXml());

        return element;
    }

    private static Element createElement(final String name)
    {
        return DocumentFactory.getInstance().createElement(name);
    }
}
