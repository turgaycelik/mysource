package com.atlassian.jira.imports.project;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertNotSame;

/**
 * ProjectImportTaskContext should be serializable
 *
 * @since v6.3
 */
public class TestProjectImportTaskContext
{
    @Test
    public void instancesShouldBeSerializable()
    {
        final ProjectImportTaskContext context = new ProjectImportTaskContext();
        // Invoke
        final ProjectImportTaskContext roundTrippedEvent = (ProjectImportTaskContext) deserialize(serialize(context));

        // Equals is not implemented so we just assert we got something new.
        assertNotSame(context, roundTrippedEvent);
    }

}
