package com.atlassian.jira.web.action.setup;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.AlreadyExecutingException;
import com.atlassian.jira.task.ImportTaskManager;
import com.atlassian.jira.task.ImportTaskManagerImpl;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextKeys;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.util.ImportResultHandler;
import com.atlassian.jira.web.servletcontext.ServletContextReference;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import com.google.common.collect.ImmutableMap;

import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@SuppressWarnings ("UnusedDeclaration")
public class SetupImport extends AbstractSetupAction
{
    private static final String PROGRESS_URL = "/importprogress?setup=true";
    private static ServletContextReference<ImportTaskManager> taskManagerReference =
            new ServletContextReference<ImportTaskManager>(ServletContextKeys.DATA_IMPORT_TASK_MANAGER);

    private final IndexPathManager indexPathManager;
    private final ExternalLinkUtil externalLinkUtil;
    private final BuildUtilsInfo buildUtilsInfo;
    private final DataImportService dataImportService;
    private ImportResultHandler importResultHandler;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final MailSettings mailSettings;

    private String filename;
    private String license;
    private boolean useDefaultPaths;
    private boolean outgoingEmail = false;
    private ErrorCollection specificErrors;

    /**
     * Whether to downgrade JIRA, even if this may
     */
    private boolean downgradeAnyway;

    /**
     * Whether to override any dangermode setting, enforcing regular safe import operation.
     */
    private boolean safeMode;

    public SetupImport(final IndexPathManager indexPathManager, final ExternalLinkUtil externalLinkUtil,
            final BuildUtilsInfo buildUtilsInfo, final FileFactory fileFactory, final DataImportService dataImportService,
            final ImportResultHandler importResultHandler, final VelocityRequestContextFactory velocityRequestContextFactory, final MailSettings mailSettings)
    {
        super(fileFactory);
        this.dataImportService = dataImportService;
        this.importResultHandler = importResultHandler;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.mailSettings = mailSettings;
        this.indexPathManager = notNull("indexPathManager", indexPathManager);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public String doFetchLicense() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        ActionContext.getSession().put(SessionKeys.SETUP_IMPORT_XML, filename);

        return INPUT;
    }

    public String doReturnFromMAC() throws Exception
    {
        // Re-populate the filename with the values saved in session before left to MAC to get the license
        // If the value isn't stored in session, then set it to empty
        if (ActionContext.getSession().get(SessionKeys.SETUP_IMPORT_XML) == null)
        {
            filename = "";
        }
        else
        {
            filename = ActionContext.getSession().get(SessionKeys.SETUP_IMPORT_XML).toString();
        }

        return INPUT;
    }

    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        //if we come to this page explicitly clear away any previous import result.
        ActionContext.getSession().remove(SessionKeys.DATA_IMPORT_RESULT);
        outgoingEmail = mailSettings.send().isEnabled();
        return super.doDefault();
    }

    protected void doValidation()
    {
        // return with no error messages, doExecute() will return the already setup view
        if (setupAlready())
        {
            return;
        }

        final DataImportService.ImportResult lastResult =
                (DataImportService.ImportResult) ActionContext.getSession().get(SessionKeys.DATA_IMPORT_RESULT);

        if (lastResult == null)
        {
            final DataImportParams params = buildDataImportParameters();
            final DataImportService.ImportValidationResult result = dataImportService.validateImport(getLoggedInUser(), params);
            if (!result.isValid())
            {
                addErrorCollection(result.getErrorCollection());
            }
        }
    }

    // Do not make this Constructor Injected! That is WRONG. Because we reset the PICO world in the middle of the import
    // we don't want to reference things from the old PICO. So we need to dynamically get it everytime to ensure we
    // always get it from the correct PICO.
    private JiraLicenseService getLicenseService()
    {
        return ComponentAccessor.getComponent(JiraLicenseService.class);
    }

    protected String doExecute() throws Exception
    {
        final DataImportService.ImportResult lastResult = (DataImportService.ImportResult) ActionContext.getSession().get(SessionKeys.DATA_IMPORT_RESULT);
        //if we got a valid importresult lets redirect to the completed setup step
        if (lastResult != null)
        {
            try
            {
                if (!lastResult.isValid())
                {
                    filename = lastResult.getParams().getFilename();
                    useDefaultPaths = lastResult.getParams().isUseDefaultPaths();
                    license = lastResult.getParams().getLicenseString();

                    addErrorCollection(lastResult.getErrorCollection());

                    specificErrors = new SimpleErrorCollection();
                    if(importResultHandler.handleErrorResult(ActionContext.getServletContext(), lastResult, this, specificErrors))
                    {
                        return getRedirect(JohnsonConfig.getInstance().getErrorPath());
                    }

                    return ERROR;
                }
                return getRedirect("Dashboard.jspa?src=" + SetupImport.class.getSimpleName());
            }
            finally
            {
                ActionContext.getSession().remove(SessionKeys.DATA_IMPORT_RESULT);
            }
        }

        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (taskManagerReference.get() != null && taskManagerReference.get().getTask() != null)
        {
            //looks like we already have a taskmanager. Let's see if there's any progress to report on.
            return getRedirect(getProgressUrl());
        }
        taskManagerReference.set(new ImportTaskManagerImpl());

        //Add a warning that setup is in progress
        final JohnsonEventContainer eventCont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        try
        {
            final DataImportParams params = buildDataImportParameters();
            final DataImportService.ImportValidationResult result = dataImportService.validateImport(getLoggedInUser(), params);
            final DataImportAsyncCommand importCallable = new DataImportAsyncCommand(eventCont, dataImportService, getLoggedInUser(), result,
                    new Event(EventType.get("setup"), "JIRA is currently being set up", EventLevel.get(EventLevel.WARNING)), velocityRequestContextFactory.getJiraVelocityRequestContext(), ActionContext.getRequest().getSession(false));
            Locale locale = getComponentInstanceOfType(JiraAuthenticationContext.class).getLocale();
            taskManagerReference.get().submitTask(importCallable, getText("setup.import.title"));
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
                setLicenseString(license).
                setUseDefaultPaths(useDefaultPaths).
                setAllowDowngrade(downgradeAnyway).
                setupImport();

        if (isOutgoingMailModifiable())
        {
            builder.setOutgoingEmailTo(outgoingEmail);
        }
        if (safeMode) {
            builder.setSafeMode();
        }
        return builder.build();
    }

    private String getProgressUrl()
    {
        return PROGRESS_URL + "&locale=" + getLocale().toString();
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Returns the absolute path for the Default Index directory ([jira-home]/caches/index/) This is used for read-only
     * info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Index directory ([jira-home]/caches/index/)
     */
    public String getDefaultIndexPath()
    {
        return indexPathManager.getDefaultIndexRootPath();
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(final String license)
    {
        this.license = license;
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public boolean isUseDefaultPaths()
    {
        return useDefaultPaths;
    }

    public boolean hasSpecificErrors()
    {
        return specificErrors != null && specificErrors.hasAnyErrors();
    }

    public ErrorCollection getSpecificErrors()
    {
        return specificErrors;
    }

    public void setUseDefaultPaths(final boolean useDefaultPaths)
    {
        this.useDefaultPaths = useDefaultPaths;
    }

    public boolean isDowngradeAnyway()
    {
        return downgradeAnyway;
    }

    public void setDowngradeAnyway(boolean downgradeAnyway)
    {
        this.downgradeAnyway = downgradeAnyway;
    }

    public String getRequestLicenseURL()
    {
        StringBuilder url = new StringBuilder();
        url.append(JiraUrl.constructBaseUrl(request));
        url.append("/secure/SetupImport!returnFromMAC.jspa");

        return externalLinkUtil.getProperty("external.link.jira.license.view", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", getServerId(), url.toString()));
    }

    public void setOutgoingEmail(boolean outgoingEmail)
    {
        this.outgoingEmail = outgoingEmail;
    }

    public boolean isOutgoingMailModifiable()
    {
        return mailSettings.send().isModifiable();
    }

    public Map<String, String> getOutgoingMailOptions()
    {
        return ImmutableMap.of("true", "Enable", "false", "Disable");
    }

    public boolean outgoingEmail()
    {
        return outgoingEmail;
    }
}
