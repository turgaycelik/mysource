package com.atlassian.jira.cluster;

import com.atlassian.jira.event.DefaultListenerManager;

import org.junit.Test;

import static com.atlassian.jira.cluster.Message.DELIMITER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Unit test of the {@link Message} class.
 *
 * @since 6.2
 */
public class TestMessage
{

    private final String CHANNEL_1 = "MyMessage";
    private final String CHANNEL_2 = "AnotherMessage";

    @Test
    public void shouldParseNoSupplementalInfoWhenSeparatorNotPresent()
    {
        // Set up
        final String messageType = CHANNEL_1;

        // Invoke
        final Message message = Message.fromString(messageType);

        // Check
        assertEquals(messageType, message.getChannel());
        assertNull(message.getSupplementalInformation());
    }

    @Test
    public void shouldParseSupplementalInfoWhenSeparatorPresent()
    {
        // Set up
        final String messageType = CHANNEL_1;
        final String supplementalInfo = "foo";
        final String messageString = messageType + DELIMITER + supplementalInfo;

        // Invoke
        final Message message = Message.fromString(messageString);

        // Check
        assertEquals(messageType, message.getChannel());
        assertEquals(supplementalInfo, message.getSupplementalInformation());
    }

    @Test
    public void shouldEqualMessageOfSameTypeWithNullSupplementalInfo()
    {
        // Set up
        final String message = CHANNEL_1;
        final Message message1 = Message.fromString(message);
        final Message message2 = Message.fromString(message);

        // Invoke and check
        assertEquals(message1, message2);
    }

    @Test
    public void shouldEqualMessageOfSameTypeWithSameSupplementalInfo()
    {
        // Set up
        final String message = CHANNEL_1 + DELIMITER + "foo";
        final Message message1 = Message.fromString(message);
        final Message message2 = Message.fromString(message);

        // Invoke and check
        assertEquals(message1, message2);
    }

    @Test
    public void shouldNotEqualMessageOfSameTypeWithDifferentNullSupplementalInfo()
    {
        // Set up
        final String message = CHANNEL_1;
        final Message message1 = Message.fromString(message);
        final Message message2 = Message.fromString(message + DELIMITER + "supplemental");

        // Invoke and check
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
    }

    @Test
    public void shouldNotEqualMessageOfSameTypeWithDifferentNonNullSupplementalInfo()
    {
        // Set up
        final String message = CHANNEL_1;
        final Message message1 = Message.fromString(message + DELIMITER + "foo");
        final Message message2 = Message.fromString(message + DELIMITER + "bar");

        // Invoke and check
        assertFalse(message1.equals(message2));
        assertFalse(message2.equals(message1));
    }

    @Test
    public void shouldNotEqualMessageOfDifferentTypeWithNullSupplementalInfo()
    {
        // Set up
        final Message message1 = Message.fromString(CHANNEL_1);
        final Message message2 = Message.fromString(CHANNEL_2);

        // Invoke and check
        assertFalse(message1.equals(message2));
    }

    @Test
    public void refreshListenersMessageShouldHaveCorrectTypeAndNoSupplementalInfo()
    {
        // Invoke
        final Message message = new Message(DefaultListenerManager.REFRESH_LISTENERS, null);

        // Check
        assertEquals(DefaultListenerManager.REFRESH_LISTENERS, message.getChannel());
        assertNull(message.getSupplementalInformation());
    }
}
