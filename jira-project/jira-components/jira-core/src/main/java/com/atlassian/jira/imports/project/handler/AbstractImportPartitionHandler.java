package com.atlassian.jira.imports.project.handler;

import java.io.PrintWriter;

import com.atlassian.jira.util.dbc.Null;

import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Abstract base class that will print "<entity-engine-xml>" open and closing tags at the begining and
 * end of handling a document.
 *
 * @since v3.13
 */
public abstract class AbstractImportPartitionHandler implements ImportEntityHandler
{
    protected final GenericDelegator delegator;
    private final PrintWriter printWriter;
    private final String encoding;

    public AbstractImportPartitionHandler(
            final PrintWriter printWriter, final String encoding, final DelegatorInterface delegatorInterface)
    {
        this.printWriter = printWriter;
        this.encoding = encoding;
        this.delegator = GenericDelegator.getGenericDelegator(delegatorInterface.getDelegatorName());
    }

    public void startDocument()
    {
        printWriter.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>");
        printWriter.println("<entity-engine-xml>");
    }

    public void endDocument()
    {
        printWriter.print("</entity-engine-xml>");
    }

    public void assertModelEntityForName(final ModelEntity modelEntity, final String expectedName)
    {
        Null.not("modelEntity", modelEntity);
        Null.not("expectedName", expectedName);
        if (!expectedName.equals(modelEntity.getEntityName()))
        {
            throw new IllegalArgumentException("This handler must only be created with a " + expectedName + " model entity");
        }
    }

    public String getEncoding()
    {
        return encoding;
    }
}
