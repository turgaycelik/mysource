package com.atlassian.jira.bc.dataimport;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TreeSet;

import com.atlassian.activeobjects.spi.ActiveObjectsImportExportException;
import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.activeobjects.spi.NullBackupProgressMonitor;
import com.atlassian.core.util.DataUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.action.admin.export.EntitiesExporter;
import com.atlassian.jira.action.admin.export.EntityXmlWriter;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelReader;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;

/**
 * @since v4.4
 */
public class DefaultExportService implements ExportService
{
    static private final Logger LOG = Logger.getLogger(DefaultExportService.class);
    static public final String ACTIVEOBJECTS_XML = "activeobjects.xml";
    static public final String ENTITIES_XML = "entities.xml";

    private final DelegatorInterface genericDelegator;
    private final EntitiesExporter entitiesExporter;
    private final I18nHelper.BeanFactory i18nFactory;
    private final EventPublisher eventPublisher;
    private final JiraProperties jiraSystemProperties;

    public DefaultExportService(final DelegatorInterface genericDelegator,
            final EntitiesExporter entitiesExporter,
            I18nHelper.BeanFactory i18nFactory,
            final EventPublisher eventPublisher,
            final JiraProperties jiraSystemProperties)
    {
        this.genericDelegator = genericDelegator;
        this.entitiesExporter = entitiesExporter;
        this.i18nFactory = i18nFactory;
        this.eventPublisher = eventPublisher;
        this.jiraSystemProperties = jiraSystemProperties;
    }

    @Override
    public ServiceOutcome<Void> export(User loggedInUser, String filename, TaskProgressSink taskProgressSink)
    {
        return export(loggedInUser, filename, ExportService.Style.NORMAL, taskProgressSink);
    }

    @Override
    public ServiceOutcome<Void> export(User loggedInUser, String filename, Style style, TaskProgressSink taskProgressSink)
    {
        final long xmlExportTime = System.currentTimeMillis();
        eventPublisher.publish(new ExportStartedEvent(loggedInUser, filename, xmlExportTime));
        final I18nHelper i18n = i18nFactory.getInstance(loggedInUser);

        ZipArchiveOutputStream zip = null;
        try
        {
            zip = getZipOutputStream(filename);
            try
            {
                zip.setUseZip64(Zip64Mode.AsNeeded);
                zip.putArchiveEntry(new ZipArchiveEntry(ENTITIES_XML));
                exportJIRA(loggedInUser, style, zip);
                zip.closeArchiveEntry();

                zip.putArchiveEntry(new ZipArchiveEntry(ACTIVEOBJECTS_XML));
                exportActiveObjects(zip);
                zip.closeArchiveEntry();

                final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.ok(null);
                eventPublisher.publish(new ExportCompletedEvent(loggedInUser, filename, outcome, xmlExportTime));
                return outcome;
            }
            catch (GenericEntityException e)
            {
                if ((e.getMessage() != null) && e.getMessage().contains("invalid XML character"))
                {
                    final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
                    errorCollection.addErrorMessage(i18n.getText("admin.export.backup.data.invalid.characters"), ErrorCollection.Reason.VALIDATION_FAILED);
                    final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.from(errorCollection, null);
                    eventPublisher.publish(new ExportCompletedEvent(loggedInUser, filename, outcome, xmlExportTime));
                    return outcome;
                }
                else
                {
                    LOG.error("Error during XML backup.", e);
                    final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.error(i18n.getText("admin.errors.dataexport.error.exporting.data", e));
                    eventPublisher.publish(new ExportCompletedEvent(loggedInUser, filename, outcome, xmlExportTime));
                    return outcome;
                }
            }
            catch (ActiveObjectsImportExportException e)
            {
                LOG.error("Error during Active Objects Backup",e);
                final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.error(i18n.getText("admin.export.backup.activeobjects.exception", e.getPluginInformation().getPluginName()));
                eventPublisher.publish(new ExportCompletedEvent(loggedInUser, filename, outcome, xmlExportTime));
                return outcome;
            }
        }
        catch (IOException e)
        {
            LOG.error("Error during XML backup.", e);
            final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.error(i18n.getText("admin.errors.export.ioerror", filename));
            eventPublisher.publish(new ExportCompletedEvent(loggedInUser, filename, outcome, xmlExportTime));
            return outcome;
        }
        finally
        {
            IOUtils.closeQuietly(zip);
        }
    }

    @Override
    public ServiceOutcome<Void> exportForDevelopment(User loggedInUser, String xmlFilename, TaskProgressSink taskProgressSink)
    {
        Style style = ExportService.Style.NORMAL;
        final I18nHelper i18n = i18nFactory.getInstance(loggedInUser);
        final long xmlExportTime = System.currentTimeMillis();
        eventPublisher.publish(new ExportStartedEvent(loggedInUser, xmlFilename, xmlExportTime));

        OutputStream xml = null;
        try
        {
            xml = getXmlOutputStream(xmlFilename);

            try
            {
                exportJIRA(loggedInUser, style, xml);

                final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.ok(null);
                eventPublisher.publish(new ExportCompletedEvent(loggedInUser, xmlFilename, outcome, xmlExportTime));
                return outcome;
            }
            catch (GenericEntityException e)
            {
                if ((e.getMessage() != null) && e.getMessage().contains("invalid XML character"))
                {
                    final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
                    errorCollection.addErrorMessage(i18n.getText("admin.export.backup.data.invalid.characters"), ErrorCollection.Reason.VALIDATION_FAILED);

                    final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.from(errorCollection, null);
                    eventPublisher.publish(new ExportCompletedEvent(loggedInUser, xmlFilename, outcome, xmlExportTime));
                    return outcome;
                }
                else
                {
                    final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.error(i18n.getText("admin.errors.dataexport.error.exporting.data", e));
                    eventPublisher.publish(new ExportCompletedEvent(loggedInUser, xmlFilename, outcome, xmlExportTime));
                    return outcome;
                }
            }
        }
        catch (IOException e)
        {
            LOG.error("Error during XML backup.", e);
            final ServiceOutcomeImpl<Void> outcome = ServiceOutcomeImpl.error(i18n.getText("admin.errors.export.ioerror", xmlFilename));
            eventPublisher.publish(new ExportCompletedEvent(loggedInUser, xmlFilename, outcome, xmlExportTime));
            return outcome;
        }
        finally
        {
            IOUtils.closeQuietly(xml);
        }
    }

    private void exportJIRA(User loggedInUser, Style style, OutputStream out)
            throws GenericEntityException, IOException
    {
        final TreeSet<String> entityNames = entitiesToExport();

        final int numberOfEntities = entityNames.size();
        LOG.debug("numberOfEntities = " + numberOfEntities);

        final EntityXmlWriter entityWriter = style.getEntityXmlWriter();

        final long start = System.currentTimeMillis();
        final long entitiesWritten = entitiesExporter.exportEntities(out, entityNames, entityWriter, loggedInUser);
        LOG.info("Data export completed in " + (System.currentTimeMillis() - start) + "ms. Wrote " + entitiesWritten + " entities to export in memory.");
    }

    private void exportActiveObjects(OutputStream out) throws IOException
    {
        final Backup activeObjects = getActiveObjectsBackup();
        if (activeObjects == null)
        {
            // we don't want this to stop all JIRA backups.
            LOG.error("Could not find ActiveObjects in OSGi fairy land. Plugins using ActiveObjects have not been backed up.");
        }
        else
        {
            LOG.info("Attempting to save the Active Objects Backup");
            try
            {
                activeObjects.save(out, NullBackupProgressMonitor.INSTANCE);
            }
            catch (NoSuchMethodError ex)
            {
                // JRADEV-5986: Sounds like we are on an old version of Java.
                final String javaRuntimeVersion = jiraSystemProperties.getProperty("java.runtime.version");
                final String message = "Error exporting Active Objects. You must run JRE 1.6_18 or higher. java.runtime.version: " + javaRuntimeVersion;
                LOG.error(message, ex);
                throw new NoSuchMethodError(message);
            }
            LOG.info("Finished saving the Active Objects Backup");
        }
    }

    protected Backup getActiveObjectsBackup()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(Backup.class);
    }

    protected ZipArchiveOutputStream getZipOutputStream(final String filename) throws IOException
    {
        final String zipFileName = DataUtils.getZipFilename(filename);
        final File zipFile = new File(zipFileName);
        // JRADEV-14354 The ZAOS will ignore Zip64Mode.AsNeeded (the default) unless you
        // let it open the file itself.  Doing an open/close with FileUtils so that the
        // error messages won't change, but letting ZAOS work on the file directly after
        // that.
        FileUtils.openOutputStream(zipFile).close();
        return new ZipArchiveOutputStream(zipFile);
    }

    protected OutputStream getXmlOutputStream(final String filename) throws IOException
    {
        final String xmlFileName = DataUtils.getXmlFilename(filename);
        return FileUtils.openOutputStream(new File(xmlFileName));
    }

    private TreeSet<String> entitiesToExport() throws GenericEntityException
    {
        final ModelReader reader = genericDelegator.getModelReader();
        return newTreeSet(difference(newHashSet(reader.getEntityNames()), EntityImportExportExclusions.ENTITIES_EXCLUDED_FROM_IMPORT_EXPORT));
    }
}
