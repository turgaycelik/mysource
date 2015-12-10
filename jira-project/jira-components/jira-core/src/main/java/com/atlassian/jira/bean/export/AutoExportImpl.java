package com.atlassian.jira.bean.export;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.dataimport.ExportService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.google.common.base.Joiner;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Determines the best possible location (if any) and export all JIRA data to
 * the chosen location using any provided filename generation strategy.
 */
public class AutoExportImpl implements AutoExport
{

    private static final Logger log = Logger.getLogger(AutoExportImpl.class);

    /**
     * Uses the {@link AutoExport#BASE_FILE_NAME} prefix and a datestamp for generating filenames.
     */
    private static final FilenameGenerator DATESTAMP_FILENAME_GENERATOR = new FilenameGenerator()
    {
        public File generate(String basepath) throws IOException
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateExtension = simpleDateFormat.format(new Date());
            File file = new File(basepath + File.separator + BASE_FILE_NAME + dateExtension + ".zip");
            return file;
        }
    };

    private String defaultDir;
    private FilenameGenerator filenameGenerator;

    /**
     * Creates an AutoExport that uses the given defaultDir and a datestamp
     * filename generator.
     * @param defaultDir
     */
    public AutoExportImpl(String defaultDir)
    {
        this(defaultDir, DATESTAMP_FILENAME_GENERATOR);
    }

    /**
     * Creates an AutoExport which uses the given defaultDir and filenameGenerator.
     * @param defaultDir
     * @param filenameGenerator
     */
    AutoExportImpl(String defaultDir, FilenameGenerator filenameGenerator)
    {
        this.defaultDir = defaultDir;
        this.filenameGenerator = filenameGenerator;
        if (defaultDir == null) {
            throw new NullPointerException("defaultDir");
        }
        if (filenameGenerator == null) {
            throw new NullPointerException("filenameGenerator");
        }
    }

    private String getDefaultDir()
    {
        return defaultDir;
    }

    /**
     * Attempts to choose the best location for export and exports the existing data
     * This method tries to export to backup path, then to index path and the to the value
     * of the "javax.servlet.context.tempdir" property of the passed in ServletContext
     *
     * @return the list of errors that occured duing the export
     */
    public String exportData()
            throws IOException, FileExistsException, IllegalXMLCharactersException, AtlassianCoreException
    {
        final String exportFilename = getExportFilePath();

        final ExportService exportService = getExportService();
        final JiraAuthenticationContext authenticationContext = getJiraAuthContext();

        final ServiceOutcome<Void> outcome = exportService.export(authenticationContext.getLoggedInUser(), exportFilename, TaskProgressSink.NULL_SINK);
        if (outcome.isValid())
        {
            return exportFilename;
        }
        else
        {
            if (outcome.getErrorCollection().getReasons().contains(ErrorCollection.Reason.VALIDATION_FAILED))
            {
                log.error("Invalid XML characters found in data. Cannot export data before upgrade.");
                throw new IllegalXMLCharactersException("Illegal XML characters found in data.\nCannot export data before upgrade.");
            }
            else
            {
                throw new AtlassianCoreException(Joiner.on("\n").join(outcome.getErrorCollection().getErrorMessages()));
            }
        }
    }

    protected JiraAuthenticationContext getJiraAuthContext()
    {
        return ComponentAccessor.getComponent(JiraAuthenticationContext.class);
    }

    protected ExportService getExportService()
    {
        return ComponentAccessor.getComponent(ExportService.class);
    }

    /**
     * Tries the JIRA backup path, the configured JIRA index path and the servlet
     * temp directory as candidate export file paths before failing.
     * @return the ultimate filepath decided.
     * @throws FileNotFoundException if configuration of each option is a missing dir.
     * @throws FileExistsException if there is a file in the way of the generated path.
     * @throws IOException if there is a filesystem problem in determining the best path.
     */
    public String getExportFilePath() throws FileNotFoundException, FileExistsException, IOException
    {
        // Get the default backup path
        JiraHome jiraHome = ComponentAccessor.getComponent(JiraHome.class);
        String filename = jiraHome.getExportDirectory().getPath();
        if (!isValidDirectory(filename))
        {
            // Check if the index directory is set
            filename = ComponentAccessor.getIndexPathManager().getIndexRootPath();
            if (!isValidDirectory(filename))
            {
                // Try the servlet temp directory
                filename = getDefaultDir();
                if (!isValidDirectory(filename))
                {
                    throw new FileNotFoundException("Could not find suitable directory for export.");
                }
            }
        }

        File file = filenameGenerator.generate(filename);
        if (file.exists())
        {
            throw new FileExistsException("File with file name '" + file.getAbsolutePath() + "' already exists.");
        }

        return file.getAbsolutePath();
    }



    protected boolean isValidDirectory(String filename)
    {
        // Ensure that the string is not null and is not empty
        if (!TextUtils.stringSet(filename))
        {
            return false;
        }

        File file = new File(filename);

        // Check if the filename exists, it is a directory and we can write there
        if (file.exists() && file.isDirectory() && file.canWrite())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
