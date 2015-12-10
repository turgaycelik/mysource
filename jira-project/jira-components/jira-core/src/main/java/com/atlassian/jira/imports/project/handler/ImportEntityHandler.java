package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;

import java.util.Map;

/**
 * Defines a handler class that will be able to perform some operation given an entity name and the entities
 * attributes. This is meant to be used by registering an instance of this with
 * {@link com.atlassian.jira.imports.project.handler.ChainedSaxHandler#registerHandler(ImportEntityHandler)}.
 *
 * @since v3.13
 */
public interface ImportEntityHandler
{
    /**
     * This is the main method to implement when using this ImportEntityHandler. This method will provide the
     * entity name and a complete map of attribute key/value pairs. This includes any nested element tags that
     * will have CDATA bodies.
     *
     * @param entityName identifies the entity (i.e. Issue)
     * @param attributes complete list of the attributes listed in the XML element including the nested
     *                   elements.
     * @throws com.atlassian.jira.exception.ParseException if the entity is invalid a ParseException will be thrown.
     * @throws AbortImportException to indicate to the {@link com.atlassian.jira.imports.project.handler.ChainedSaxHandler}
     *          that it should abort its SAX parsing.
     */
    void handleEntity(String entityName, Map<String, String> attributes) throws ParseException, AbortImportException;

    /**
     * Provides the implementation an opportunity to perform some action when the document is starting to
     * be read.
     */
    void startDocument();

    /**
     * Provides the implementation an opportunity to perform some action when the document is finished being read.
     */
    void endDocument();

}
