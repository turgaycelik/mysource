package com.atlassian.jira.functest.unittests.config.dashboard;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.dashboard.ConfigExternalGadget;
import com.atlassian.jira.functest.config.dashboard.ConfigGadget;
import com.atlassian.jira.functest.config.dashboard.ConfigGadgetManager;
import com.atlassian.jira.functest.config.dashboard.ConfigGadgetSupport;
import com.atlassian.jira.functest.config.dashboard.DefaultConfigGadgetManager;
import com.atlassian.jira.util.collect.MapBuilder;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link com.atlassian.jira.functest.config.dashboard.DefaultConfigGadgetManager}.
 *
 * @since v4.2
 */
public class TestDefaultConfigGadgetManager extends TestCase
{
    private static final String ELEMENT_GADGET = "PortletConfiguration";
    private static final String ELEMENT_GADGET_PREF = "GadgetUserPreference";
    private static final String ELEMENT_EXTERNAL_GADGET = "ExternalGadget";

    private static final String ATTRIB_PORTLETCONFIG = "portletconfiguration";
    private static final String ATTRIB_PREF_KEY = "userprefkey";
    private static final String ATTRIB_PREF_VALUE = "userprefvalue";

    public void testLoadGadgetsDelegates() throws Exception
    {
        List<ConfigGadget> expectedGadgets = Arrays.asList(new ConfigGadget().setId(109L), new ConfigGadget().setId(1919L));

        IMocksControl iMocksControl = EasyMock.createControl();
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigGadget> configGadgetSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigExternalGadget> externalSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        EasyMock.expect(configGadgetSupport.loadAll()).andReturn(expectedGadgets);

        iMocksControl.replay();

        ConfigGadgetManager mgr = new DefaultConfigGadgetManager(configGadgetSupport, externalSupport);
        assertEquals(expectedGadgets, mgr.loadGadgets());

        iMocksControl.verify();
    }

    public void testSaveGadgetsDelegates() throws Exception
    {
        List<ConfigGadget> expectedGadgets = Arrays.asList(new ConfigGadget().setId(109L), new ConfigGadget().setId(1919L));

        IMocksControl iMocksControl = EasyMock.createControl();
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigGadget> configGadgetSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigExternalGadget> externalSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        EasyMock.expect(configGadgetSupport.sync(expectedGadgets)).andReturn(true).andReturn(false);

        iMocksControl.replay();

        ConfigGadgetManager mgr = new DefaultConfigGadgetManager(configGadgetSupport, externalSupport);
        assertTrue(mgr.saveGadgets(expectedGadgets));
        assertFalse(mgr.saveGadgets(expectedGadgets));

        iMocksControl.verify();
    }

    public void testLoadExternalGadgetsDelegates() throws Exception
    {
        List<ConfigExternalGadget> expectedGadgets = Arrays.asList(new ConfigExternalGadget().setId(109L));

        IMocksControl iMocksControl = EasyMock.createControl();
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigGadget> configGadgetSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigExternalGadget> externalSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        EasyMock.expect(externalSupport.loadAll()).andReturn(expectedGadgets);

        iMocksControl.replay();

        ConfigGadgetManager mgr = new DefaultConfigGadgetManager(configGadgetSupport, externalSupport);
        assertEquals(expectedGadgets, mgr.loadExternalGadgets());

        iMocksControl.verify();
    }

    public void testSaveExternalGadgetsDelegates() throws Exception
    {
        List<ConfigExternalGadget> expectedGadgets = Arrays.asList(new ConfigExternalGadget().setId(109L));

        IMocksControl iMocksControl = EasyMock.createControl();
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigGadget> configGadgetSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        @SuppressWarnings ({ "unchecked" })
        ConfigGadgetSupport<ConfigExternalGadget> externalSupport = iMocksControl.createMock(ConfigGadgetSupport.class);
        EasyMock.expect(externalSupport.sync(expectedGadgets)).andReturn(true).andReturn(false);

        iMocksControl.replay();

        ConfigGadgetManager mgr = new DefaultConfigGadgetManager(configGadgetSupport, externalSupport);
        assertTrue(mgr.saveExternalGadgets(expectedGadgets));
        assertFalse(mgr.saveExternalGadgets(expectedGadgets));

        iMocksControl.verify();
    }

    public void testHelperLoadGadgetsNoId() throws Exception
    {
        ConfigGadget one = new ConfigGadget().setGadgetXml("localhost:8080").setColumnNumber(2).setRowNumber(1);
        ConfigGadget two = new ConfigGadget().setId(329398L).setPortletId("100101").setColumnNumber(1).setRowNumber(1);

        DefaultConfigGadgetManager.GadgetHelper manager = new DefaultConfigGadgetManager.GadgetHelper(createDocument(one, two));
        try
        {
            manager.loadAll();
            fail("Expected to fail because a gadget did not have an id.");
        }
        catch (ConfigException expected)
        {
        }
    }

    public void testHelperLoadGadgets() throws Exception
    {
        ConfigGadget one = new ConfigGadget().setId(2992L).setGadgetXml("localhost:8080").setColumnNumber(2).setRowNumber(1);
        ConfigGadget two = new ConfigGadget().setId(329398L).setPortletId("100101").setColumnNumber(1).setRowNumber(1);

        List<ConfigGadget> expected = Arrays.asList(one, two);

        DefaultConfigGadgetManager.GadgetHelper manager = new DefaultConfigGadgetManager.GadgetHelper(createDocument(one, two));
        assertEquals(expected, manager.loadAll());
    }

    public void testHelperNewGadgetFails() throws Exception
    {
        ConfigGadget one = new ConfigGadget().setId(2992L).setGadgetXml("localhost:8080").setColumnNumber(2).setRowNumber(1);
        ConfigGadget two = new ConfigGadget().setId(299L);
        Document document = createDocument(one);
        DefaultConfigGadgetManager.GadgetHelper manager = new DefaultConfigGadgetManager.GadgetHelper(document);
        try
        {
            manager.create(two);
            fail("Expected a configuration exception when adding a gadget.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        assertDocumentContent(document, Arrays.asList(one));
    }

    public void testHelperGadgetUpdate() throws Exception
    {
        final Map<String, String> config = MapBuilder.<String, String>newBuilder().add("a", "b").toMutableMap();

        final ConfigGadget one = new ConfigGadget().setId(2992L).setGadgetXml("localhost:8080").setColumnNumber(2).setRowNumber(1);
        final ConfigGadget two = new ConfigGadget().setId(329398L).setPortletId("100101").setColumnNumber(1).setRowNumber(1);
        final Document document = createDocument(one, two);
        addUserPrefs(document, one.getId(), config);

        final ConfigGadget newOne = new ConfigGadget(one).setColumnNumber(1).setGadgetXml("http://extranet.atlassian.com");

        final List<ConfigGadget> expectedGadgets = Arrays.asList(newOne, two);
        final DefaultConfigGadgetManager.GadgetHelper manager = new DefaultConfigGadgetManager.GadgetHelper(document);

        //Change should be reported.
        assertTrue(manager.update(one, newOne));
        assertDocumentContent(document, expectedGadgets);
        assertEquals(config, getUserPrefs(document, one.getId()));
        assertEquals(Collections.<String, String>emptyMap(), getUserPrefs(document, two.getId()));

        //Make sure that no changes are reported.
        assertFalse(manager.update(two, new ConfigGadget(two)));
        assertDocumentContent(document, expectedGadgets);
        assertEquals(config, getUserPrefs(document, one.getId()));
        assertEquals(Collections.<String, String>emptyMap(), getUserPrefs(document, two.getId()));
    }

    public void testHelperDeleteWorks() throws Exception
    {
        final Map<String, String> config = MapBuilder.<String, String>newBuilder().add("a", "b").toMutableMap();

        final ConfigGadget one = new ConfigGadget().setId(2992L).setGadgetXml("localhost:8080").setColumnNumber(2).setRowNumber(1);
        final ConfigGadget two = new ConfigGadget().setId(329398L).setPortletId("100101").setColumnNumber(1).setRowNumber(1);
        final Document document = createDocument(one, two);
        addUserPrefs(document, one.getId(), config);

        final List<ConfigGadget> expectedGadgets = Arrays.asList(two);
        final DefaultConfigGadgetManager.GadgetHelper manager = new DefaultConfigGadgetManager.GadgetHelper(document);

        //Change should be reported.
        assertTrue(manager.delete(one));
        assertDocumentContent(document, expectedGadgets);
        assertEquals(Collections.<String, String>emptyMap(), getUserPrefs(document, one.getId()));
        assertEquals(Collections.<String, String>emptyMap(), getUserPrefs(document, two.getId()));
    }

    public void testHelperLoadExternalGadgetsNoId() throws Exception
    {
        ConfigExternalGadget one = new ConfigExternalGadget().setGadgetXml("localhost:8080");
        ConfigExternalGadget two = new ConfigExternalGadget().setId(329398L);

        DefaultConfigGadgetManager.ExternalHelper manager = new DefaultConfigGadgetManager.ExternalHelper(createDocument(one, two));
        try
        {
            manager.loadAll();
            fail("Expected to fail because a gadget did not have an id.");
        }
        catch (ConfigException expected)
        {
        }
    }

    public void testHelperLoadExternalGadgets() throws Exception
    {
        ConfigExternalGadget one = new ConfigExternalGadget().setId(2992L).setGadgetXml("localhost:8080");
        ConfigExternalGadget two = new ConfigExternalGadget().setId(329398L);

        List<ConfigExternalGadget> expected = Arrays.asList(one, two);

        DefaultConfigGadgetManager.ExternalHelper manager = new DefaultConfigGadgetManager.ExternalHelper(createDocument(one, two));
        assertEquals(expected, manager.loadAll());
    }

    public void testHelperNewExternalGadgetFails() throws Exception
    {
        ConfigExternalGadget one = new ConfigExternalGadget().setId(2992L).setGadgetXml("localhost:8080");
        Document document = createDocument(one);
        DefaultConfigGadgetManager.ExternalHelper manager = new DefaultConfigGadgetManager.ExternalHelper(document);
        try
        {
            manager.create(one);
            fail("Expected a configuration exception when adding a gadget.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        assertExternalDocumentContent(document, Arrays.asList(one));
    }

    public void testHelperUpdateExternal() throws Exception
    {
        final ConfigExternalGadget one = new ConfigExternalGadget().setId(2992L).setGadgetXml("localhost:8080");
        final ConfigExternalGadget two = new ConfigExternalGadget().setId(329398L);
        final Document document = createDocument(one, two);

        final ConfigExternalGadget newOne = new ConfigExternalGadget(one).setGadgetXml("http://extranet.atlassian.com");

        final List<ConfigExternalGadget> expectedGadgets = Arrays.asList(newOne, two);
        final DefaultConfigGadgetManager.ExternalHelper manager = new DefaultConfigGadgetManager.ExternalHelper(document);

        //Change should be reported.
        assertTrue(manager.update(one, newOne));
        assertExternalDocumentContent(document, expectedGadgets);

        //Make sure that no changes are reported.
        assertFalse(manager.update(two, two));
        assertExternalDocumentContent(document, expectedGadgets);
    }

    public void testHelperDeleteExternal() throws Exception
    {
        final ConfigExternalGadget one = new ConfigExternalGadget().setId(2992L).setGadgetXml("localhost:8080");
        final ConfigExternalGadget two = new ConfigExternalGadget().setId(329398L).setGadgetXml("https://www.abc.net.au");
        final Document document = createDocument(one, two);

        final List<ConfigExternalGadget> expectedGadgets = Arrays.asList(two);
        final DefaultConfigGadgetManager.ExternalHelper manager = new DefaultConfigGadgetManager.ExternalHelper(document);

        //Change should be reported.
        assertTrue(manager.delete(one));
        assertExternalDocumentContent(document, expectedGadgets);
    }

    private void assertExternalDocumentContent(final Document actualDocument, final List<ConfigExternalGadget> expectedList)
    {
        assertEquals(expectedList, getExternalGadgets(actualDocument));
    }

    private static Collection<ConfigExternalGadget> getExternalGadgets(final Document actualDocument)
    {
        List<ConfigExternalGadget> servers = new ArrayList<ConfigExternalGadget>();

        @SuppressWarnings ({ "unchecked" })
        final Collection<Element> elements = actualDocument.getRootElement().elements(ELEMENT_EXTERNAL_GADGET);
        for (Element mailElem : elements)
        {
            servers.add(new ConfigExternalGadget(mailElem));
        }

        return servers;
    }

    private static Element addGadgetToElement(ConfigExternalGadget gadget, Element root)
    {
        final Element newMailElement = ConfigXmlUtils.createNewElement(root, ELEMENT_EXTERNAL_GADGET);
        gadget.save(newMailElement);
        return newMailElement;
    }

    private static Document createDocument(ConfigExternalGadget... gadgets)
    {
        Element element = createRootElement();
        for (ConfigExternalGadget gadget : gadgets)
        {
            addGadgetToElement(gadget, element);
        }
        return element.getDocument();
    }

    private void assertDocumentContent(final Document actualDocument, final List<ConfigGadget> expectedList)
    {
        assertEquals(expectedList, getGadgets(actualDocument));
    }

    private static Collection<ConfigGadget> getGadgets(final Document actualDocument)
    {
        List<ConfigGadget> servers = new ArrayList<ConfigGadget>();

        @SuppressWarnings ({ "unchecked" })
        final Collection<Element> elements = actualDocument.getRootElement().elements(ELEMENT_GADGET);
        for (Element mailElem : elements)
        {
            servers.add(new ConfigGadget(mailElem));
        }

        return servers;
    }

    private static Element addGadgetToElement(ConfigGadget gadget, Element root)
    {
        final Element newMailElement = ConfigXmlUtils.createNewElement(root, ELEMENT_GADGET);
        gadget.save(newMailElement);
        return newMailElement;
    }

    private static void addUserPrefs(final Document document, final long gadgetId, final Map<String, String> values)
    {
        //<GadgetUserPreference id="10000" portletconfiguration="10000" userprefkey="isConfigured" userprefvalue="true"/>
        final String idString = String.valueOf(gadgetId);
        for (Map.Entry<String, String> entry : values.entrySet())
        {
            final Element element = document.getRootElement().addElement(ELEMENT_GADGET_PREF);
            element.addAttribute(ATTRIB_PORTLETCONFIG, idString);
            element.addAttribute(ATTRIB_PREF_KEY, entry.getKey());
            element.addAttribute(ATTRIB_PREF_VALUE, entry.getValue());
        }
    }

    private static Map<String, String> getUserPrefs(final Document document, final long gadgetId)
    {
        //<GadgetUserPreference id="10000" portletconfiguration="10000" userprefkey="isConfigured" userprefvalue="true"/>
        final Map<String, String> values = new HashMap<String, String>();
        final List<Element> list = ConfigXmlUtils.getElementsByXpath(document, 
                String.format("/entity-engine-xml/GadgetUserPreference[@portletconfiguration='%d']", gadgetId));
        for (Element element : list)
        {
            final String key = element.attributeValue(ATTRIB_PREF_KEY);
            final String value = element.attributeValue(ATTRIB_PREF_VALUE);
            values.put(key, value);
        }
        return values;
    }

    private static Document createDocument(ConfigGadget... gadgets)
    {
        Element element = createRootElement();
        for (ConfigGadget gadget : gadgets)
        {
            addGadgetToElement(gadget, element);
        }
        return element.getDocument();
    }

    private static Element createRootElement()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement("entity-engine-xml");
    }
}