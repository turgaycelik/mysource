package com.atlassian.jira.functest.unittests.config.mail;

import com.atlassian.jira.functest.config.mail.ConfigMailServer;
import junit.framework.TestCase;

/**
 * Test for {@link ConfigMailServer}
 *
 * @since v4.1
 */
public class TestConfigMailServer extends TestCase
{
    public void testType() throws Exception
    {
        assertNull(ConfigMailServer.Type.parseString(null));

        assertSame(ConfigMailServer.Type.POP, ConfigMailServer.Type.parseString("pOP"));
        assertSame(ConfigMailServer.Type.POP, ConfigMailServer.Type.parseString("POP"));
        assertSame(ConfigMailServer.Type.POP, ConfigMailServer.Type.parseString("pop"));

        assertSame(ConfigMailServer.Type.SMTP, ConfigMailServer.Type.parseString("SmTP"));
        assertSame(ConfigMailServer.Type.SMTP, ConfigMailServer.Type.parseString("SMTP"));
        assertSame(ConfigMailServer.Type.SMTP, ConfigMailServer.Type.parseString("smtp"));

        assertNull(ConfigMailServer.Type.parseString("someRandomType"));
    }

    public void testMailServers() throws Exception
    {
        final ConfigMailServer mailServer = new ConfigMailServer().setId(10L).setName("name").setDescription("desc").setFrom("jack@jill.com");
        mailServer.setPrefix("PREFIX").setPort(10).setType(ConfigMailServer.Type.SMTP).setServerName("localhost").setJndiLocation("location");
        mailServer.setUserName("bbain").setPassword("password");

        assertEquals(10L, (long)mailServer.getId());
        assertEquals("name", mailServer.getName());
        assertEquals("desc", mailServer.getDescription());
        assertEquals("jack@jill.com", mailServer.getFrom());
        assertEquals("PREFIX", mailServer.getPrefix());
        assertEquals(10, (int)mailServer.getPortNumber());
        assertEquals(ConfigMailServer.Type.SMTP, mailServer.getType());
        assertEquals("localhost", mailServer.getServerName());
        assertEquals("location", mailServer.getJndiLocation());
        assertEquals("bbain", mailServer.getUserName());
        assertEquals("password", mailServer.getPassword());

        mailServer.setPort("abc");
        assertEquals("abc", mailServer.getPort());
        assertNull(mailServer.getPortNumber());

        mailServer.setType("skss");
        assertNull(mailServer.getType());
        mailServer.setType("pOP");
        assertEquals(ConfigMailServer.Type.POP, mailServer.getType());
    }
}