package com.atlassian.jira.action.admin.export;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.SortedSet;

public interface EntitiesExporter
{
    /**
     * Exports the entities in the TreeSet to the outputStream.
     *
     * @param outputStream - the stream to write to
     * @param entityNames - the entities names to write out
     * @param entityWriter - an XML output helper that can write generic values to XML
     * @param exportingUser - the user performing the export operation
     * @return the number of individual entities written
     */
    long exportEntities(OutputStream outputStream, SortedSet<String> entityNames, EntityXmlWriter entityWriter, User exportingUser)
            throws IOException, GenericEntityException;
}
