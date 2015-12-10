package com.atlassian.mail.server.managers;

import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestOFBizMailServerManager
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    private OFBizMailServerManager mailServerManager = new OFBizMailServerManager();

    @Test
    public void testCreateSMTPAndGet() throws Exception
    {
        MailServer localMailServer = new SMTPMailServerImpl(1000L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows");
        Long id = mailServerManager.create(localMailServer);
        assertThat(id, Matchers.notNullValue());
        MailServer mailServer = mailServerManager.getMailServer(id);
        assertEquals(localMailServer, mailServer);
    }

    @Test
    public void testUpdateSMTP() throws Exception
    {
        Long id = mailServerManager.create(new SMTPMailServerImpl(1000L, "Name", "Description", "owen@atlassian.com", "[OWEN]", false, "mail.atlassian.com", "owen", "fellows"));
        MailServer mailServer = mailServerManager.getMailServer("Name");
        assertTrue("Mail Server returned should be an SMTP server", OFBizMailServerManager.SERVER_TYPES[1].equals(mailServer.getType()));

        SMTPMailServer smtp = (SMTPMailServer) mailServer;
        smtp.setUsername(null);
        smtp.setPassword(null);
        smtp.setName("new name");

        mailServerManager.update(smtp);
        MailServer updatedMailServer = mailServerManager.getMailServer("new name");

        assertThat(updatedMailServer.getUsername(), Matchers.nullValue());
        assertThat(updatedMailServer.getPassword(), Matchers.nullValue());
        assertEquals("new name", updatedMailServer.getName());

        //manually override id due to fact, that mockOfbizDelegator is not adding an id
        updatedMailServer.setId(id);
        assertEquals(mailServer, updatedMailServer);
    }


    @Test
    public void testDeleteSMTP() throws Exception
    {
        Long id = mailServerManager.create(new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows"));
        MailServer mailServer = mailServerManager.getMailServer(id);
        mailServerManager.delete(mailServer.getId());
        assertThat(mailServerManager.getMailServer(id), Matchers.nullValue());
    }

    @Test
    public void testGetMailServerGV() throws Exception
    {
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("id", 1000L)
                .add("name", "Name")
                .add("description", "Description")
                .add("from", "owen@atlassian.com")
                .add("prefix", "[OWEN]")
                .add("servername", "mail.atlassian.com")
                .add("smtpPort", "25")
                .add("protocol", MailProtocol.SMTP.getProtocol())
                .add("username", "owen")
                .add("password", "fellows")
                .add("type", OFBizMailServerManager.SERVER_TYPES[1])
                .add("istlsrequired", "false")
                .add("timeout", 5000L)
                .add("socksHost", null)
                .add("socksPort", null).toMap();
        GenericValue gv = ofBizDelegator.makeValue("MailServer", params);
        Long id = mailServerManager.create(new SMTPMailServerImpl(1000L,"Name","Description","owen@atlassian.com","[OWEN]",false,MailProtocol.SMTP,"mail.atlassian.com","25",false,"owen","fellows",5000));
        GenericValue mailServerGV = mailServerManager.getMailServerGV(id);
        assertEquals(gv, mailServerGV);
    }

    @Test
    public void testConstructMailServer1()
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1000L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows");
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("id", 1000L)
                .add("name", "Name")
                .add("description", "Description")
                .add("from", "owen@atlassian.com")
                .add("prefix", "[OWEN]")
                .add("servername", "mail.atlassian.com")
                .add("username", "owen")
                .add("password", "fellows")
                .add("smtpPort", "25")
                .add("protocol", "smtp")
                .add("istlsrequired", "false")
                .add("type", OFBizMailServerManager.SERVER_TYPES[1]).toMap();
        GenericValue gv = ofBizDelegator.makeValue("MailServer", params);
        MailServer newMailServer = mailServerManager.constructMailServer(gv);
        assertEquals(oldMailServer, newMailServer);
    }

    @Test
    public void testConstructMailServer2()
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1000L,"Name","Description","owen@atlassian.com","[OWEN]",true,"mail.atlassian.com",null,null);
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("id", 1000L)
                .add("name", "Name")
                .add("description", "Description")
                .add("from", "owen@atlassian.com")
                .add("prefix", "[OWEN]")
                .add("jndilocation", "mail.atlassian.com")
                .add("smtpPort", "25")
                .add("protocol", "smtp")
                .add("username", null)
                .add("password", null)
                .add("type", OFBizMailServerManager.SERVER_TYPES[1]).toMap();
        GenericValue gv = ofBizDelegator.makeValue("MailServer", params);
        MailServer newMailServer = mailServerManager.constructMailServer(gv);
        assertEquals(oldMailServer, newMailServer);
    }

    @Test
    public void testConstructMailServer3()
    {
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("id", 1000L)
                .add("name", "Name")
                .add("description", "Description")
                .add("servername", "mail.atlassian.com")
                .add("username", "owen")
                .add("password", "fellows")
                .add("smtpPort", "110")
                .add("protocol", "pop")
                .add("type", OFBizMailServerManager.SERVER_TYPES[0]).toMap();
        GenericValue gv = ofBizDelegator.makeValue("MailServer", params);
        MailServer newMailServer = mailServerManager.constructMailServer(gv);
        PopMailServer expectedResult = new PopMailServerImpl(1000L, "Name", "Description", "mail.atlassian.com", "owen", "fellows");
        assertEqualServers(expectedResult, newMailServer);
    }

    private void assertEqualServers(final MailServer expectedResult, final MailServer newMailServer)
    {
        assertEquals(expectedResult.getId(), newMailServer.getId());
        assertEquals(expectedResult.getName(), newMailServer.getName());
        assertEquals(expectedResult.getDescription(), newMailServer.getDescription());
        assertEquals(expectedResult.getHostname(), newMailServer.getHostname());
        assertEquals(expectedResult.getUsername(), newMailServer.getUsername());
        assertEquals(expectedResult.getPassword(), newMailServer.getPassword());
    }

    @Test
    public void testConstructMailServer4()
    {
        Map<String, Object> params = MapBuilder.<String, Object>build("type", "notype");
        GenericValue gv = ofBizDelegator.makeValue("MailServer", params);
        MailServer newMailServer = mailServerManager.constructMailServer(gv);
        assertEquals(null, newMailServer);
    }

    @Test
    public void testGetMapFromColumns1() throws Exception
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows");
        oldMailServer.setSocksHost("socks");
        oldMailServer.setSocksPort("1080");
        Map oldMap = mailServerManager.getMapFromColumns(oldMailServer);
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("name", "Name")
                .add("description", "Description")
                .add("from", "owen@atlassian.com")
                .add("prefix", "[OWEN]")
                .add("servername", "mail.atlassian.com")
                .add("username", "owen")
                .add("password", "fellows")
                .add("smtpPort", "25")
                .add("protocol", MailProtocol.SMTP.getProtocol())
                .add("type", OFBizMailServerManager.SERVER_TYPES[1])
                .add("istlsrequired", "false")
                .add("timeout", 10000L)
                .add("socksHost", "socks")
                .add("socksPort", "1080").toMap();
        assertEquals(oldMap, params);
    }

    @Test
    public void testGetMapFromColumns2() throws Exception
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",true,"mail.atlassian.com",null,null);
        Map oldMap = mailServerManager.getMapFromColumns(oldMailServer);
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("name", "Name")
                .add("description", "Description")
                .add("from", "owen@atlassian.com")
                .add("prefix", "[OWEN]")
                .add("servername", null)
                .add("jndilocation", "mail.atlassian.com")
                .add("username", null)
                .add("password", null)
                .add("smtpPort", "25")
                .add("protocol", MailProtocol.SMTP.getProtocol())
                .add("type", OFBizMailServerManager.SERVER_TYPES[1])
                .add("istlsrequired", "false")
                .add("timeout", 10000L)
                .add("socksHost", null)
                .add("socksPort", null).toMap();
        assertEquals(oldMap, params);
    }

    @Test
    public void testGetMapFromColumns3() throws Exception
    {
        MailServer oldMailServer = new PopMailServerImpl(1L, "Name", "Description", "mail.atlassian.com", "owen", "fellows");
        oldMailServer.setSocksHost("socks");
        oldMailServer.setSocksPort("1080");
        Map oldMap = mailServerManager.getMapFromColumns(oldMailServer);
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("name", "Name")
                .add("description", "Description")
                .add("username", "owen")
                .add("password", "fellows")
                .add("smtpPort", "110")
                .add("protocol", MailProtocol.POP.getProtocol())
                .add("type", OFBizMailServerManager.SERVER_TYPES[0])
                .add("servername", "mail.atlassian.com")
                .add("timeout", 10000L)
                .add("socksHost", "socks")
                .add("socksPort", "1080").toMap();
        assertEquals(oldMap, params);
    }

    @Test
    public void testGetDefaultSMTPMailServer() throws Exception
    {
        SMTPMailServer server1 = new SMTPMailServerImpl(1000L, "Smtp Server Name1", "Description1", "test1@atlassian.com", "[OWEN]", false, "mail1.atlassian.com", "owen", "fellows");
        SMTPMailServer server2 = new SMTPMailServerImpl(1001L, "Smtp Server Name", "Description2", "test2@atlassian.com", "[OWEN]", false, "mail2.atlassian.com", "owen", "fellows");
        mailServerManager.create(server1);
        mailServerManager.create(server2);

        // get it from the prefereneces-default.xml file
        assertEquals(server2, mailServerManager.getDefaultSMTPMailServer());
        assertEquals(server1, mailServerManager.getSmtpMailServers().get(0));

        // now get if from the first smtp server in servers
        mailServerManager.delete(1001L);
        assertEquals(server1, mailServerManager.getDefaultSMTPMailServer());
        assertEquals(server1, mailServerManager.getSmtpMailServers().get(0));

        // cannot get any smtp servers, return null
        mailServerManager.delete(1000L);
        assertThat(mailServerManager.getDefaultSMTPMailServer(), Matchers.nullValue());
    }

    @Test
    public void testGetDefaultPopMailServer() throws Exception
    {
        PopMailServer server1 = new PopMailServerImpl(1000L, "Pop Server Name1", "Description1", "mail1.atlassian.com", "ownen", "fellows");
        PopMailServer server2 = new PopMailServerImpl(1001L, "Pop Server Name", "Description2", "mail2.atlassian.com", "ownen", "fellows");
        mailServerManager.create(server1);
        mailServerManager.create(server2);

        assertEquals(server2, mailServerManager.getDefaultPopMailServer());
        assertEquals(server1, mailServerManager.getPopMailServers().get(0));

        mailServerManager.delete(1001L);
        assertEquals(server1, mailServerManager.getDefaultPopMailServer());
        assertEquals(server1, mailServerManager.getPopMailServers().get(0));

        mailServerManager.delete(1000L);
        assertThat(mailServerManager.getDefaultPopMailServer(), Matchers.nullValue());
    }

}