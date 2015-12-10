package com.atlassian.jira.functest.unittests.config.service;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetManager;
import com.atlassian.jira.functest.config.service.ConfigService;
import com.atlassian.jira.functest.config.service.DefaultConfigServiceManager;
import junit.framework.TestCase;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.IMocksControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

/**
 * Test class for {@link DefaultConfigServiceManager}.
 *
 * @since v4.1
 */
public class TestDefaultConfigServiceManager extends TestCase
{
    private static final String ELEMENT_SERVICE_CONFIG = "ServiceConfig";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TIME = "time";
    private static final String ATTRIBUTE_CLAZZ = "clazz";
    private static final String ATTRIBUTE_NAME = "name";

    private ConfigService service1;
    private ConfigService service2;
    private ConfigService service3;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        service1 = new ConfigService().setId(100L).setName("qwerty").setClazz("blarg").setTimeout(738L);
        ConfigPropertySet cps = new ConfigPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId());
        cps.setStringProperty("one", "one");
        service1.setPropertySet(cps);

        service2 = new ConfigService().setId(1001L).setName("asdfgh").setClazz("aajaja").setTimeout(3773L);
        cps = new ConfigPropertySet(ELEMENT_SERVICE_CONFIG, service2.getId());
        cps.setStringProperty("two", "two");
        service2.setPropertySet(cps);

        service3 = new ConfigService().setId(2266272L).setName("zxcvbn").setClazz("jjksks").setTimeout(383838L);
        cps = new ConfigPropertySet(ELEMENT_SERVICE_CONFIG, service3.getId());
        cps.setStringProperty("three", "three");
        service3.setPropertySet(cps);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        service1 = service2 = service3 = null;
    }

    public void testReadServices() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(new ConfigPropertySet(service1.getPropertySet()));
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service2.getId())).andReturn(new ConfigPropertySet(service2.getPropertySet()));
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service3.getId())).andReturn(new ConfigPropertySet(service3.getPropertySet()));

        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1, service2, service3).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        final List<ConfigService> actualList = manager.loadServices();

        assertEqualsCollections(actualList, Arrays.asList(service1, service2, service3));

        control.verify();
    }

    public void testReadServicesNoId() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1.setId(null)).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);

        try
        {
            final List<ConfigService> actualList = manager.loadServices();
            fail("Expected exception but got list: " + actualList);
        }
        catch (ConfigException e)
        {
            //expected.
        }

        control.verify();
    }

    public void testSaveServicesNew() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(new ConfigPropertySet(service1.getPropertySet()));

        expect(propertySetManager.savePropertySet(service2.getPropertySet())).andReturn(true);
        expect(sequence.getNextId(ELEMENT_SERVICE_CONFIG)).andReturn(service2.getId());

        expect(propertySetManager.savePropertySet(service3.getPropertySet())).andReturn(false);
        expect(sequence.getNextId(ELEMENT_SERVICE_CONFIG)).andReturn(service3.getId());

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        assertTrue(manager.saveServices(Arrays.asList(service1, service2, service3)));

        final DocumentAsserter documentParser = new DocumentAsserter(document);
        documentParser.assertServices(Arrays.asList(service1, service2, service3));

        control.verify();
    }

    public void testSaveServicesDelete() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(new ConfigPropertySet(service1.getPropertySet()));
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service2.getId())).andReturn(new ConfigPropertySet(service2.getPropertySet()));
        propertySetManager.deletePropertySet(service2.getPropertySet());

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1, service2).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        assertTrue(manager.saveServices(Arrays.asList(service1)));

        final DocumentAsserter documentParser = new DocumentAsserter(document);
        documentParser.assertServices(Arrays.asList(service1));

        control.verify();
    }

    public void testSaveServicesDeleteAll() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(new ConfigPropertySet(service1.getPropertySet()));
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service2.getId())).andReturn(new ConfigPropertySet(service2.getPropertySet()));
        propertySetManager.deletePropertySet(service1.getPropertySet());
        propertySetManager.deletePropertySet(service2.getPropertySet());

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1, service2).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        assertTrue(manager.saveServices(Collections.<ConfigService>emptyList()));

        final DocumentAsserter documentParser = new DocumentAsserter(document);
        documentParser.assertServices(Collections.<ConfigService>emptyList());

        control.verify();
    }

    public void testSaveServicesDeleteNoId() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1.setId(null), service2).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        try
        {
            manager.saveServices(Collections.<ConfigService>emptyList());
            fail("Error expected.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        control.verify();
    }

    public void testUpdateService() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service3.getId())).andReturn(service3.getPropertySet());
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(service1.getPropertySet());
        expect(propertySetManager.savePropertySet(service3.getPropertySet())).andReturn(false);
        expect(propertySetManager.savePropertySet(service1.getPropertySet())).andReturn(false);

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service3, service1).createDocument();

        control.replay();

        service3.setName("newName");
        service3.setTimeout(190387483L);
        service3.setClazz("this.Is.A.Very.Random.Clazzz");

        service1.setName(null);
        service1.setTimeout(null);
        service1.setClazz(null);

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        final List<ConfigService> expectedServices = Arrays.asList(service3, service1);
        assertTrue(manager.saveServices(expectedServices));

        final DocumentAsserter documentParser = new DocumentAsserter(document);
        documentParser.assertServices(expectedServices);

        control.verify();
    }

    public void testUpdateServicePropertySetChange() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        final ConfigPropertySet cps = service2.getPropertySet().copyForEntity("bad", 12L);
        cps.setLongProperty("New", 67L);

        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(new ConfigPropertySet(service1.getPropertySet()));
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service2.getId())).andReturn(new ConfigPropertySet(service2.getPropertySet()));
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service3.getId())).andReturn(null);

        propertySetManager.deletePropertySet(service1.getPropertySet());
        expect(propertySetManager.savePropertySet(cps.copyForEntity(ELEMENT_SERVICE_CONFIG, service2.getId()))).andReturn(true);

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1, service2, service3).createDocument();

        control.replay();

        //this should cause a delete of the propertyset.
        service1.setPropertySet(null);

        //this should update the property set.
        service2.setPropertySet(cps);

        //this should do nothing to the property set.
        service3.setPropertySet(null).setName("nsnskajksfjalkdjfldksjfdlks");

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        final List<ConfigService> expectedServices = Arrays.asList(service1, service2, service3);
        assertTrue(manager.saveServices(expectedServices));

        final DocumentAsserter documentParser = new DocumentAsserter(document);
        documentParser.assertServices(expectedServices);

        control.verify();
    }

    public void testUpdateServicesNoId() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service1.setId(null), service2).createDocument();

        control.replay();

        service1.setName("somethineNew");

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        try
        {
            manager.saveServices(Arrays.asList(service1, service2));
            fail("Error expected.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        control.verify();
    }


    public void testSaveNoChanges() throws Exception
    {
        final IMocksControl control = createControl();
        final ConfigPropertySetManager propertySetManager = control.createMock(ConfigPropertySetManager.class);
        final ConfigSequence sequence = control.createMock(ConfigSequence.class);

        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service3.getId())).andReturn(service3.getPropertySet());
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service1.getId())).andReturn(service1.getPropertySet());
        expect(propertySetManager.loadPropertySet(ELEMENT_SERVICE_CONFIG, service2.getId())).andReturn(service2.getPropertySet());

        final DocumentBuilder helper = new DocumentBuilder();
        final Document document = helper.addServices(service3, service1, service2).createDocument();

        control.replay();

        final DefaultConfigServiceManager manager = new DefaultConfigServiceManager(document, propertySetManager, sequence);
        final List<ConfigService> expectedServices = Arrays.asList(service3, service1, service2);
        assertFalse(manager.saveServices(expectedServices));

        final DocumentAsserter documentParser = new DocumentAsserter(document);
        documentParser.assertServices(expectedServices);

        control.verify();
    }

    private void assertEqualsCollections(Collection<?> one, Collection<?> two)
    {
        assertEquals(String.format("size(%s) != size(%s)", one, two), one.size(), two.size());
        assertTrue(String.format("%s != %s", one, two), one.containsAll(two));
    }

    private static class DocumentAsserter
    {
        private final Document doc;

        public DocumentAsserter(final Document doc)
        {
            this.doc = doc;
        }

        private void assertServices(List<ConfigService> expectedServices)
        {
            List<ConfigService> actualServices = readServices();
            assertEquals(String.format("size(%s) != size(%s)", expectedServices, actualServices),
                    expectedServices.size(), actualServices.size());

            final Iterator<ConfigService> actualIter =  actualServices.iterator();
            for (final ConfigService expectedService : expectedServices)
            {
                final ConfigService actualService = actualIter.next();
                if (!checkServiceEquals(expectedService, actualService))
                {
                    fail(String.format("Expected(%s) != Actual(%s)", expectedService, actualService));
                }
            }
        }

        private List<ConfigService> readServices()
        {
            @SuppressWarnings ({ "unchecked" })
            final List<Element> elements = doc.getRootElement().elements(ELEMENT_SERVICE_CONFIG);
            final List<ConfigService> services = new ArrayList<ConfigService>(elements.size());
            for (Element element : elements)
            {
                services.add(readService(element));
            }
            return services;
        }

        private ConfigService readService(final Element element)
        {
            final ConfigService service = new ConfigService();
            service.setId(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID));
            service.setName(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_NAME));
            service.setClazz(ConfigXmlUtils.getTextValue(element, ATTRIBUTE_CLAZZ));
            service.setTimeout(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_TIME));

            return service;
        }

        private boolean checkServiceEquals(ConfigService expected, ConfigService actual)
        {
            return new EqualsBuilder().append(expected.getId(), actual.getId()).append(expected.getTimeout(), actual.getTimeout())
                    .append(actual.getClazz(), expected.getClazz()).append(actual.getName(), expected.getName()).isEquals();
        }
    }

    private static class DocumentBuilder
    {
        private Element root;

        private DocumentBuilder()
        {
            this(createRoot());
        }

        private DocumentBuilder(Document document)
        {
            this.root = document.getRootElement();
        }

        private Document createDocument()
        {
            return createDocumentWithRoot(root.createCopy());
        }

        private DocumentBuilder addServices(ConfigService... services)
        {
            for (ConfigService service : services)
            {
                addService(service);
            }
            return this;
        }

        private DocumentBuilder addService(ConfigService service)
        {
            final Element element = root.addElement(ELEMENT_SERVICE_CONFIG);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, asString(service.getId()));
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_NAME, service.getName());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_CLAZZ, service.getClazz());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_TIME, asString(service.getTimeout()));

            return this;
        }

        private static Document createRoot()
        {
            final DocumentFactory factory = DocumentFactory.getInstance();
            final Document document = factory.createDocument();
            document.addElement("entity-engine-xml");

            return document;
        }

        private static Document createDocumentWithRoot(Element root)
        {
            final DocumentFactory factory = DocumentFactory.getInstance();
            final Document document = factory.createDocument();
            document.add(root);

            return document;
        }

        private static String asString(Object obj)
        {
            return obj == null ? null : obj.toString();
        }
    }
}
