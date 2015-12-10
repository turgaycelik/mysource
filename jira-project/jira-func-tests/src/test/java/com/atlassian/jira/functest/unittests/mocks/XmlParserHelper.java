package com.atlassian.jira.functest.unittests.mocks;

import com.atlassian.jira.util.DomFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A helper class to parse and create XML objects
 *
 * @since v3.13
 */
public class XmlParserHelper
{
    public static Document parseXml(String xmlText) throws ParserConfigurationException, IOException, SAXException
    {
        InputSource inputSource = new InputSource(new StringReader(xmlText));
        // Create the builder and parse the file
        return DomFactory.createDocumentBuilder().parse(inputSource);
    }
}
