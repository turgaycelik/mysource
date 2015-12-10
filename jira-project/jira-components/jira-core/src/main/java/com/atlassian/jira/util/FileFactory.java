package com.atlassian.jira.util;

import java.io.File;

/**
 * Interface for file operations. This interface exists so that we can mock out IO operations in unit tests.
 *
 * @since v4.3
 */
public interface FileFactory
{
    /**
     * Creates a File object for the file with the given path.
     *
     * @param absoluteFilename a String containing an absolute path name
     * @return a new File
     */
    File getFile(final String absoluteFilename);

    /**
     * Removes a directory.
     *
     * @param directory a File object
     */
    void removeDirectory(final File directory);

    /**
     * Removes a directory if it exists.
     *
     * @param directoryName a String containing a directory path
     */
    void removeDirectoryIfExists(final String directoryName);
}
