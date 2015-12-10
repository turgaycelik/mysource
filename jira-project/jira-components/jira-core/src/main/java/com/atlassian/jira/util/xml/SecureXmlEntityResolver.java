package com.atlassian.jira.util.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * An EntityResolver that refuses to resolve references to external xml entities by unconditionally returning an
 * InputSource wrapping an empty InputStream.
 */
public final class SecureXmlEntityResolver implements EntityResolver
{
    public InputSource resolveEntity(final String publicId, final String systemId)
    {
        return new InputSource(new StringReader(""));
    }
}
