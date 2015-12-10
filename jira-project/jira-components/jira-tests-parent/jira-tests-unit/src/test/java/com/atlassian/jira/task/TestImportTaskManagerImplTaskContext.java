package com.atlassian.jira.task;

import com.atlassian.jira.imports.project.ProjectImportTaskContext;

import org.junit.Test;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;
import static org.junit.Assert.assertNotSame;

/**
 * ProjectImportTaskContext should be serializable
 *
 * @since v6.3
 */
public class TestImportTaskManagerImplTaskContext
{
    @Test
    public void instancesShouldBeSerializable()
    {
        final ImportTaskManagerImpl.NoOpTaskContext context = new ImportTaskManagerImpl.NoOpTaskContext();
        // Invoke
        final ImportTaskManagerImpl.NoOpTaskContext roundTrippedEvent = (ImportTaskManagerImpl.NoOpTaskContext) deserialize(serialize(context));

        // Equals is not implemented so we just assert we got something new.
        assertNotSame(context, roundTrippedEvent);
    }

}
