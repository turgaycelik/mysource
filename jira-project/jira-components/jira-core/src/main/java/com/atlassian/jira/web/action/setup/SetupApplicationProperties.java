package com.atlassian.jira.web.action.setup;

import com.atlassian.core.util.DateUtils;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.export.ExportService;
import com.atlassian.jira.tenancy.TenantImpl;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.UrlValidator;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.tenancy.api.event.TenantArrivedEvent;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class SetupApplicationProperties extends AbstractSetupAction
{
    private String nextStep; //The button at the bottom of the page
    private String title;
    private String baseURL;
    private String mode = "public";
    private String attachmentPath;
    private JiraHome jiraHome;
    private AttachmentPathManager.Mode attachmentMode = AttachmentPathManager.Mode.DEFAULT;
    private IndexPathManager.Mode indexMode = IndexPathManager.Mode.DEFAULT;

    private final ExternalLinkUtil externalLinkUtil;

    private static enum DirectoryMode
    {
        DEFAULT,
        DISABLED
    }

    private DirectoryMode backupMode = DirectoryMode.DEFAULT;

    private String indexPath;

    private final IssueIndexManager indexManager;
    private final ServiceManager serviceManager;
    private final IndexPathManager indexPathManager;
    private final AttachmentPathManager attachmentPathManager;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraWebResourceManager webResourceManager;

    public SetupApplicationProperties(final IssueIndexManager indexManager, final ServiceManager serviceManager,
            final IndexPathManager indexPathManager, final AttachmentPathManager attachmentPathManager,
            final JiraHome jiraHome, final BuildUtilsInfo buildUtilsInfo, final JiraSystemRestarter jiraSystemRestarter,
            final FileFactory fileFactory, final ExternalLinkUtil externalLinkUtil,
            final JiraWebResourceManager webResourceManager, final EventPublisher eventPublisher)
    {
        super(fileFactory);
        this.indexManager = indexManager;
        this.serviceManager = serviceManager;
        this.indexPathManager = indexPathManager;
        this.attachmentPathManager = attachmentPathManager;
        this.webResourceManager = webResourceManager;
        this.jiraHome = notNull("jiraHome", jiraHome);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraSystemRestarter = notNull("jiraSystemRestarter", jiraSystemRestarter);
        this.externalLinkUtil = notNull("externalLinkUtil", externalLinkUtil);
    }

    @Override
    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        final ApplicationProperties applicationProperties = getApplicationProperties();

        title = applicationProperties.getString(APKeys.JIRA_TITLE);
        if (title == null)
        {
            title = "Your Company JIRA";
            applicationProperties.setString(APKeys.JIRA_TITLE, title);
        }

        if (applicationProperties.getString(APKeys.JIRA_BASEURL) != null)
        {
            baseURL = applicationProperties.getString(APKeys.JIRA_BASEURL);
        }

        if (applicationProperties.getString(APKeys.JIRA_MODE) != null)
        {
            mode = applicationProperties.getString(APKeys.JIRA_MODE);
        }

        if (attachmentPathManager.getAttachmentPath() != null)
        {
            attachmentPath = attachmentPathManager.getAttachmentPath();
        }

        if (indexPathManager.getIndexRootPath() != null)
        {
            indexPath = indexPathManager.getIndexRootPath();
        }

        webResourceManager.putMetadata("version-number", buildUtilsInfo.getVersion());

        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        // return with no error messages, doExecute() will return the already setup view
        if ((nextStep == null) || setupAlready())
        {
            return;
        }

        if (!TextUtils.stringSet(title))
        {
            addError("title", getText("setup.error.specifytitle"));
        }

        if (!UrlValidator.isValid(baseURL))
        {
            addError("baseURL", getText("setup.error.baseURL"));
        }

        if (!getAllowedModes().keySet().contains(mode))
        {
            addError("mode", getText("setup.error.mode"));
        }

        super.doValidation();
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (nextStep == null)
        {
            return ERROR;
        }
        else
        {
            getApplicationProperties().setString(APKeys.JIRA_TITLE, title);
            getApplicationProperties().setString(APKeys.JIRA_BASEURL, baseURL);
            getApplicationProperties().setString(APKeys.JIRA_MODE, mode);

            // If we come back to this step then we need to ensure that the indexing is turned off.
            if (indexManager.isIndexAvailable())
            {
                try
                {
                    indexManager.deactivate();
                }
                catch (final Exception ignored)
                {
                    // Sink this exception as it will happen most of the time because indexing will be off.
                }
            }

            indexPathManager.setUseDefaultDirectory();

            try
            {
                indexManager.activate(Contexts.percentageLogger(indexManager, log));
            }
            catch (final Exception e)
            {
                log.error("Error activating indexing with path '" + indexPath + "': " + e, e);
                addError("indexPath", getText("setup.error.indexpath.activate_error", e.getMessage()));
            }

            if (attachmentMode == AttachmentPathManager.Mode.DISABLED)
            {
                getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, false);
            }
            else
            {
                attachmentPathManager.setUseDefaultDirectory();
                getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
            }

            // This must be done after the language is set
            if (backupMode != DirectoryMode.DISABLED)
            {
                new BackupServiceHelper().createOrUpdateBackupService(new I18nBean(), getDefaultBackupPath());
            }

            return invalidInput() ? ERROR : forceRedirect("SetupProductBundle!default.jspa");
        }
    }

    public void setNextStep(final String nextStep)
    {
        this.nextStep = nextStep;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(final String mode)
    {
        this.mode = mode;
    }

    public String getBaseURL()
    {
        // if the base URL is null, try to guess it from the request
        if (baseURL == null)
        {
            baseURL = JiraUrl.constructBaseUrl(request);
        }

        return baseURL;
    }

    public void setBaseURL(final String baseURL)
    {
        this.baseURL = StringUtils.stripEnd(StringUtils.strip(baseURL), " /");
    }

    private Map<String, String> getAllowedModes()
    {
        final Map<String, String> allowedModes = new HashMap<String, String>();

        allowedModes.put("public", "Public");
        allowedModes.put("private", "Private");

        return allowedModes;
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }

    public String getAttachmentPathOption()
    {
        return attachmentMode.toString();
    }

    public void setAttachmentPathOption(final String attachmentPathOption)
    {
        attachmentMode = AttachmentPathManager.Mode.valueOf(attachmentPathOption);
    }

    public String getIndexPathOption()
    {
        return indexMode.toString();
    }

    public void setIndexPathOption(final String indexPathOption)
    {
        indexMode = IndexPathManager.Mode.valueOf(indexPathOption);
    }

    public String getIndexPath()
    {
        return indexPath;
    }

    /**
     * Returns the absolute path for the Default Backup directory that lives under the home directory. This is used for
     * read-only info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Backup directory that lives under the home directory.
     */
    public String getDefaultBackupPath()
    {
        return jiraHome.getExportDirectory().getPath();
    }

    public String getBackupPathOption()
    {
        return backupMode.toString();
    }

    public void setBackupPathOption(final String backupPathOption)
    {
        backupMode = DirectoryMode.valueOf(backupPathOption);
    }

    public int modulo(int index, int modulus)
    {
        return index % modulus;
    }

    private final class BackupServiceHelper
    {
        private static final String SERVICE_NAME_KEY = "admin.setup.services.backup.service";
        private final long DELAY = DateUtils.HOUR_MILLIS * 12;

        /**
         * Creates a new Backup service, or updates an already existing one with the parameters supplied.
         */
        public void createOrUpdateBackupService(final I18nHelper i18n, final String backupPath)
        {
            try
            {
                final Map<String, String[]> params = new HashMap<String, String[]>();
                params.put(ExportService.USE_DEFAULT_DIRECTORY, new String[] { "true" });
                final String serviceName = geti18nTextWithDefault(i18n, SERVICE_NAME_KEY, "Backup Service");
                if (serviceManager.getServiceWithName(serviceName) == null)
                {
                    serviceManager.addService(serviceName, ExportService.class.getName(), DELAY, params);
                }
                else
                {
                    serviceManager.editServiceByName(serviceName, DELAY, params);
                }
                getApplicationProperties().setString(APKeys.JIRA_PATH_BACKUP, backupPath);
            }
            catch (final Exception e) // intentionally catching RuntimeException as well
            {
                log.error("Error creating backup service", e);
                addErrorMessage(getText("admin.errors.setup.error.adding.service", e.toString()));
            }
        }

        String geti18nTextWithDefault(final I18nHelper i18n, final String key, final String defaultString)
        {
            final String result = i18n.getText(key);
            if (key.equals(result))
            {
                return defaultString;
            }
            else
            {
                return result;
            }
        }
    }
}
