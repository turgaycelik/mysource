package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestMessageSetImpl
{

    @Test
    public void testAddMessageLinkToWarning() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        messageSet.addWarningMessage("I am warning", link);
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals("I am warning", messageSet.getWarningMessages().iterator().next());
        assertEquals(link, messageSet.getLinkForWarning("I am warning"));
    }

    @Test
    public void testAddMessageLinkWithLevelToWarning() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        messageSet.addMessage(MessageSet.Level.WARNING, "I am warnie", link);

        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals("I am warnie", messageSet.getWarningMessages().iterator().next());
        assertEquals(link, messageSet.getLinkForWarning("I am warnie"));
    }

    @Test
    public void testAddMessageWithLevelToWarning()
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        messageSet.addMessage(MessageSet.Level.WARNING, "WARNIE!!!");

        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals("WARNIE!!!", messageSet.getWarningMessages().iterator().next());
    }

    @Test
    public void testAddMessageLinkToError() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        messageSet.addErrorMessage("I am error", link);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("I am error", messageSet.getErrorMessages().iterator().next());
        assertEquals(link, messageSet.getLinkForError("I am error"));
    }

    @Test
    public void testAddMessageLinkWithLevelToError() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        messageSet.addMessage(MessageSet.Level.ERROR, "I am erroneous", link);

        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("I am erroneous", messageSet.getErrorMessages().iterator().next());
        assertEquals(link, messageSet.getLinkForError("I am erroneous"));
    }

    @Test
    public void testAddMessageWithLevelToError()
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        messageSet.addMessage(MessageSet.Level.ERROR, "ERRNIE!!!");

        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("ERRNIE!!!", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testAddMessageLinkUnique() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        messageSet.addWarningMessage("test", link);
        messageSet.addWarningMessage("test", link);
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals(link, messageSet.getLinkForWarning("test"));
    }

    @Test
    public void testAddMessageLinkUniqueGetsReplaced() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        final MessageSet.MessageLink link2 = new MessageSet.MessageLink("helloes", "/world/boom");
        messageSet.addWarningMessage("test", link);
        messageSet.addWarningMessage("test", link2);
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals(link2, messageSet.getLinkForWarning("test"));
    }

    @Test
    public void testAddMessageLinkUniqueOnlyForMessageType() throws Exception
    {
        MessageSetImpl messageSet = new MessageSetImpl();

        final MessageSet.MessageLink link = new MessageSet.MessageLink("hello", "/world");
        messageSet.addWarningMessage("test", link);
        messageSet.addErrorMessage("test", link);
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals(link, messageSet.getLinkForWarning("test"));
        assertEquals(link, messageSet.getLinkForError("test"));
    }

    @Test
    public void testHasAnyMessages()
    {
        MessageSetImpl messageSet = new MessageSetImpl();
        assertFalse(messageSet.hasAnyMessages());

        messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("fool");
        assertTrue(messageSet.hasAnyMessages());

        messageSet = new MessageSetImpl();
        messageSet.addWarningMessage("fool");
        assertTrue(messageSet.hasAnyMessages());
    }

    @Test
    public void testAddErrorMessage()
    {
        MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("fool");
        messageSet.addErrorMessage("fubar");
        messageSet.addErrorMessage("fool");

        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());

        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().contains("fool"));
        assertTrue(messageSet.getErrorMessages().contains("fubar"));
    }

    @Test
    public void testAddWarningMessage()
    {
        MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addWarningMessage("fool");
        messageSet.addWarningMessage("fubar");
        messageSet.addWarningMessage("fool");

        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyWarnings());
        assertFalse(messageSet.hasAnyErrors());

        assertEquals(2, messageSet.getWarningMessages().size());
        assertTrue(messageSet.getWarningMessages().contains("fool"));
        assertTrue(messageSet.getWarningMessages().contains("fubar"));
    }

    @Test
    public void testAddMessageSetNull()
    {
        MessageSetImpl messageSet1 = new MessageSetImpl();
        messageSet1.addErrorMessage("snafu");
        messageSet1.addErrorMessage("fubar");

        messageSet1.addMessageSet(null);
        assertEquals(2, messageSet1.getErrorMessages().size());
        assertTrue(messageSet1.getErrorMessages().contains("snafu"));
        assertTrue(messageSet1.getErrorMessages().contains("fubar"));
        assertFalse(messageSet1.hasAnyWarnings());
    }
    
    @Test
    public void testAddMessageSet()
    {
        MessageSetImpl messageSet1 = new MessageSetImpl();
        messageSet1.addErrorMessage("snafu");
        messageSet1.addErrorMessage("fubar");

        MessageSetImpl messageSet2 = new MessageSetImpl();
        messageSet2.addErrorMessage("purple");
        messageSet2.addErrorMessage("fubar");
        messageSet2.addWarningMessage("!!!");

        messageSet1.addMessageSet(messageSet2);
        assertEquals(3, messageSet1.getErrorMessages().size());
        assertTrue(messageSet1.getErrorMessages().contains("snafu"));
        assertTrue(messageSet1.getErrorMessages().contains("fubar"));
        assertTrue(messageSet1.getErrorMessages().contains("purple"));
        assertEquals(1, messageSet1.getWarningMessages().size());
        assertTrue(messageSet1.getWarningMessages().contains("!!!"));
    }
}
