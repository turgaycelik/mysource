package com.atlassian.jira.action.admin.export;

import org.ofbiz.core.entity.GenericValue;

import java.io.PrintWriter;

/**
 * A writer that performs no transformations before writing the GenericValue to XML
 */
public class DefaultEntityXmlWriter implements EntityXmlWriter
{
    public void writeXmlText(GenericValue entity, PrintWriter writer)
    {
        entity.writeXmlText(writer, "");
    }
}
