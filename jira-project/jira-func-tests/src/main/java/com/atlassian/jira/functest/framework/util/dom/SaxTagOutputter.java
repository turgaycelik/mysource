package com.atlassian.jira.functest.framework.util.dom;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handle that converts element tags to lower case.  Only seems to work from the Document
 * level down regardless of what Node you start at.  So I wrote the DomNodeOutputter
 */
class SaxTagOutputter extends DefaultHandler implements LexicalHandler
{
    private final StringBuffer sb;
    private final boolean useLowercase;

    public SaxTagOutputter(StringBuffer sb, boolean useLowercase)
    {
        this.sb = sb;
        this.useLowercase = useLowercase;
    }

    private String getName(String localName, String qName)
    {
        String name = localName;
        if (qName != null && qName.length() > 0)
        {
            name = qName;
        }
        return (this.useLowercase ? name.toLowerCase() : name);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException
    {
        sb.append('<');
        sb.append(getName(localName, qName));
        for (int i = 0; i < atts.getLength(); i++)
        {
            sb.append(' ');
            sb.append(getName(atts.getLocalName(i), atts.getQName(i)));
            sb.append('=');
            sb.append('"');
            sb.append(atts.getValue(i));
            sb.append('"');
        }
        sb.append('>');
    }


    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        sb.append('<');
        sb.append('/');
        sb.append(getName(localName, qName));
        sb.append('>');
    }


    public void characters(char[] ch, int start, int length) throws SAXException
    {
        for (int i = start; i < length; i++)
        {
            sb.append(ch[i]);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        for (int i = start; i < length; i++)
        {
            sb.append(ch[i]);
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException
    {
        sb.append("<!--");
        for (int i = start; i < length; i++)
        {
            sb.append(ch[i]);
        }
        sb.append("-->");
    }

    public void startCDATA() throws SAXException
    {
        sb.append("<![CDATA[");
    }

    public void endCDATA() throws SAXException
    {
        sb.append("]]>");
    }

    public void endDTD() throws SAXException
    {
    }

    public void endEntity(String string) throws SAXException
    {
        sb.length();
    }

    public void startEntity(String string) throws SAXException
    {
        sb.length();
    }

    public void startDTD(String string, String string1, String string2) throws SAXException
    {
    }
}
