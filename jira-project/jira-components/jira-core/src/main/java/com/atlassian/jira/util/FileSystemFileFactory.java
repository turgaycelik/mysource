package com.atlassian.jira.util;

import org.apache.log4j.Logger;

import java.io.File;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;

public class FileSystemFileFactory implements FileFactory
{
    /**
     * Logger for this class.
     */
    private static final Logger log = Logger.getLogger(FileSystemFileFactory.class);
    private final JiraProperties jiraSystemProperties;

    /**
     * Creates a new FileFactory.
     */
    public FileSystemFileFactory(JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    public File getFile(final String absoluteFilename)
    {
        return new File(absoluteFilename);
    }

    public void removeDirectoryIfExists(final String directoryName)
    {
        final File file = getFile(directoryName);
        if (file.exists())
        {
            removeDirectory(file);
        }
    }

    public void removeDirectory(final File directory)
    {
        String[] list = directory.list();

        if (list == null)
        {
            list = new String[0];
        }

        for (final String filename : list)
        {
            final File f = getFile(directory.getAbsolutePath() + jiraSystemProperties.getProperty("file.separator") + filename);
            if (f.isDirectory())
            {
                removeDirectory(f);
            }
            else
            {
                log.debug("Deleting " + f.getAbsolutePath());
                if (!f.delete())
                {
                    log.warn("Unable to delete file " + f.getAbsolutePath());
                }
            }
        }

        if (!directory.delete())
        {
            log.error("Unable to delete directory " + directory.getAbsolutePath());
        }
    }
}
