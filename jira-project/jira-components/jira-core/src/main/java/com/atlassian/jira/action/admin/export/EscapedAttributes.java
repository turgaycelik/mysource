package com.atlassian.jira.action.admin.export;

import com.atlassian.jira.imports.project.util.XMLEscapeUtil;
import org.xml.sax.Attributes;

/**
 * Delegates to original attributes but decodes attributes names and values encoded earlier with XMLEscapingReader
 *
 * @since v6.0
 */
public class EscapedAttributes implements Attributes
{
    final private Attributes delegate;

    public EscapedAttributes(final Attributes delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public String getValue(final String qName)
    {
        return XMLEscapeUtil.unicodeDecode(delegate.getValue(qName));
    }

    @Override
    public String getValue(final String uri, final String localName)
    {
        return XMLEscapeUtil.unicodeDecode(delegate.getValue(uri, localName));
    }

    @Override
    public String getType(final String qName)
    {
        return delegate.getType(qName);
    }

    @Override
    public String getType(final String uri, final String localName)
    {
        return delegate.getType(uri, localName);
    }

    @Override
    public int getIndex(final String qName)
    {
        return delegate.getIndex(qName);
    }

    @Override
    public int getIndex(final String uri, final String localName)
    {
        return delegate.getIndex(uri, localName);
    }

    @Override
    public String getValue(final int index)
    {
        return XMLEscapeUtil.unicodeDecode(delegate.getValue(index));
    }

    @Override
    public String getType(final int index)
    {
        return delegate.getType(index);
    }

    @Override
    public String getQName(final int index)
    {
        return XMLEscapeUtil.unicodeDecode(delegate.getQName(index));
    }

    @Override
    public String getLocalName(final int index)
    {
        return XMLEscapeUtil.unicodeDecode(delegate.getLocalName(index));
    }

    @Override
    public String getURI(final int index)
    {
        return delegate.getURI(index);
    }

    @Override
    public int getLength()
    {
        return delegate.getLength();
    }
}
