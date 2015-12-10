package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressProcessor;
import com.atlassian.jira.imports.project.util.XMLEscapeUtil;
import com.atlassian.jira.util.xml.SecureXmlEntityResolver;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A base class used for processing a JIRA backup file. This collects Entity information and calls the {@link
 * com.atlassian.jira.imports.project.handler.ImportEntityHandler#handleEntity(String, java.util.Map)} method on its
 * registered delegates. The attributes map includes any attributes that may be nested as sub-elements. <br/> If you
 * construct this with a {@link com.atlassian.jira.task.TaskProgressSink} then the progress of the XML processing will
 * be relayed.
 *
 * @since v3.13
 */
public class ChainedSaxHandler extends DefaultHandler
{
    private static final Logger log = Logger.getLogger(ChainedSaxHandler.class);
    private static final String ENTITY_ENGINE_XML = "entity-engine-xml";
    public static final SecureXmlEntityResolver EMPTY_ENTITY_RESOLVER = new SecureXmlEntityResolver();

    private Map<String, String> attributesMap;
    private String inEntity = null;
    private StringBuffer textBuffer;
    private boolean hasRootElement = false;
    private long entityCount;
    private int entityTypeCount;
    private String lastTopLevelElementName = null;
    private long currentEntityCount;

    final Collection<ImportEntityHandler> delegateHandlers = new ArrayList<ImportEntityHandler>();
    private final TaskProgressProcessor taskProgressProcessor;

    public void registerHandler(final ImportEntityHandler handler)
    {
        delegateHandlers.add(handler);
    }

    /**
     * Simple constructor for creating an AbstractHandler without progress feedback.
     */
    public ChainedSaxHandler()
    {
        taskProgressProcessor = null;
    }

    /**
     * Constructor to create an AbstractHandler with progress feedback.
     *
     * @param taskProgressProcessor the TaskProgressProcessor that relays the progress information.
     */
    public ChainedSaxHandler(final TaskProgressProcessor taskProgressProcessor)
    {
        this.taskProgressProcessor = taskProgressProcessor;
    }

    /**
     * Provides the number of actual XML elements that the parser encounters.
     *
     * @return number of actual XML elements the parser encounters
     */
    public long getEntityCount()
    {
        return entityCount;
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
    {
        return EMPTY_ENTITY_RESOLVER.resolveEntity(publicId, systemId);
    }

    public final void startDocument() throws SAXException
    {
        log.debug("Starting parsing Document with ChainedSaxHandler.");
        entityCount = 0;
        // Call startDocument() on the registered ImportEntityHandlers
        for (final ImportEntityHandler importEntityHandler : delegateHandlers)
        {
            importEntityHandler.startDocument();
        }
        processTaskProgress("Start");
    }

    public final void endDocument() throws SAXException
    {
        if (hasRootElement)
        {
            throw new SAXException("XML file ended too early.  There was no </entity-engine-xml> tag.");
        }
        // Call endDocument() on the registered ImportEntityHandlers
        for (final ImportEntityHandler importEntityHandler : delegateHandlers)
        {
            importEntityHandler.endDocument();
        }
        log.debug("Ended parsing Document with ChainedSaxHandler.");
    }

    public final void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        if (hasRootElement)
        {
            startElement(qName, attributes);
        }
        // When we encounter the root element we should record it
        else if (ENTITY_ENGINE_XML.equals(qName))
        {
            // Set that the document has started correctly
            hasRootElement = true;
        }
        else
        {
            throw new SAXException("The XML document does not contain the <entity-engine-xml> root element or it was closed too early.");
        }
    }

    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        // Only process the endElement if we have a root element
        if (hasRootElement)
        {
            // Are we closing the root element?
            if (ENTITY_ENGINE_XML.equals(qName))
            {
                hasRootElement = false;
            }
            else
            {
                // We should never get into this state
                if (inEntity == null)
                {
                    throw new SAXException("There is no entity set");
                }
                endElement(qName);
            }
        }
        else
        {
            throw new SAXException("How did we get here an exception should already have been thrown");
        }
    }

    public final void characters(final char ch[], final int start, final int length) throws SAXException
    {
        final String s =  XMLEscapeUtil.unicodeDecode(new String(ch, start, length));
        if (textBuffer == null)
        {
            textBuffer = new StringBuffer(s);
        }
        else
        {
            textBuffer.append(s);
        }
    }

    private void startElement(final String qName, final Attributes attributes)
    {
        if (inEntity == null)
        {
            // When we encounter an element we want to create a map of its attributes and record that we are
            // currently inside that entity
            inEntity = qName;
            attributesMap = convertAttributesToMap(attributes);
        }
        else
        {
            // If we are not in a entity then we want to clear out the text buffer so that it can be set with
            // the internal contents of the nested element.
            textBuffer = null;
        }
    }

    private void endElement(final String qName) throws SAXException
    {
        // If the inEntity is the same as the current end element name then we must be ending a top level
        // element (i.e. an element that corresponds to an Entity name, like Issue).
        if (inEntity.equals(qName))
        {
            endTopLevelElement(qName);
        }
        // Otherwise we are in a nested element and we need to add that elements contents to the current
        // attributes map
        else
        {
            endNestedElement(qName);
        }
    }

    /**
     * Examine the textBuffer set by {@link #characters(char[], int, int)} and add it to the attributes map for the
     * current inEntity entity.
     *
     * @param qName identifies the name of the attribute we are examining
     */
    private void endNestedElement(final String qName)
    {
        if (textBuffer != null)
        {
            attributesMap.put(qName, textBuffer.toString());
            textBuffer = null;
        }
    }

    /**
     * Closes the top level element (i.e. Issue). This signals that we have filled the attributesMap with all attributes
     * for this entity. This method calls the delegates {@link com.atlassian.jira.imports.project.handler.ImportEntityHandler#handleEntity(String,
     * java.util.Map)} methods with the collected information and then clears the state objects.
     *
     * @param qName identifies the name of the top level element we are closing.
     * @throws SAXException if a {@link org.apache.xerces.impl.xpath.regex.ParseException} is caused by calling
     * handleEntity
     */
    private void endTopLevelElement(final String qName) throws SAXException
    {
        inEntity = null;
        try
        {
            // Counts all entities found
            entityCount++;
            // Records how many of qName entities we have encountered
            currentEntityCount++;
            // If we are not the same as the last block of top-level entities then we have some counts to update
            if (!qName.equals(lastTopLevelElementName))
            {
                lastTopLevelElementName = qName;
                entityTypeCount++;
                currentEntityCount = 0;
            }
            processTaskProgress(qName);

            // Call handleEntity() on the registered ImportEntityHandlers
            for (final ImportEntityHandler importEntityHandler : delegateHandlers)
            {
                importEntityHandler.handleEntity(qName, attributesMap);
            }
        }
        catch (final ParseException e)
        {
            log.warn("Encountered a parsing exception.", e);
            throw new SAXException(e);
        }
    }

    /**
     * Calculates, if a task progress sink is registered, the progress of processing the XML file.
     *
     * @param qName the current top-level entity being processed
     */
    private void processTaskProgress(final String qName)
    {
        // We only want to record progress if we have a sink and we want to do it every 100 entities encountered
        if ((taskProgressProcessor != null) && callMakeProgress())
        {
            taskProgressProcessor.processTaskProgress(qName, entityTypeCount, entityCount, currentEntityCount);
        }
    }

    private Map<String, String> convertAttributesToMap(final Attributes attributes)
    {
        final Map<String, String> attMap = new HashMap<String, String>();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            //decode escaped characters; earlier it should have been read through XMLEscapingReader
            final String attName = XMLEscapeUtil.unicodeDecode(attributes.getQName(i));
            final String value = XMLEscapeUtil.unicodeDecode(attributes.getValue(i));
            attMap.put(attName, value);
        }
        return attMap;
    }

    boolean callMakeProgress()
    {
        return entityCount % 100 == 0;
    }

}
