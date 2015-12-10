package com.atlassian.jira.bc.project.version.remotelink;

import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.version.Version;

import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @since v6.1.1
 */
@SuppressWarnings("ConstantConditions")
public class TestRemoteVersionLinkImpl
{
    @Test(expected = IllegalArgumentException.class)
    public void testConstructionNullVersion()
    {
        new RemoteVersionLinkImpl(null, "MyGlobalId", "\"value\"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionNullGlobalId()
    {
        new RemoteVersionLinkImpl(new MockVersion(), null, "\"value\"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionBlankGlobalId()
    {
        new RemoteVersionLinkImpl(new MockVersion(), "  ", "\"value\"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionNullJson()
    {
        new RemoteVersionLinkImpl(new MockVersion(), "MyGlobalId", null);
    }

    @Test
    public void testConstructionInvalidJson()
    {
        final Version version = new MockVersion();
        final RemoteVersionLink link = new RemoteVersionLinkImpl(version, "MyGlobalId", "asdf");

        try
        {
            link.getApplicationName();
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            assertEquals("Invalid JSON", iae.getMessage());
            assertNotNull("The exception should have had a 'cause'", iae.getCause());
            assertThat(iae.getCause().getMessage(), containsString("Unexpected character ('a'"));
        }

        try
        {
            link.getApplicationType();
            fail("Should have thrown IllegalArgumentException");
        }
        catch (IllegalArgumentException iae)
        {
            assertEquals("Invalid JSON", iae.getMessage());
            assertNotNull("The exception should have had a 'cause'", iae.getCause());
            assertThat(iae.getCause().getMessage(), containsString("Unexpected character ('a'"));
        }

        // These don't involve parsing the JSON, so they should be fine
        assertSame("entity", version, link.getEntity());
        assertEquals("entityId", version.getId(), link.getEntityId());
        assertEquals("globalId", "MyGlobalId", link.getGlobalId());
        assertEquals("json", "asdf", link.getJsonString());
    }

    @Test
    public void testConstructionWithUselessJson()
    {
        final Version version = new MockVersion();
        final RemoteVersionLink link = new RemoteVersionLinkImpl(version, "MyGlobalId", "true");

        // Things that don't require parsing the JSON
        assertSame("entity", version, link.getEntity());
        assertEquals("entityId", version.getId(), link.getEntityId());
        assertEquals("globalId", "MyGlobalId", link.getGlobalId());
        assertEquals("json", "true", link.getJsonString());

        // Stuff that could come from the JSON but can't here
        assertNull("applicationType", link.getApplicationType());
        assertNull("applicationName", link.getApplicationName());
        assertNull("iconTitle", link.getIconTitle());
        assertNull("iconUrl", link.getIconUrl());
        assertNull("summary", link.getSummary());
        assertNull("title", link.getTitle());
        assertNull("url", link.getUrl());
    }

    @Test
    public void testConstructionWithAwesomeJson()
    {
        // Shamelessly stolen from the "JIRA REST API for Remote Issue Links" docs
        final String json = "\n"
                + "{\n"
                + "    \"globalId\": \"system=http://www.mycompany.com/support&id=1\",\n"
                + "    \"application\": {\n"
                + "         \"type\":\"com.acme.tracker\",\n"
                + "         \"name\":\"My Acme Tracker\"\n"
                + "    },\n"
                + "    \"relationship\":\"causes\",\n"
                + "    \"object\": {\n"
                + "        \"url\":\"http://www.mycompany.com/support?id=1\",\n"
                + "        \"title\":\"TSTSUP-111\",\n"
                + "        \"summary\":\"Crazy customer support issue\",\n"
                + "        \"icon\": {\n"
                + "            \"url16x16\":\"http://www.openwebgraphics.com/resources/data/3321/16x16_voice-support.png\",\n"
                + "            \"title\":\"Support Ticket\"\n"
                + "        },\n"
                + "        \"status\": {\n"
                + "            \"resolved\": true,\n"
                + "            \"icon\": {\n"
                + "                \"url16x16\":\"http://www.openwebgraphics.com/resources/data/47/accept.png\",\n"
                + "                \"title\":\"Case Closed\",\n"
                + "                \"link\":\"http://www.mycompany.com/support?id=1&details=closed\"\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}";

        final Version version = new MockVersion();
        final RemoteVersionLink link = new RemoteVersionLinkImpl(version, "MyGlobalId", json);

        // Things that don't require parsing the JSON
        assertSame("entity", version, link.getEntity());
        assertEquals("entityId", version.getId(), link.getEntityId());
        assertEquals("globalId", "MyGlobalId", link.getGlobalId());
        assertEquals("json", json, link.getJsonString());

        // Stuff that could come from the JSON
        assertEquals("applicationType", "com.acme.tracker", link.getApplicationType());
        assertEquals("applicationName", "My Acme Tracker", link.getApplicationName());
        assertEquals("iconTitle", "Support Ticket", link.getIconTitle());
        assertEquals("iconUrl", "http://www.openwebgraphics.com/resources/data/3321/16x16_voice-support.png", link.getIconUrl());
        assertEquals("summary", "Crazy customer support issue", link.getSummary());
        assertEquals("title", "TSTSUP-111", link.getTitle());
        assertEquals("url", "http://www.mycompany.com/support?id=1", link.getUrl());
    }
}
