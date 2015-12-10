/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.util;

import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.AlreadyExecutingException;
import com.atlassian.jira.task.ImportTaskManager;
import com.atlassian.jira.task.ImportTaskManagerImpl;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextKeys;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.atlassian.jira.web.action.setup.DataImportAsyncCommand;
import com.atlassian.jira.web.servletcontext.ServletContextReference;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableMap;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@WebSudoRequired
public class ImportAll extends AbstractSetupAction
{
    private static final String PROGRESS_URL = "/importprogress";
    private static ServletContextReference<ImportTaskManager> taskManagerReference =
            new ServletContextReference<ImportTaskManager>(ServletContextKeys.DATA_IMPORT_TASK_MANAGER);

    private final IndexPathManager indexPathManager;
    private final JiraHome jiraHome;
    private final DataImportService dataImportService;
    private final ImportResultHandler importResultHandler;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final MailSettings mailSettings;

    private boolean quickImport;
    private String filename;
    private String licenseString;
    private boolean useDefaultPaths;
    private boolean outgoingEmail;
    private boolean safeMode;
    private boolean downgradeAnyway;
    private ErrorCollection specificErrors;

    public ImportAll(final IndexPathManager indexPathManager, final JiraHome jiraHome, FileFactory fileFactory,
            final DataImportService dataImportService, final ImportResultHandler importResultHandler,
            final VelocityRequestContextFactory velocityRequestContextFactory, final MailSettings mailSettings)
    {
        super(fileFactory);
        this.dataImportService = dataImportService;
        this.importResultHandler = importResultHandler;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.mailSettings = mailSettings;
        this.indexPathManager = notNull("indexPathManager", indexPathManager);
        this.jiraHome = notNull("jiraHome", jiraHome);
    }

    @Override
    public String doDefault() throws Exception
    {
        //if we come to this page explicitly clear away any previous import result.
        ActionContext.getSession().remove(SessionKeys.DATA_IMPORT_RESULT);
        outgoingEmail = mailSettings.send().isEnabled();
        return super.doDefault();
    }

    protected void doValidation()
    {
        final DataImportParams params =
                buildDataImportParameters();
        final DataImportService.ImportValidationResult result = dataImportService.validateImport(getLoggedInUser(), params);
        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
    }

    public String doFinish() throws Exception
    {
        final DataImportService.ImportResult lastResult = (DataImportService.ImportResult) ActionContext.getSession().get(SessionKeys.DATA_IMPORT_RESULT);
        //if we got an invalid importresult handle errors!
        if (lastResult != null && !lastResult.isValid())
        {
            try
            {
                filename = lastResult.getParams().getFilename();
                useDefaultPaths = lastResult.getParams().isUseDefaultPaths();
                quickImport = lastResult.getParams().isQuickImport();
                licenseString = lastResult.getParams().getLicenseString();

                addErrorCollection(lastResult.getErrorCollection());

                specificErrors = new SimpleErrorCollection();

                if (importResultHandler.handleErrorResult(ActionContext.getServletContext(), lastResult, this, specificErrors))
                {
                    return getRedirect(JohnsonConfig.getInstance().getErrorPath());
                }
            }
            finally
            {
                ActionContext.getSession().remove(SessionKeys.DATA_IMPORT_RESULT);
            }
            return ERROR;
        }
        return getRedirect("/secure/admin/XmlRestore!default.jspa");
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (taskManagerReference.get() != null && taskManagerReference.get().getTask() != null)
        {
            //looks like we already have a taskmanager. Let's see if there's any progress to report on.
            return getRedirect(getProgressUrl());
        }
        taskManagerReference.set(new ImportTaskManagerImpl());
        log.info("Running ImportAll");

        final JohnsonEventContainer eventCont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        try
        {
            final DataImportParams params = buildDataImportParameters();

            final DataImportService.ImportValidationResult result = dataImportService.validateImport(getLoggedInUser(), params);
            final DataImportAsyncCommand importCallable = new DataImportAsyncCommand
                    (
                            eventCont, dataImportService, getLoggedInUser(), result,
                            new Event
                                    (
                                            EventType.get("import"), "JIRA is currently being restored from backup",
                                            EventLevel.get(EventLevel.WARNING)
                                    ),
                            velocityRequestContextFactory.getJiraVelocityRequestContext(),
                            ActionContext.getRequest().getSession(false)
                    );

            Locale locale = getComponentInstanceOfType(JiraAuthenticationContext.class).getLocale();
            taskManagerReference.get().submitTask(importCallable, getText("admin.import.restore.jira.data.from.backup"));
            taskManagerReference.get().prepareCachedResourceBundleStrings(locale);
            return getRedirect(getProgressUrl());
        }
        catch (final AlreadyExecutingException e)
        {
            return getRedirect(getProgressUrl());
        }
        catch (final RejectedExecutionException e)
        {
            addErrorMessage(getText("common.tasks.rejected.execution.exception", e.getMessage()));
            return ERROR;
        }
    }

    private DataImportParams buildDataImportParameters()
    {
        final DataImportParams.Builder builder = new DataImportParams.Builder(filename).
                setLicenseString(licenseString).
                setUseDefaultPaths(useDefaultPaths).
                setQuickImport(quickImport).
                setAllowDowngrade(downgradeAnyway);

        if (isOutgoingMailModifiable())
        {
            builder.setOutgoingEmailTo(outgoingEmail);
        }
        if (safeMode)
        {
            builder.setSafeMode();
        }
        return builder.build();
    }

    private String getProgressUrl()
    {
        return PROGRESS_URL + "?locale=" + getLocale().toString();
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(final String filename)
    {
        this.filename = filename;
    }

    /**
     * Returns the absolute path for the Default Index directory ([jira-home]/caches/index/). This is used for read-only
     * info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Index directory ([jira-home]/caches/index/)
     */
    public String getDefaultIndexPath()
    {
        return indexPathManager.getDefaultIndexRootPath();
    }

    /**
     * Returns the absolution path for the Default Import directory ([jira-home/import]).
     *
     * @return the absolute path for the Default Import directory ([jira-home/import])
     */
    public String getDefaultImportPath()
    {
        return jiraHome.getImportDirectory().getAbsolutePath();
    }

    public String getLicense()
    {
        return licenseString;
    }

    public void setLicense(final String licenseString)
    {
        this.licenseString = licenseString;
    }

    public void setQuickImport(final boolean quickImport)
    {
        this.quickImport = quickImport;
    }

    public boolean isUseDefaultPaths()
    {
        return useDefaultPaths;
    }

    public void setUseDefaultPaths(final boolean useDefaultPaths)
    {
        this.useDefaultPaths = useDefaultPaths;
    }

    public boolean hasSpecificErrors()
    {
        return specificErrors != null && specificErrors.hasAnyErrors();
    }

    public ErrorCollection getSpecificErrors()
    {
        return specificErrors;
    }

    public boolean isOutgoingMailModifiable()
    {
        return mailSettings.send().isModifiable();
    }

    public Map<String, String> getOutgoingMailOptions()
    {
        return ImmutableMap.of("true", "Enable", "false", "Disable");
    }

    public void setOutgoingEmail(boolean outgoingEmail)
    {
        this.outgoingEmail = outgoingEmail;
    }

    public boolean isOutgoingEmail()
    {
        return outgoingEmail;
    }

    public boolean isDowngradeAnyway()
    {
        return downgradeAnyway;
    }

    public void setDowngradeAnyway(boolean downgradeAnyway)
    {
        this.downgradeAnyway = downgradeAnyway;
    }
}
