package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.service.ConfigService;

import java.util.Iterator;

/**
 * Check to make sure that there are no backup service configured. There are two things to check here:
 *
 * <ol>
 * <li>
 * The backup service. We can delete the service, or tell it to use the default directory if the service
 * actually needs to be in the file.
 * </li>
 * The default (aka global) JIRA backup directory. This is used by the upgrade manager to backup the database
 * before an upgrade on direct database upgrade.
 * <li>
 *
 * @since v4.0
 */
final public class BackupChecker implements ConfigurationCheck
{
    /**
     * ID of the check that looks for the backup service.
     */
    public static final String CHECK_BACKUP_SERVICE = "backupservice";

    /**
     * ID of the check that makes sure the backup path is using JIRA.HOME.
     */
    public static final String CHECK_BACKUP_SERVICE_HOME = "backupservicehome";

    /**
     * ID of the check that makes sure the backup path is set to the default if the backup service is required.
     */
    public static final String CHECK_BACKUP_SERVICE_DIRECTORY = "backupservicedirectory";

    /**
     * ID of the check that cleans up JIRA's global backup path. This global backup path is used by the upgrade manager
     * when performing a database upgrade.
     */
    public static final String CHECK_GLOBAL_BACKUP_DIRECTORY = "backupglobaldirectory";

    /**
     * Service configuration key that indicates whether or the default directory should be used.
     */
    private static final String OPT_USE_DEFAULT_DIRECTORY = "USE_DEFAULT_DIRECTORY";

    /**
     * Service configuration key that indicates the location of the backup directory.
     */
    private static final String OPT_DIR_NAME = "DIR_NAME";

    /**
     * The property that holds JIRA's global backup path.
     */
    private static final String JIRA_PATH_BACKUP = "jira.path.backup";

    /**
     * Name of the class for the export service used in JIRA.
     */
    public static final String BACKUP_SERVICE = "ExportService";

    /**
     * Name of the default directory to use for the backup path.
     */
    private static final String DEAFULT_BACKUP_PATH = "func_test_backup";

    public Result checkConfiguration(final JiraConfig config, final CheckOptions options)
    {
        CheckResultBuilder builder = new CheckResultBuilder();
        try
        {
            processConfig(config, options, builder);
        }
        catch (Exception e)
        {
            return builder.error(e.getMessage()).buildResult();
        }
        return builder.buildResult();
    }

    private void processConfig(final JiraConfig config, final CheckOptions options, final CheckResultBuilder builder)
    {
        //<ServiceConfig id="10001" time="43200000" clazz="com.atlassian.jira.service.services.export.ExportService" name="Backup Service"/>
        for (ConfigService service : config.getServices())
        {
            if (service.getClazz() != null && service.getClazz().contains(BACKUP_SERVICE))
            {
                //Check to see if the backup service exists.
                if (options.checkEnabled(CHECK_BACKUP_SERVICE))
                {
                    final String name = service.getName() != null ? service.getName() : "<anonymous>";
                    builder.error(String.format("Backup service '%s' exists.", name), CHECK_BACKUP_SERVICE);
                }
                else
                {
                    //If we are told to leave the backup service, then we should at least try to use the default directory.
                    if (options.checkEnabled(CHECK_BACKUP_SERVICE_HOME))
                    {
                        final ConfigPropertySet propertySet = service.getPropertySet();
                        if (!Boolean.parseBoolean(propertySet.getStringProperty(OPT_USE_DEFAULT_DIRECTORY)))
                        {
                            final String directory = propertySet.getStringPropertyDefault(OPT_DIR_NAME, "<none>");
                            builder.error(String.format("Backup service configured to output to directory '%s' it should be using JIRA.HOME.", directory), CHECK_BACKUP_SERVICE_HOME);
                        }
                    }

                    if (options.checkEnabled(CHECK_BACKUP_SERVICE_DIRECTORY))
                    {
                        final ConfigPropertySet propertySet = service.getPropertySet();
                        final String property = propertySet.getStringPropertyDefault(OPT_DIR_NAME, "<none>");
                        if (propertySet.contains(OPT_DIR_NAME) && !DEAFULT_BACKUP_PATH.equals(property))
                        {
                            builder.warning(String.format("Backup service configured to output to '%s'. It should always be set to '%s' even when using JIRA.HOME.", property, DEAFULT_BACKUP_PATH),
                                    CHECK_BACKUP_SERVICE_DIRECTORY);
                        }
                    }
                }
            }
        }

        //Give a warning if JIRA's global backup path is set.
        if (options.checkEnabled(CHECK_GLOBAL_BACKUP_DIRECTORY))
        {
            final ConfigPropertySet configPropertySet = config.getApplicationProperties();
            final String s = configPropertySet.getStringProperty(JIRA_PATH_BACKUP);
            if (s != null)
            {
                builder.warning(String.format("Global backup path set to '%s'.", s), CHECK_GLOBAL_BACKUP_DIRECTORY);
            }
        }
    }

    public void fixConfiguration(final JiraConfig config, final CheckOptions options)
    {
        for (Iterator<ConfigService> iterator = config.getServices().iterator(); iterator.hasNext();)
        {
            ConfigService service = iterator.next();
            if (service.getClazz() != null && service.getClazz().contains(BACKUP_SERVICE))
            {
                //Remove any backup service if allowed to do so.
                if (options.checkEnabled(CHECK_BACKUP_SERVICE))
                {
                    iterator.remove();
                }
                else
                {
                    final ConfigPropertySet propertySet = service.getPropertySet();

                    //Get the service to use the default directory if allowed.
                    if (options.checkEnabled(CHECK_BACKUP_SERVICE_HOME))
                    {
                        propertySet.setStringProperty(OPT_USE_DEFAULT_DIRECTORY, Boolean.toString(true));
                    }

                    if (options.checkEnabled(CHECK_BACKUP_SERVICE_DIRECTORY))
                    {
                        //Cleanup the directory location.
                        String dirName = propertySet.getStringProperty(OPT_DIR_NAME);
                        if (dirName != null)
                        {
                            propertySet.setStringProperty(OPT_DIR_NAME, DEAFULT_BACKUP_PATH);
                        }
                    }
                }
            }
        }

        final ConfigPropertySet configPropertySet = config.getApplicationProperties();
        if (options.checkEnabled(CHECK_GLOBAL_BACKUP_DIRECTORY))
        {
            //Remove the backup path if allowed.
            configPropertySet.removeProperty(JIRA_PATH_BACKUP);
        }
    }
}
