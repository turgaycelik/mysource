package com.atlassian.jira.imports.project.handler;

import org.xml.sax.SAXException;

/**
 * A special extension of SAXException that is used to indicate to the {@link com.atlassian.jira.imports.project.handler.ChainedSaxHandler}
 * that it should abort its operation.
 * SAXException was extended because we need to throw this exception from a SAX handler.
 *
 * @since v3.13
 */
public class AbortImportException extends SAXException
{
    public AbortImportException()
    {
        // SaxException has no default constructor in 1.4
        super((String) null);
    }
}
