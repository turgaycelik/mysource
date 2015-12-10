package com.atlassian.jira.bc.dataimport;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.action.admin.export.EntitiesExporter;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.4
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultExportService
{
    @Mock
    public DelegatorInterface delegatorInterface;

    @Mock
    public EntitiesExporter entitiesExporter;

    @Mock
    public ModelReader modelReader;

    @Mock
    public EventPublisher eventPublisher;

    private final JiraProperties jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());

    @Before
    public void initMocks()
    {
        when(delegatorInterface.getModelReader()).thenReturn(modelReader);
    }

    @Test
    public void happyPathNoActiveObjects() throws Exception
    {
        when(modelReader.getEntityNames()).thenReturn(Collections.<String>emptyList());

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory(), eventPublisher, jiraProperties)
        {
            @Override
            protected Backup getActiveObjectsBackup()
            {
                return null;
            }
        };

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);

        assertTrue(outcome.isValid());
        assertFalse(outcome.getErrorCollection().hasAnyErrors());

        verify(eventPublisher, times(1)).publish(isA(ExportStartedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(ExportCompletedEvent.class));
    }

    @Test
    public void developmentExport() throws Exception
    {
        when(modelReader.getEntityNames()).thenReturn(Collections.<String>emptyList());

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory(), eventPublisher, jiraProperties)
        {
            @Override
            protected Backup getActiveObjectsBackup()
            {
                fail();
                return null;
            }
        };

        final ServiceOutcome<Void> outcome = exportService.exportForDevelopment(null, "filename", TaskProgressSink.NULL_SINK);

        assertTrue(outcome.isValid());
        assertFalse(outcome.getErrorCollection().hasAnyErrors());

        verify(eventPublisher, times(1)).publish(isA(ExportStartedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(ExportCompletedEvent.class));
    }

    @Test
    public void ioException() throws Exception
    {
        when(modelReader.getEntityNames()).thenReturn(Collections.<String>emptyList());

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory(), eventPublisher, jiraProperties)
        {
            @Override
            protected ZipArchiveOutputStream getZipOutputStream(String filename) throws IOException
            {
                throw new IOException("unit test exception");
            }
        };

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertFalse(outcome.isValid());
        assertNull(outcome.getReturnedValue());

        final ErrorCollection errorCollection = outcome.getErrorCollection();
        assertEquals(0, errorCollection.getReasons().size());

        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        final String message = errorMessages.iterator().next();
        assertEquals("Unable to save the backup file 'filename'.", message);

        verify(eventPublisher, times(1)).publish(isA(ExportStartedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(ExportCompletedEvent.class));
    }

    @Test
    public void invalidXml() throws Exception
    {
        // this special string triggers the "invalid XML" error code to try to give the user a better error message
        // for this common error case
        when(modelReader.getEntityNames()).thenThrow(new GenericEntityException("invalid XML character"));

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory(), eventPublisher, jiraProperties);

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertFalse(outcome.isValid());
        assertNull(outcome.getReturnedValue());

        final ErrorCollection errorCollection = outcome.getErrorCollection();
        assertEquals(1, errorCollection.getReasons().size());
        assertEquals(ErrorCollection.Reason.VALIDATION_FAILED, errorCollection.getReasons().iterator().next());

        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        final String message = errorMessages.iterator().next();
        assertEquals("Backup Data: Invalid XML characters", message);

        verify(eventPublisher, times(1)).publish(isA(ExportStartedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(ExportCompletedEvent.class));
    }

    @Test
    public void entityException() throws Exception
    {
        when(modelReader.getEntityNames()).thenThrow(new GenericEntityException("error message goes here"));

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory(), eventPublisher, jiraProperties);

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertFalse(outcome.isValid());
        assertNull(outcome.getReturnedValue());

        final ErrorCollection errorCollection = outcome.getErrorCollection();
        assertEquals(0, errorCollection.getReasons().size());
        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        final String message = errorMessages.iterator().next();
        assertEquals("Error exporting data: org.ofbiz.core.entity.GenericEntityException: error message goes here", message);

        verify(eventPublisher, times(1)).publish(isA(ExportStartedEvent.class));
        verify(eventPublisher, times(1)).publish(isA(ExportCompletedEvent.class));
    }
}
