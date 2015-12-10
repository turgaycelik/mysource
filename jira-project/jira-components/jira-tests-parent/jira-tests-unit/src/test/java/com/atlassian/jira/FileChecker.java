package com.atlassian.jira;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * A thing that checks the contents of files for test purposes.
 *
 * @since v4.0
 */
public interface FileChecker
{
    /**
     * Checks the file and returns a list of failures for that file or directory (empty on success)
     *
     * @param file the file to check.
     * @return a possibly empty list of failure messages.
     */
    List<String> checkFile(File file);

    FilenameFilter getFilenameFilter();

}
