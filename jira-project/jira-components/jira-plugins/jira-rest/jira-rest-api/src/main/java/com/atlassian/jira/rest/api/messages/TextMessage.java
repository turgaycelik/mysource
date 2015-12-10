package com.atlassian.jira.rest.api.messages;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Represents a simple informational text message.
 *
 * @since v4.2
 */
@XmlRootElement(name="text-message")
public class TextMessage
{
    @XmlValue
    private String content;

    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private TextMessage() {}

    public TextMessage(final String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) { return true; }

        if (!(obj instanceof TextMessage)) { return false; }

        TextMessage rhs = (TextMessage) obj;

        return new EqualsBuilder().
                append(content, rhs.content).
                isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 31).
                append(content).
                toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("content", content).
                toString();
    }
}
