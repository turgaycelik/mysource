package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigAdminLocator;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.crowd.ConfigCrowdApplication;
import com.atlassian.jira.functest.config.crowd.ConfigCrowdApplicationManager;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboard;
import com.atlassian.jira.functest.config.dashboard.ConfigDashboardManager;
import com.atlassian.jira.functest.config.dashboard.ConfigExternalGadget;
import com.atlassian.jira.functest.config.dashboard.ConfigGadgetManager;
import com.atlassian.jira.functest.config.mail.ConfigMailServer;
import com.atlassian.jira.functest.config.mail.ConfigMailServerManager;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetManager;
import com.atlassian.jira.functest.config.service.ConfigService;
import com.atlassian.jira.functest.config.service.ConfigServiceManager;
import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.IMocksControl;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;

/**
 * Test for {@link com.atlassian.jira.functest.config.JiraConfig}.
 *
 * @since v4.1
 */
public class TestJiraConfig extends TestCase
{
    private static final String ROOT_ELEMENT = "entity-engine-xml";
    private static final String PROP_NAME = "jira.properties";
    private static final long PROP_ID = 1L;

    private ConfigMailServerManager mailServerManager;
    private ConfigPropertySetManager propertySetManager;
    private ConfigServiceManager serviceManager;
    private ConfigSequence configSequence;
    private ConfigDashboardManager dashboardManager;
    private ConfigGadgetManager gadgetManager;
    private ConfigAdminLocator adminLocator;
    private ConfigCrowdApplicationManager applicationManager;
    private IMocksControl control;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        control = createControl();
        mailServerManager = control.createMock(ConfigMailServerManager.class);
        propertySetManager = control.createMock(ConfigPropertySetManager.class);
        serviceManager = control.createMock(ConfigServiceManager.class);
        configSequence = control.createMock(ConfigSequence.class);
        dashboardManager = control.createMock(ConfigDashboardManager.class);
        gadgetManager = control.createMock(ConfigGadgetManager.class);
        adminLocator = control.createMock(ConfigAdminLocator.class);
        applicationManager = control.createMock(ConfigCrowdApplicationManager.class);
    }

    @Override
    protected void tearDown()
    {
        mailServerManager = null;
        propertySetManager = null;
        serviceManager = null;
        configSequence = null;
        dashboardManager = null;
        gadgetManager = null;
        adminLocator = null;
        applicationManager = null;
        control = null;
    }

    public void testIsJiraConfig() throws Exception
    {
        assertFalse(JiraConfig.isJiraXml(null));
        assertFalse(JiraConfig.isJiraXml(createEmptyDocument("shshshshjs")));
        assertTrue(JiraConfig.isJiraXml(createJiraConfig()));

        final Document notSoEmptyDocument = createJiraConfig();
        notSoEmptyDocument.addComment("comment");
        notSoEmptyDocument.getRootElement().addAttribute("rna", "value");
        notSoEmptyDocument.getRootElement().addElement("rna").setText("valye");
        assertTrue(JiraConfig.isJiraXml(notSoEmptyDocument));
    }

    public void testEmptyCotr() throws Exception
    {
        JiraConfig config = new JiraConfig();
        final Document document = config.getDocument();
        final Element root = document.getRootElement();

        assertEquals(ROOT_ELEMENT, root.getName());
        assertTrue(root.elements().isEmpty());
        assertTrue(root.attributes().isEmpty());
    }

    public void testCotrDocumentFile() throws Exception
    {
        File file = new File("/tmp/test.txt");
        Document doc = createJiraConfig();

        JiraConfig config = new JiraConfig(doc, file);
        assertSame(file, config.getFile());
        assertSame(doc, config.getDocument());

        final Document notSoEmptyDocument = createJiraConfig();
        notSoEmptyDocument.addComment("comment");
        notSoEmptyDocument.getRootElement().addAttribute("rna", "value");
        notSoEmptyDocument.getRootElement().addElement("rna").setText("valye");
        assertTrue(JiraConfig.isJiraXml(notSoEmptyDocument));

        config = new JiraConfig(notSoEmptyDocument, null);
        assertNull(config.getFile());
        assertSame(notSoEmptyDocument, config.getDocument());

        try
        {
            new JiraConfig(null, file);
            fail("Expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new JiraConfig(createEmptyDocument("djsdjaksda"), file);
            fail("Expected IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    public void testSetApplicationProperties() throws Exception
    {
        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        control.replay();
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();
        configPropertySet.setTextProperty("this", "mine");
        config.setApplicationProperties(configPropertySet);

        assertEquals(configPropertySet, config.getApplicationProperties());

        control.verify();
    }

    public void testGetApplicationProperties() throws Exception
    {
        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        ConfigPropertySet set = new ConfigPropertySet();
        set.setLongProperty("value", 1L);

        expect(propertySetManager.loadPropertySet(PROP_NAME, PROP_ID)).andReturn(set);

        control.replay();

        assertEquals(set, config.getApplicationProperties());
        
        control.verify();
    }

    public void testSetServices() throws Exception
    {
        ConfigService service = new ConfigService().setId(10L).setName("test");
        List<ConfigService> services = singletonList(service);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setServices(services);
        assertEquals(services, config.getServices());

        control.verify();
    }

    public void testGetServices() throws Exception
    {
        ConfigService service = new ConfigService().setId(10L).setName("test");
        List<ConfigService> services = singletonList(service);

        expect(serviceManager.loadServices()).andReturn(services).andReturn(null);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertEquals(services, config.getServices());

        //reset the config.
        config.setServices(null);

        assertEquals(Collections.<ConfigService>emptyList(), config.getServices());

        control.verify();
    }

    public void testSetMail() throws Exception
    {
        ConfigMailServer server = new ConfigMailServer().setId(10L).setName("test");
        List<ConfigMailServer> servers = singletonList(server);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setMailServers(servers);
        assertEquals(servers, config.getMailServers());

        control.verify();
    }

    public void testGetMail() throws Exception
    {
        ConfigMailServer server = new ConfigMailServer().setId(10L).setName("test");
        List<ConfigMailServer> servers = singletonList(server);

        expect(mailServerManager.loadServers()).andReturn(servers).andReturn(null);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertEquals(servers, config.getMailServers());

        //reset the config.
        config.setMailServers(null);

        assertEquals(Collections.<ConfigMailServer>emptyList(), config.getMailServers());

        control.verify();
    }

    public void testGetDashboards() throws Exception
    {
        ConfigDashboard dashboard = new ConfigDashboard().setId(10L).setName("test");
        List<ConfigDashboard> dashboards = singletonList(dashboard);

        expect(dashboardManager.loadDashboards()).andReturn(dashboards).andReturn(null);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertEquals(dashboards, config.getDashboards());
        assertEquals(dashboards, config.getDashboards());

        //reset the config.
        config.setDashboards(null);

        assertEquals(Collections.<ConfigDashboard>emptyList(), config.getDashboards());

        control.verify();
    }

    public void testGetExternalGadgets() throws Exception
    {
        ConfigExternalGadget external = new ConfigExternalGadget().setId(10L);
        List<ConfigExternalGadget> externals = singletonList(external);

        expect(gadgetManager.loadExternalGadgets()).andReturn(externals).andReturn(null);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertEquals(externals, config.getExternalGadgets());
        assertEquals(externals, config.getExternalGadgets());

        //reset the config.
        config.setExternalGadgets(null);

        assertEquals(Collections.<ConfigExternalGadget>emptyList(), config.getExternalGadgets());

        control.verify();
    }

    public void testCrowdApplications() throws Exception
    {
        ConfigCrowdApplication application = new ConfigCrowdApplication().setId(10L);
        List<ConfigCrowdApplication> apps = singletonList(application);

        expect(applicationManager.loadApplications()).andReturn(apps).andReturn(null);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertEquals(apps, config.getCrowdApplications());
        assertEquals(apps, config.getCrowdApplications());

        //reset the config.
        config.setCrowdApplications(null);

        assertEquals(Collections.<ConfigCrowdApplication>emptyList(), config.getCrowdApplications());

        control.verify();
    }

    public void testSaveNoData() throws Exception
    {
        expect(configSequence.save()).andReturn(false);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertFalse(config.save());

        control.verify();
    }

    public void testSaveWithApplicationPropertyChanges() throws Exception
    {
        ConfigPropertySet propertySet = new ConfigPropertySet("test", 10101L);

        expect(propertySetManager.savePropertySet(propertySet.copyForEntity(PROP_NAME, PROP_ID))).andReturn(true);
        expect(configSequence.save()).andReturn(false);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setApplicationProperties(propertySet);
        assertTrue(config.save());

        control.verify();

    }

    public void testSaveWithServiceChanges() throws Exception
    {
        ConfigService srv = new ConfigService().setId(17272L).setName("blarg");
        List<ConfigService> srvs = singletonList(srv);

        expect(serviceManager.saveServices(srvs)).andReturn(true);
        expect(configSequence.save()).andReturn(false);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setServices(srvs);
        assertTrue(config.save());

        control.verify();
    }

    public void testSaveWithMailChanges() throws Exception
    {
        ConfigMailServer mailsrv = new ConfigMailServer().setId(17272L).setName("blarg");
        List<ConfigMailServer> mailsrvs = singletonList(mailsrv);

        expect(mailServerManager.saveServers(mailsrvs)).andReturn(true);
        expect(configSequence.save()).andReturn(false);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setMailServers(mailsrvs);
        assertTrue(config.save());

        control.verify();
    }

    public void testSaveWithDashboardChanges() throws Exception
    {
        ConfigDashboard dashboard = new ConfigDashboard().setId(17272L).setName("blarg");
        List<ConfigDashboard> dashboards = singletonList(dashboard);

        expect(dashboardManager.saveDashboards(dashboards)).andReturn(true);
        expect(configSequence.save()).andReturn(false);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setDashboards(dashboards);
        assertTrue(config.save());

        control.verify();
    }

    public void testSaveWithCrowdApplicationChanges() throws Exception
    {
        ConfigCrowdApplication app = new ConfigCrowdApplication().setId(17272L).setName("blarg");
        List<ConfigCrowdApplication> apps = singletonList(app);

        expect(applicationManager.saveApplications(apps)).andReturn(true);
        expect(configSequence.save()).andReturn(false);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setCrowdApplications(apps);
        assertTrue(config.save());

        control.verify();
    }

    public void testGetAdmins() throws Exception
    {
        Set<String> admins = Sets.newHashSet("a", "c", "d");

        expect(adminLocator.locateSystemAdmins()).andReturn(admins);

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        assertEquals(admins, config.getSystemAdmins());

        //Make sure the lookup is cached.
        assertEquals(admins, config.getSystemAdmins());
        control.verify();
    }

    public void testGetBuildNumberWithNumberInData() throws Exception
    {
        control.replay();

        ConfigPropertySet ps = new ConfigPropertySet();
        ps.setLongProperty("jira.version.patched", 485L);

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setApplicationProperties(ps);

        assertEquals(485, config.getBuildNumber());

        control.verify();
    }

    public void testGetBuildNumberNoNumberInData() throws Exception
    {
        control.replay();

        ConfigPropertySet ps = new ConfigPropertySet();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setApplicationProperties(ps);

        assertEquals(0, config.getBuildNumber());

        control.verify();
    }
    
    public void testSaveCheckChangedFlag() throws Exception
    {
        //We just want to check all the combinations. We use an integer to generate all these combinations
        //for us.
        for (int i = 0; i < (1 << 7); i++)
        {
            checkSave(i);
        }
    }

    private static boolean isBitSet(int number, int bit)
    {
        return (number & (1 << bit)) != 0;
    }

    private void checkSave(int bits)
    {
        control.reset();

        List<ConfigMailServer> mailsrvs = singletonList(new ConfigMailServer().setId(17272L).setName("blarg"));
        List<ConfigService> srvs = singletonList(new ConfigService().setId(17272L).setName("blarg"));
        List<ConfigDashboard> dashboards = singletonList(new ConfigDashboard().setId(17272L).setName("blarg"));
        List<ConfigExternalGadget> externalGadgets = singletonList(new ConfigExternalGadget().setId(17272L).setGadgetXml("blarg"));
        List<ConfigCrowdApplication> applications = singletonList(new ConfigCrowdApplication().setId(4747487L));
        ConfigPropertySet ap = new ConfigPropertySet("aaa", 10101L);
        ap.setStringProperty("dkasla", "1111");

        expect(propertySetManager.savePropertySet(ap.copyForEntity(PROP_NAME, PROP_ID))).andReturn(isBitSet(bits, 0));
        expect(mailServerManager.saveServers(mailsrvs)).andReturn(isBitSet(bits, 1));
        expect(serviceManager.saveServices(srvs)).andReturn(isBitSet(bits, 2));
        expect(configSequence.save()).andReturn(isBitSet(bits, 3));
        expect(dashboardManager.saveDashboards(dashboards)).andReturn(isBitSet(bits, 4));
        expect(gadgetManager.saveExternalGadgets(externalGadgets)).andReturn(isBitSet(bits, 5));
        expect(applicationManager.saveApplications(applications)).andReturn(isBitSet(bits, 6));

        control.replay();

        final JiraConfig config = new JiraConfig(createJiraConfig(), new File("m"), configSequence, mailServerManager,
                propertySetManager, serviceManager, dashboardManager, gadgetManager, adminLocator, applicationManager);

        config.setMailServers(mailsrvs);
        config.setServices(srvs);
        config.setApplicationProperties(ap);
        config.setDashboards(dashboards);
        config.setExternalGadgets(externalGadgets);
        config.setCrowdApplications(applications);

        assertEquals(bits != 0, config.save());

        control.verify();
    }

    private static Document createJiraConfig()
    {
        return createEmptyDocument(ROOT_ELEMENT);
    }

    private static Document createEmptyDocument(final String rootElement)
    {
        final DocumentFactory instance = DocumentFactory.getInstance();
        final Document rootDoc = instance.createDocument();
        rootDoc.setRootElement(instance.createElement(rootElement));
        return rootDoc;
    }
}
