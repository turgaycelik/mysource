package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.IOUtil;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.validation.Validated;
import com.atlassian.validation.Validator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Migrates custom defaults to the database for selected ApplicationProperties that have been preemptively added to the
 * classpath via the jira-application.properties file in the installation directory under $APP_ROOT/WEB-INF/classes.
 * <p/>
 * The selection of application properties that have been added to the database (and have a UI) includes a specific list
 * for JIRA 4.4. These are {@link #MIGRATABLE_KEYS}.
 *
 * @since v4.4
 */
public class UpgradeTask_Build637 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build637.class);

    /**
     * Used prior to JIRA 4.4.
     */
    private static final String LEGACY_PROPERTIES_FILE = "jira-application.properties";

    /**
     * Keys of properties that now have a UI. Custom defaults (as written into the jira-application.properties file)
     * will better be put in the database.
     */
    private List<String> MIGRATABLE_KEYS = Arrays.asList("jira.view.issue.links.sort.order", "jira.table.cols.subtasks", "jira.projectkey.pattern", "jira.issue.actions.order", "jira.date.picker.java.format", "jira.clone.prefix", "jira.attachment.number.of.zip.entries");

    private final ApplicationPropertiesService applicationPropertiesService;
    private final ApplicationPropertiesStore propertiesStore;
    private final JiraHome jiraHome;

    public UpgradeTask_Build637(ApplicationPropertiesService applicationPropertiesService,
            ApplicationPropertiesStore propertiesStore, JiraHome jiraHome)
    {
        super(false);
        this.applicationPropertiesService = applicationPropertiesService;
        this.propertiesStore = propertiesStore;
        this.jiraHome = jiraHome;
    }

    @Override
    public final String getBuildNumber()
    {
        return "637";
    }

    @Override
    public String getShortDescription()
    {
        return "Migrates custom defaults to the database for selected ApplicationProperties";
    }

    @Override
    public final void doUpgrade(boolean setupMode) throws Exception
    {

        File homeDirConfigFile = new File(jiraHome.getLocalHomePath(), ApplicationPropertiesStore.JIRA_CONFIG_PROPERTIES);
        if (homeDirConfigFile.exists())
        {
            log.warn(homeDirConfigFile.getPath() + " already exists. Migration of '" + LEGACY_PROPERTIES_FILE + "' will be skipped.");
            return;
        }
        
        final Properties properties = loadPropertiesFromLegacyFile(getLegacyPropertiesStream());
        final Properties overlayProps = new Properties();
        for (Object o : properties.keySet())
        {
            String key = (String) o;
            String value = properties.getProperty(key);
            if (isModified(key, value))
            {
                if (MIGRATABLE_KEYS.contains(key))
                {
                    storeInDatabase(key, value);
                }
                else
                {
                    if (!"jira.home".equals(key))
                    {
                        overlayProps.setProperty(key, value);
                    }
                }
            }
        }
        if (!overlayProps.isEmpty())
        {
            writeOverlayFile(overlayProps);
        }

        propertiesStore.refresh();
    }

    private boolean isModified(String key, String value)
    {
        try
        {
            ApplicationProperty prop = applicationPropertiesService.getApplicationProperty(key);
            if (prop != null)
            {
                return !prop.getMetadata().getDefaultValue().equals(value);
            }
            else
            {
                // If property is not defined inside defaults than we consider it as modified.
                // Some properties were commented out on default inside jira-application.properties and are not
                // in jpm.xml.
                return true;
            }
        }
        catch (Exception ignored)
        {
            // if we don't have the property, we probably can't use it for evil
        }
        return false;
    }

    void writeOverlayFile(Properties overlayProps) throws IOException
    {
        File homeDirConfigFile = new File(jiraHome.getLocalHomePath(), ApplicationPropertiesStore.JIRA_CONFIG_PROPERTIES);
        String homeDirPath = homeDirConfigFile.getCanonicalPath();
        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(homeDirConfigFile);
            overlayProps.store(fileOutputStream, "Generated by JIRA upgrade task " + getBuildNumber() + " migrating custom properties out of legacy " + LEGACY_PROPERTIES_FILE);
            log.warn("Custom properties have been moved from the legacy file " + LEGACY_PROPERTIES_FILE + " to the new shiny " + homeDirPath);

            try
            {
                fileOutputStream.close();
            }
            catch (Exception e)
            {
                log.warn("Failed to peacfully complete migration from '" + LEGACY_PROPERTIES_FILE + "'. Please confirm " + "your custom properties from this have arrived safely in the " + homeDirPath + " file.", e);
            }
        }
        catch (IOException e)
        {
            log.error("Unable to write custom properties into " + homeDirPath, e);
        }
    }

    InputStream getLegacyPropertiesStream()
    {
        return ClassLoaderUtils.getResourceAsStream(LEGACY_PROPERTIES_FILE, this.getClass());
    }

    private void storeInDatabase(String key, String value)
    {
        try
        {
            Validated<ApplicationProperty> validated = applicationPropertiesService.setApplicationProperty(key, value);
            Validator.Result validatorResult = validated.getResult();
            if (!validatorResult.isValid())
            {
                log.error("Cannot set application property '" + key + "' to value '" + value + "' due to validation failure: " + validatorResult.getErrorMessage());
                handleFailure(key, value, null);
            }
        }
        catch (Exception e)
        {
            // fail whale
            handleFailure(key, value, e);
        }
    }

    private Properties loadPropertiesFromLegacyFile(InputStream propertiesStream)
    {
        Properties props = new Properties();
        if (propertiesStream != null)
        {
            try
            {
                props.load(propertiesStream);
                IOUtil.shutdownStream(propertiesStream);
            }
            catch (final IOException e)
            {
                log.warn("Failed to migrate custom default properties from '" + LEGACY_PROPERTIES_FILE + "' file. Please check the new admin configuration options added in " + "JIRA 4.4 to ensure your configuration is correct.");
            }
        }
        else
        {
            log.debug("No " + LEGACY_PROPERTIES_FILE + " file found to migrate, doing nothing.");
        }
        return props;
    }

    void handleFailure(String key, String property, Exception e)
    {
        String mesg = "Failed to store the custom default application property '" + key + "' as value '" + property + "'. Please use the administration interface to configure the setting";
        if (e != null)
        {
            log.warn(mesg, e);
        }
        else
        {
            log.warn(mesg);
        }
    }
}
