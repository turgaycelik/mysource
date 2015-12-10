package com.atlassian.jira.cluster;

/**
 * Represents the set of messages that can be sent in the cluster
 *
 * @since v6.1
 */
public class Message
{
    public final static String DELIMITER = ":-";

    private final String channel;
    private final String supplementalInformation;

    public Message(String channel, String supplementalInformation)
    {
        this.channel = channel;
        this.supplementalInformation = supplementalInformation;
    }

    public static Message fromString(final String message)
    {
        final String supplementalInformation;
        final String channel;
        final int separatorIndex = message.indexOf(DELIMITER);
        if (separatorIndex > 0)
        {
            supplementalInformation = message.substring(separatorIndex + DELIMITER.length());
            channel = message.substring(0, separatorIndex);
        }
        else
        {
            supplementalInformation = null;
            channel = message;
        }
        return new Message(channel, supplementalInformation);
    }

    public String getChannel()
    {
        return channel;
    }

    public String getSupplementalInformation()
    {
        return supplementalInformation;
    }

    @Override
    public String toString()
    {
        return serializeAsString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Message message = (Message) o;

        return channel.equals(message.channel) && isSameSupplementalInformation(message);
    }

    private boolean isSameSupplementalInformation(final Message message)
    {
        return supplementalInformation == null ?
                message.supplementalInformation == null : supplementalInformation.equals(message.supplementalInformation);
    }

    @Override
    public int hashCode()
    {
        int result = channel.hashCode();
        result = 31 * result + (supplementalInformation != null ? supplementalInformation.hashCode() : 0);
        return result;
    }

    private String serializeAsString()
    {
        StringBuilder sb = new StringBuilder(channel);
        if (supplementalInformation != null)
        {
            sb.append(DELIMITER);
            sb.append(supplementalInformation);
        }
        return sb.toString();
    }
}
