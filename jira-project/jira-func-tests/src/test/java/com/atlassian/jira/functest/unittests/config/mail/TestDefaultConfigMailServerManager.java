package com.atlassian.jira.functest.unittests.config.mail;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.mail.ConfigMailServer;
import com.atlassian.jira.functest.config.mail.ConfigMailServerManager;
import com.atlassian.jira.functest.config.mail.DefaultConfigMailServerManager;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.classextension.EasyMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.mail.ConfigMailServer}.
 *
 * @since v4.1
 */
public class TestDefaultConfigMailServerManager extends TestCase
{
    private static final String ELEMENT_SERVER = "MailServer";

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_FROM = "from";
    private static final String ATTRIBUTE_PREFIX = "prefix";
    private static final String ATTRIBUTE_SMTP_PORT = "smtpPort";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_SERVERNAME = "servername";
    private static final String ATTRIBUTE_JNDILOCATION = "jndilocation";
    private static final String ATTRIBUTE_USERNAME = "username";
    private static final String ATTRIBUTE_PASSWORD = "password";

    private ConfigMailServer server1;
    private ConfigMailServer server2;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        server1 = new ConfigMailServer().setId(10L).setName("name").setDescription("desc").setFrom("jack@jill.com")
        .setPrefix("PREFIX").setPort(10).setType(ConfigMailServer.Type.SMTP).setServerName("localhost").setJndiLocation("location")
        .setUserName("bbain").setPassword("password");

        server2 = new ConfigMailServer().setFrom("a@jill.com").setId(101L);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        server1 = server2 = null;
    }

    public void testMailServersRead() throws Exception
    {
        final Element rootElement = createStandardRoot();
        final ConfigMailServerManager mgr = new DefaultConfigMailServerManager(rootElement.getDocument(), null);
        final List<ConfigMailServer> actualList = mgr.loadServers();
        assertEqualsCollections(Arrays.asList(server1, server2), actualList);
    }

    public void testMailServersReadNoId() throws Exception
    {
        final Element rootElement1 = createRootElement();
        addServerToRoot(server1, rootElement1);
        addServerToRoot(server2.setId(null), rootElement1);

        final ConfigMailServerManager mgr = new DefaultConfigMailServerManager(rootElement1.getDocument(), null);
        try
        {
            mgr.loadServers();
            fail("Exception should have been thrown.");
        }
        catch (ConfigException e)
        {
            //goodo
        }
    }

    public void testDeleteMailServer() throws Exception
    {
        final Element element = createStandardRoot();
        List<ConfigMailServer> expectedList = Collections.singletonList(server1);

        final ConfigMailServerManager mgr = new DefaultConfigMailServerManager(element.getDocument(), null);

        mgr.saveServers(expectedList);
        assertDocumentContent(element.getDocument(), expectedList);

        expectedList = Collections.emptyList();

        mgr.saveServers(expectedList);
        assertDocumentContent(element.getDocument(), expectedList);        
    }

    public void testAddMailServer() throws Exception
    {
        ConfigSequence sequence = EasyMock.createMock(ConfigSequence.class);

        List<ConfigMailServer> expectedList = Collections.singletonList(server2);
        final Element element = createRootElement();
        addServerToRoot(server1, element);

        EasyMock.expect(sequence.getNextId(ELEMENT_SERVER)).andReturn(5L);

        EasyMock.replay(sequence);

        final ConfigMailServerManager mgr = new DefaultConfigMailServerManager(element.getDocument(), sequence);
        try
        {
            mgr.saveServers(expectedList);
            fail("Don't support adding new servers.");
        }
        catch (ConfigException e)
        {
            //all is good.
        }

        EasyMock.verify(sequence);
    }

    public void testEditMailServer() throws Exception
    {
        final Element element = createRootElement();
        addServerToRoot(server1, element);

        final ConfigMailServerManager mgr = new DefaultConfigMailServerManager(element.getDocument(), null);
        List<ConfigMailServer> expectedList = Collections.singletonList(server1.setPassword("anotherPassword"));
        try
        {
            mgr.saveServers(expectedList);
            fail("Don't support editing servers.");
        }
        catch (ConfigException e)
        {
            //all is good.
        }
    }

    private void assertDocumentContent(final Document actualDocument, final List<ConfigMailServer> expectedList)
    {
        assertEqualsCollections(expectedList, getMailServersElement(actualDocument));
    }

    private static Collection<ConfigMailServer> getMailServersElement(final Document actualDocument)
    {
        List<ConfigMailServer> servers = new ArrayList<ConfigMailServer>();

        @SuppressWarnings ({ "unchecked" })
        final Collection<Element> elements = actualDocument.getRootElement().elements(ELEMENT_SERVER);
        for (Element mailElem : elements)
        {
            servers.add(elementToServer(mailElem));
        }

        return servers;
    }

    private static ConfigMailServer elementToServer(final Element mailElem)
    {
        ConfigMailServer server = new ConfigMailServer();
        server.setId(ConfigXmlUtils.getLongValue(mailElem, ATTRIBUTE_ID));
        server.setName(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_NAME));
        server.setDescription(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_DESCRIPTION));
        server.setFrom(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_FROM));
        server.setPrefix(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_PREFIX));
        server.setPort(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_SMTP_PORT));
        server.setType(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_TYPE));
        server.setServerName(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_SERVERNAME));
        server.setJndiLocation(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_JNDILOCATION));
        server.setUserName(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_USERNAME));
        server.setPassword(ConfigXmlUtils.getTextValue(mailElem, ATTRIBUTE_PASSWORD));

        return server;
    }

    private Element createStandardRoot()
    {
        final Element rootElement = createRootElement();
        addServerToRoot(server1, rootElement);
        addServerToRoot(server2, rootElement);

        return rootElement;
    }

    private static Element addServerToRoot(ConfigMailServer server, Element root)
    {
        final Element newMailElement = ConfigXmlUtils.createNewElement(root, ELEMENT_SERVER);

        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_ID, server.getId());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_NAME, server.getName());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_DESCRIPTION, server.getDescription());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_FROM, server.getFrom());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_PREFIX, server.getPrefix());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_SMTP_PORT, server.getPort());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_TYPE, server.getType());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_SERVERNAME, server.getServerName());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_JNDILOCATION, server.getJndiLocation());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_USERNAME, server.getUserName());
        ConfigXmlUtils.setAttribute(newMailElement, ATTRIBUTE_PASSWORD, server.getPassword());

        return newMailElement;
    }

    private static Element createRootElement()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement("entity-engine-xml");
    }

    private void assertEqualsCollections(Collection<?> one, Collection<?> two)
    {
        assertEquals(String.format("size(%s) != size(%s)", one, two), one.size(), two.size());
        assertTrue(String.format("%s != %s", one, two), one.containsAll(two));
    }
}
