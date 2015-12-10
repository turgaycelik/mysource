package com.atlassian.jira.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @since v6.0
 */
public final class DomFactory
{
    private DomFactory() {}

    public static DocumentBuilderFactory createDocumentBuilderFactory()
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory;
    }

    public static DocumentBuilder createDocumentBuilder()
    {
        try
        {
            return createDocumentBuilderFactory().newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
