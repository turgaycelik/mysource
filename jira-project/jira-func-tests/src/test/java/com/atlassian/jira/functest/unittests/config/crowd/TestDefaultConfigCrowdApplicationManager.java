package com.atlassian.jira.functest.unittests.config.crowd;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.crowd.ConfigCrowdApplication;
import com.atlassian.jira.functest.config.crowd.DefaultConfigCrowdApplicationManager;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since v4.3
 */
public class TestDefaultConfigCrowdApplicationManager
{
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_LOWER_NAME = "lowerName";
    private static final String ATTRIBUTE_ACTIVE = "active";
    private static final String ATTRIBUTE_APPLICATION_TYPE = "applicationType";
    private static final String ELEMENT_APPLICATION = "Application";

    @Test(expected = ConfigException.class)
    public void testTestLoadNoId() throws Exception
    {
        Document document = toDocument(new ConfigCrowdApplication());
        DefaultConfigCrowdApplicationManager manager = new DefaultConfigCrowdApplicationManager(document, null);
        manager.loadApplications();
    }

    @Test
    public void testTestLoad() throws Exception
    {
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(10L);
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(11L).setActive(false)
                .setApplicationType("CROWD").setName("jack").setLowerName("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(12L).setActive(true);

        Document document = toDocument(application1, application2, application3);
        DefaultConfigCrowdApplicationManager manager = new DefaultConfigCrowdApplicationManager(document, null);
        Assert.assertEquals(Arrays.asList(application1, application2, application3), manager.loadApplications());
    }

    @Test
    public void testUpdateNoChanges() throws Exception
    {
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(10L);
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(11L).setActive(false)
                .setApplicationType("CROWD").setName("jack").setLowerName("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(12L).setActive(true);

        Document document = toDocument(application1, application2, application3);
        DefaultConfigCrowdApplicationManager manager = new DefaultConfigCrowdApplicationManager(document, null);
        List<ConfigCrowdApplication> expectedApps = asList(application1, application2, application3);
        Assert.assertFalse(manager.saveApplications(expectedApps));
        Assert.assertEquals(expectedApps, parseDocument(document));
    }

    @Test
    public void testUpdateChanges() throws Exception
    {
        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(10L).setName("Name").setLowerName("name")
                .setApplicationType("type");
        ConfigCrowdApplication application2 = new ConfigCrowdApplication().setId(11L).setActive(false)
                .setApplicationType("CROWD").setName("jack").setLowerName("jill");
        ConfigCrowdApplication application3 = new ConfigCrowdApplication().setId(12L).setActive(true);

        Document document = toDocument(application1, application2, application3);
        DefaultConfigCrowdApplicationManager manager = new DefaultConfigCrowdApplicationManager(document, null);

        ConfigCrowdApplication newApplication1 = new ConfigCrowdApplication().setId(10L);
        ConfigCrowdApplication newApplication2 = new ConfigCrowdApplication().setId(11L).setActive(true).setName("NewName")
                .setLowerName("name").setApplicationType("type");

        List<ConfigCrowdApplication> expectedList = asList(newApplication1, newApplication2);
        Assert.assertTrue(manager.saveApplications(expectedList));
        Assert.assertEquals(expectedList, parseDocument(document));
    }

    @Test
    public void testUpdateCreateFail() throws Exception
    {
        IMocksControl control = EasyMock.createControl();

        ConfigSequence configSequence = control.createMock(ConfigSequence.class);
        EasyMock.expect(configSequence.getNextId(ELEMENT_APPLICATION)).andReturn(2056L);

        ConfigCrowdApplication application1 = new ConfigCrowdApplication().setId(10L).setName("Name").setLowerName("name")
                .setApplicationType("type");

        Document document = toDocument(application1);
        DefaultConfigCrowdApplicationManager manager = new DefaultConfigCrowdApplicationManager(document, configSequence);

        ConfigCrowdApplication newApplication2 = new ConfigCrowdApplication().setActive(true).setName("NewName")
                .setLowerName("name").setApplicationType("type");

        control.replay();

        List<ConfigCrowdApplication> expectedList = asList(application1, newApplication2.setId(2056L));
        Assert.assertTrue(manager.saveApplications(expectedList));
        Assert.assertEquals(expectedList, parseDocument(document));

        control.verify();
    }

    private Document toDocument(ConfigCrowdApplication... applications)
    {
        Element element = createRootElement();
        for (ConfigCrowdApplication application : applications)
        {
            toElement(element, application);
        }
        return element.getDocument();
    }

    private List<ConfigCrowdApplication> parseDocument(Document document)
    {
        List<Element> elements = ConfigXmlUtils.getTopElementsByName(document, ELEMENT_APPLICATION);
        List<ConfigCrowdApplication> applications = new ArrayList<ConfigCrowdApplication>();

        for (Element element : elements)
        {
            applications.add(fromElement(element));
        }

        return applications;
    }

    private static Element createRootElement()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement("entity-engine-xml");
    }

    private Element toElement(Element root, ConfigCrowdApplication application)
    {
        Element element = root.addElement(ELEMENT_APPLICATION);
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, application.getId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_NAME, application.getName());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_LOWER_NAME, application.getLowerName());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ACTIVE, application.isActive() == null ? null : application.isActive() ? "1" : "0");
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_APPLICATION_TYPE, application.getApplicationType());

        return element;
    }

    private ConfigCrowdApplication fromElement(Element elements)
    {
        ConfigCrowdApplication application = new ConfigCrowdApplication();
        application.setId(ConfigXmlUtils.getLongValue(elements, ATTRIBUTE_ID));
        application.setName(ConfigXmlUtils.getTextValue(elements, ATTRIBUTE_NAME));
        application.setLowerName(ConfigXmlUtils.getTextValue(elements, ATTRIBUTE_LOWER_NAME));
        application.setApplicationType(ConfigXmlUtils.getTextValue(elements, ATTRIBUTE_APPLICATION_TYPE));

        Integer active = ConfigXmlUtils.getIntegerValue(elements, ATTRIBUTE_ACTIVE);
        application.setActive(active == null ? null : active == 1);


        return application;
    }

    private static <T, S extends T> List<T> asList(S...list)
    {
        return new ArrayList<T>(Arrays.asList(list));
    }
}
