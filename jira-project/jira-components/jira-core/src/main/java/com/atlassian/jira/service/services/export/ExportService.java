package com.atlassian.jira.service.services.export;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.IOUtil;
import com.google.common.base.Joiner;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportService extends AbstractService
{
    public static final String USE_DEFAULT_DIRECTORY = "USE_DEFAULT_DIRECTORY";
    public static final String DIR_NAME = "DIR_NAME";
    public static final String OPT_DATE_FORMAT = "OPT_DATE_FORMAT";
    /**
     * The subdirectory of jira-home that exports go to when we use the default directory
     */
    public static final String EXPORT_SUBDIRECTORY = "export";

    private static final Logger log = Logger.getLogger(ExportService.class);
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MMM-dd--HHmm";
    private String dirName;
    private String dateFormat;
    private static final String CORRUPTED_DIRNAME = "corrupted";
    private static final String FAILURE_TXT_EXT = ".failure.txt";
    private final JiraHome jiraHome;

    public ExportService(final JiraHome jiraHome)
    {
        this.jiraHome = jiraHome;
    }

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);
        boolean useDefaultDirectory = isUsingDefaultDirectory();
        // If they've explicitly opted for a default directory use that.
        if (useDefaultDirectory)
        {
            dirName = jiraHome.getHomePath() + File.separator + EXPORT_SUBDIRECTORY;
        }
        // If they have something else specified use that.
        else if (hasProperty(DIR_NAME))
        {
            dirName = getProperty(DIR_NAME);
        }
        // And as a final fall back, use the default directory. This will be the most
        // common case for "new" installations that don't have all the above configuration options.
        else
        {
            dirName = jiraHome.getHomePath() + File.separator + EXPORT_SUBDIRECTORY;
        }
        if (hasProperty(OPT_DATE_FORMAT))
        {
            dateFormat = getProperty(OPT_DATE_FORMAT);
        }
    }

    private boolean isUsingDefaultDirectory() throws ObjectConfigurationException
    {
        final String key = USE_DEFAULT_DIRECTORY;
        return hasProperty(key) && Boolean.parseBoolean(getProperty(key));
    }

    public void run()
    {
        log.debug("Jira Export Service Running");

        File backupDir = null;
        File backupFile = null;
        try
        {
            if (dirName == null)
            {
                log.error("No directory specified for Export Service \"" + getName() + "\". Using default directory.");
                dirName = jiraHome.getHomePath() + File.separator + EXPORT_SUBDIRECTORY;
            }

            backupDir = new File(dirName);
            if (!backupDir.exists())
            {
                log.info("Backup directory '" + dirName + "' for Export Service \"" + getName() + "\" does not exist - attempting to create...");
                // Attempt to create it
                if (backupDir.mkdirs())
                {
                    log.info("Backup directory '" + dirName + "' for Export Service \"" + getName() + "\" was created.");
                }
                else
                {
                    log.warn("Backup directory '" + dirName + "' for Export Service \"" + getName() + "\" does not exist and JIRA could not create it.");
                    return;
                }
            }
            if (!backupDir.isDirectory())
            {
                log.warn("Backup directory " + dirName + " for Export Service \"" + getName() + "\" is not a directory");
                return;
            }
            if (!backupDir.canWrite())
            {
                log.warn("Backup directory " + dirName + " for Export Service \"" + getName() + "\" is not writable");
                return;
            }

            backupFile = new File(dirName, createFileName());
            if (backupFile.exists())
            {
                log.warn("File " + backupFile.getAbsolutePath() + " for Export Service \"" + getName() + "\" exists already.");
                return;
            }

            performBackup(backupFile.getAbsolutePath());
            log.debug("Jira Export Service Finished without Exception");
        }
        catch (Exception failureEx)
        {
            log.error("An exception while running the export service \"" + getName() + "\": " + failureEx.getMessage(), failureEx);
            moveBackupAside(backupDir, backupFile, failureEx);
        }
    }

    void performBackup(final String filename) throws Exception
    {
        final com.atlassian.jira.bc.dataimport.ExportService exportService = ComponentAccessor.getComponent(com.atlassian.jira.bc.dataimport.ExportService.class);
        final JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponent(JiraAuthenticationContext.class);

        final ServiceOutcome<Void> outcome = exportService.export(authenticationContext.getLoggedInUser(), filename, TaskProgressSink.NULL_SINK);
        if (!outcome.isValid())
        {
            throw new AtlassianCoreException(Joiner.on("\n").join(outcome.getErrorCollection().getErrorMessages()));
        }
    }

    /**
     * @return an I18nHelper
     */
    I18nHelper getI18nHelper()
    {
        return ComponentAccessor.getComponentOfType(I18nHelper.class);
    }

    /**
     * We want to be careful and not give the admin the impressions that the backup worked.  So if an exception occurs
     * we move it aside into a 'corrupted' directory.  We also leave a .txt file note indicating the what went wrong
     * during the exception
     * <p/>
     * Fix for JRA-11877
     *
     * @param backupDir  the service backup directory
     * @param backupFile the target service backup file
     * @param failureEx  the exception incurred during backup
     *
     * @return true if the backup file could be moved aside
     */
    boolean moveBackupAside(File backupDir, File backupFile, Exception failureEx)
    {
        // does the backup file exist.  if not we can move it
        if (backupDir == null || backupFile == null || !backupFile.exists())
        {
            // nothing to move.  This would be very unusual in practice but at least cater for it
            return false;
        }

        File corruptedDirectory = createCorruptedDirectory(backupDir);
        if (corruptedDirectory == null)
        {
            // if we cant create a corrupted directory then lets
            // just write a reason file along side the failed backup file
            // as a worst case measure
            writeFailureReasonFile(backupDir, backupFile, failureEx);
            return false;
        }
        else
        {
            //
            // move the backup file and write a reason file
            boolean ok = moveFailedBackupFile(backupFile, corruptedDirectory);
            writeFailureReasonFile(corruptedDirectory, backupFile, failureEx);
            return ok;
        }
    }

    /**
     * This will lazilly create the corrupted directory under the backup directory passed in.
     *
     * @param backupDir - where to create the corrupted directory
     *
     * @return null if it cant be created.
     */
    File createCorruptedDirectory(File backupDir)
    {
        File corruptedDirectory = new File(backupDir, CORRUPTED_DIRNAME);
        try
        {
            if (corruptedDirectory.exists())
            {
                if (corruptedDirectory.isDirectory() && corruptedDirectory.canWrite())
                {
                    return corruptedDirectory;
                }
                else
                {
                    log.error("Cannot create backup corrupted directory '" + corruptedDirectory.getAbsoluteFile() + "'. it exists but is not a writeable directory.");
                    return null;
                }
            }
            if (corruptedDirectory.mkdirs())
            {
                return corruptedDirectory;
            }
            log.error("Cannot create backup corrupted directory '" + corruptedDirectory.getAbsoluteFile() + "'. mkdir() failed.");
        }
        catch (Exception e)
        {
            // seriously this should not happen.  A SecurityException would be very bad but lets be defensive
            log.error("Cannot create backup corrupted directory '" + corruptedDirectory.getAbsoluteFile() + "'.", e);
        }
        return null;
    }

    /**
     * Creates a text file in the targetDir with the reason the backup failed and exception stack trace
     *
     * @param targetDir  the target directory for the reason file
     * @param backupFile the name of the failed backup file
     * @param failureEx  the exception incurred during backup
     */
    void writeFailureReasonFile(File targetDir, File backupFile, Exception failureEx)
    {
        String backupFileName = backupFile.getName();
        File reasonFile = new File(targetDir, backupFileName + FAILURE_TXT_EXT);
        PrintWriter pw = null;
        try
        {
            I18nHelper i18nHelper = getI18nHelper();

            pw = new PrintWriter(new FileWriter(reasonFile));
            pw.println(i18nHelper.getText("admin.service.export.backup.failed", backupFile.getAbsolutePath()));
            pw.println(i18nHelper.getText("admin.service.export.backup.movedaside", targetDir.getAbsolutePath()));
            pw.println(i18nHelper.getText("admin.service.export.backup.stacktrace"));
            failureEx.printStackTrace(pw);
            log.warn("A backup failure reason file was written to '" + reasonFile.getAbsolutePath() + "'.");
        }
        catch (IOException e1)
        {
            log.error("Unable to create backup failure reason file '" + reasonFile.getAbsolutePath() + "'", e1);
        }
        finally
        {
            IOUtil.shutdownWriter(pw);
        }
    }

    /**
     * Moves the named backup file to the specified directory.
     *
     * @param backupFile the file to move
     * @param targetDir  the target directory
     *
     * @return true if it worked ok
     */
    boolean moveFailedBackupFile(File backupFile, File targetDir)
    {
        String backupFileName = backupFile.getName();
        File corruptedBackupFile = new File(targetDir, backupFileName);
        try
        {
            FileUtils.copyFile(backupFile, corruptedBackupFile, true);
            if (!backupFile.delete())
            {
                // unlikely but hey
                log.warn("The corrupted backup file '" + backupFile.getAbsolutePath() + "' was copied to '" + corruptedBackupFile.getAbsolutePath() + "' but could not be deleted.");
                return false;
            }
            log.warn("The corrupted backup file '" + backupFile.getAbsolutePath() + "' was moved to '" + corruptedBackupFile.getAbsolutePath() + "'.");
            return true;
        }
        catch (IOException ioe)
        {
            log.error("Exception while moving corrupted backup file '" + backupFile.getAbsolutePath() + "' to '" + corruptedBackupFile.getAbsolutePath() + "'.", ioe);
        }
        return false;
    }

    private String createFileName()
    {
        DateFormat format;

        //use a custom date format if given
        if (TextUtils.stringSet(dateFormat))
        {
            format = new SimpleDateFormat(dateFormat);
        }
        else //use default format
        {
            format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }

        return format.format(new Date()) + ".zip";
    }

    public void destroy()
    {
        log.debug("Export service \"" + getName() + "\" being destroyed");
    }

    /**
     * Can have multiple export services, all running at different times
     *
     * @return false
     */
    public boolean isUnique()
    {
        return false;
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("EXPORTSERVICE", "services/com/atlassian/jira/service/services/export/exportservice.xml", null);
    }
}
