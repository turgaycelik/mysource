package com.atlassian.jira.plugin.link.confluence.service.rpc;

import com.atlassian.jira.plugin.link.confluence.Builder;
import com.atlassian.jira.util.xml.SecureXmlEntityResolver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for handling XML responses from Confluence.
 *
 * @since v5.0
 */
public abstract class AbstractConfluenceSaxHandler<T, B extends Builder<T>> extends DefaultHandler
{
    private final List<T> entities = new ArrayList<T>();
    private final StringBuilder currentString = new StringBuilder();
    private Fault fault = null;
    private final B builder;
    private NameValuePair currentMember;
    public static final SecureXmlEntityResolver EMPTY_ENTITY_RESOLVER = new SecureXmlEntityResolver();

    protected AbstractConfluenceSaxHandler(final B builder)
    {
        this.builder = builder;
    }

    public List<T> getEntities()
    {
        return entities;
    }

    public boolean hasFault()
    {
        return (fault != null);
    }

    public String getFaultString()
    {
        return fault.getFaultString();
    }

    /**
     * Add the given member to the given builder. The member name will determine the field in the builder.
     *
     * @param member the member to add
     * @param builder the builder to add the member to
     */
    protected abstract void addMember(NameValuePair member, B builder);

    private void addFaultMember(final NameValuePair member)
    {
        if (fault != null)
        {
            if ("faultString".equals(member.getName()))
            {
                fault.setFaultString(member.getValue());
            }
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if ("struct".equals(qName))
        {
            builder.clear();
        }
        else if ("member".equals(qName))
        {
            currentMember = new NameValuePair();
        }
        else if ("fault".equals(qName))
        {
            fault = new Fault();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        currentString.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if ("struct".equals(qName))
        {
            entities.add(builder.build());
            builder.clear();
        }
        else if ("member".equals(qName))
        {
            addFaultMember(currentMember);
            addMember(currentMember, builder);
            currentMember = null;
        }
        else if ("name".equals(qName))
        {
            if (currentMember != null)
            {
                currentMember.setName(currentString.toString());
            }
        }
        else if ("value".equals(qName))
        {
            if (currentMember != null)
            {
                currentMember.setValue(currentString.toString());
            }
        }

        currentString.setLength(0);
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
    {
        return EMPTY_ENTITY_RESOLVER.resolveEntity(publicId,systemId);
    }

    protected static class NameValuePair
    {
        private String name;
        private String value;

        public String getName()
        {
            return name;
        }

        public void setName(final String name)
        {
            this.name = name;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(final String value)
        {
            this.value = value;
        }
    }

    private static class Fault
    {
        private String faultString;

        public String getFaultString()
        {
            return faultString;
        }

        public void setFaultString(String faultString)
        {
            this.faultString = faultString;
        }
    }
}