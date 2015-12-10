package com.atlassian.jira.imports.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

/**
 * Simple class that will parse an XML file from disk using SAX and use the provided handler to process the
 * SAX events. This class will also use the {@link com.atlassian.jira.config.properties.APKeys#JIRA_IMPORT_CLEAN_XML}
 * flag to determine if it should try to clean the XML of bad characters.
 *
 * @since v3.13
 */
public interface BackupXmlParser
{
    /**
     * Uses a SAXParser to parse the given file using the given SAX DefaultHandler.
     *
     * @param fileName Path name of the XML Backup file.
     * @param handler A SAX DefaultHandler to handle the SAX events.
     * @throws java.io.FileNotFoundException If the given file does not exist.
     * @throws IOException If on IO Exception occurs
     * @throws SAXException If any SAX errors occur during processing.
     */
    void parseBackupXml(final String fileName, final DefaultHandler handler) throws IOException, SAXException;
}
