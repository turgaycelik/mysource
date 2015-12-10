package com.atlassian.jira.web.action.util;

import com.atlassian.core.util.DataUtils;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.dataimport.ExportService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.action.setup.DevModeSecretSauce;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

@SuppressWarnings ( { "UnusedDeclaration" })
@WebSudoRequired
public class XmlBackup extends ProjectActionSupport
{
    private static final String CONFIRM = "confirm";
    private static final String FIX_INVALID_XML_CHARACTERS = "fixchars";

    private String filename;
    private String destinationFile;
    boolean confirm = false;
    private final DevModeSecretSauce devModeSecretSauce;
    private final ExportService exportService;

    public XmlBackup(final ExportService exportService, final JiraProperties jiraSystemProperties)
    {
        this.exportService = exportService;
        this.devModeSecretSauce = new DevModeSecretSauce(jiraSystemProperties);
    }

    protected void doValidation()
    {
        if (StringUtils.isBlank(filename))
        {
            addError("filename", getText("admin.errors.export.must.enter.file.location"));
        }
        else
        {
            File safeBackupPath = getSafeBackupPath();
            // Check if we can get the canonical path, this proves that the filename
            // is valid and does not contain invalid characters (e.g. a colon character
            // on Windows files systems)

            // Created from http://jira.atlassian.com/browse/JRA-14686.
            try
            {
                //noinspection ResultOfMethodCallIgnored
                new File(safeBackupPath, filename).getCanonicalFile();
            }
            catch (IOException e)
            {
                // filename is invalid
                addError("filename", getText("admin.errors.export.file.invalid", filename));
            }
        }
    }


    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            if (TextUtils.stringSet(filename))
            {
                if (devModeSecretSauce.isBoneFideJiraDeveloper())
                {
                    filename = DataUtils.getXmlFilename(filename.trim());
                }
                else
                {
                    filename = DataUtils.getZipFilename(filename.trim());
                }
                File safeBackupPath = getSafeBackupPath();

                File potentialFile = new File(filename);
                File potentiaSafeFile = new File(safeBackupPath, potentialFile.getName());

                // we can go direct to a path if we have the secret sauce
                if (devModeSecretSauce.isBoneFideJiraDeveloper()  && potentialFile.isAbsolute())
                {
                    potentiaSafeFile = potentialFile;
                }

                destinationFile = potentiaSafeFile.getAbsoluteFile().toString();

                //if the file already exists, and we have not confirmed the over-write, then redirect to a confirmation page.
                if (potentiaSafeFile.exists())
                {
                    if (!potentiaSafeFile.canWrite())
                    {
                        addError("filename", getText("admin.errors.export.file.exists.unwriteable", "'" + destinationFile + "'"));
                        return getResult();
                    }
                    else if (!confirm)
                    {
                        return CONFIRM;
                    }
                }
                // ok use that file name!
                filename = destinationFile;
                log.warn(String.format("The filename that will be used for exporting is: '%s'", filename));
            }

            ServiceOutcome<Void> outcome = null;
            if (devModeSecretSauce.isBoneFideJiraDeveloper())
            {
                outcome = exportService.exportForDevelopment(getLoggedInUser(), filename, TaskProgressSink.NULL_SINK);
            }
            else
            {
                outcome = exportService.export(getLoggedInUser(), filename, TaskProgressSink.NULL_SINK);
            }
            if (outcome.isValid())
            {
                return SUCCESS;
            }
            else
            {
                addErrors(outcome.getErrorCollection().getErrors());
                addErrorMessages(outcome.getErrorCollection().getErrorMessages());
                if (outcome.getErrorCollection().getReasons().contains(Reason.VALIDATION_FAILED))
                {
                    return FIX_INVALID_XML_CHARACTERS;
                }
                else
                {
                    return ERROR;
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception occurred backing up: " + e, e);
            addErrorMessage(TextUtils.plainTextToHtml(getText("admin.errors.export.exception.occured.backing.up", e)));
            return ERROR;
        }
    }

    public String doFixChars() throws Exception
    {
        OfBizDelegator ofBizDelegator = ComponentAccessor.getComponent(OfBizDelegator.class);
        ApplicationProperties applicationProperties = ComponentAccessor.getComponent(ApplicationProperties.class);
        DataCleaner dataCleaner = new DataCleaner(applicationProperties, ofBizDelegator);
        dataCleaner.clean();
        return INPUT;
    }

    public File getSafeBackupPath()
    {
        return new File(jiraHome().getHome(), JiraHome.EXPORT).getAbsoluteFile();
    }

    JiraHome jiraHome()
    {
        return ComponentAccessor.getComponentOfType(JiraHome.class);
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        if (TextUtils.stringSet(filename))
        {
            this.filename = filename;
        }
    }

    public String getDestinationFile()
    {
        return destinationFile;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }
}
