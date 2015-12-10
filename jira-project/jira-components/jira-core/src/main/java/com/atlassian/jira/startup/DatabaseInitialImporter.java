package com.atlassian.jira.startup;

import com.atlassian.core.util.FileUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.task.TaskProgressSink;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Loads a startup set ot data into the JIRA database
 *
 * @since v6.0
 */
public class DatabaseInitialImporter
{
    private static final Logger log = Logger.getLogger(DatabaseInitialImporter.class);

    private static final String STARTUP_XML = "startupdatabase.xml";
    private static final String STARTUP_XML_RESOURCE = "/" + STARTUP_XML;
    private static final String PERMISSION_SCHEME_ENTITY_NAME = "PermissionScheme";

    public DatabaseInitialImporter()
    {
    }

    public boolean dataAlreadyLoaded()
    {
        // JIRA will as a very minimum always have at least one (default) permission scheme
        DelegatorInterface delegator = ComponentAccessor.getOfBizDelegator().getDelegatorInterface();
        try
        {
            return delegator.countAll(PERMISSION_SCHEME_ENTITY_NAME) > 0;
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void importInitialData(User loggedInUser)
    {
        DataImportService dataImportService =  ComponentAccessor.getComponent(DataImportService.class);

        final DataImportService.ImportValidationResult result = dataImportService.validateImport(loggedInUser, buildDataImportParameters());
        DataImportService.ImportResult importResult = dataImportService.doImport(loggedInUser, result, TaskProgressSink.NULL_SINK);
        if (!importResult.isValid())
        {
            log.error(importResult.getSpecificErrorMessage());
            for (String error : importResult.getErrorCollection().getErrorMessages())
            {
                log.error(error);
            }
            for (String error : importResult.getErrorCollection().getErrors().values())
            {
                log.error(error);
            }
        }
    }

    private DataImportParams buildDataImportParameters()
    {
        JiraHome jiraHome = ComponentAccessor.getComponent(JiraHome.class);
        File importFile = new File(jiraHome.getImportDirectory(), STARTUP_XML);
        InputStream dataResource = DatabaseInitialImporter.class.getResourceAsStream(STARTUP_XML_RESOURCE);
        try
        {
            FileUtils.copyFile(dataResource, importFile, true);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(dataResource);
        }
        final DataImportParams.Builder builder = new DataImportParams.Builder(importFile.getPath().toString()).
                setUseDefaultPaths(true).
                setAllowDowngrade(false).
                setupImport().
                setStartupDataOnly().
                setOutgoingEmailTo(true).
                setNoLicenseCheck().
                setQuickImport(true).
                setUnsafeJiraBackup(importFile);

        return builder.build();
    }

}
