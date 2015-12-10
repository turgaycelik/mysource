/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bean.export;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Abstraction to perform the actual export of backup data from JIRA. Implementations
 * typically have some backing configuration.
 */
public interface AutoExport
{
    public String FIX_INVALID_XML_CHARACTERS = "fixchars";

    /**
     * A default filename prefix that can be used by implementations.
     */
    public String BASE_FILE_NAME = "jira_autoexport_";

    /**
     * Performs the actual export of the data according to the implementation's
     * configuration. Implementations may choose what filename is ultimately used.
     * @return the filename ultimately used for the export.
     * @throws FileNotFoundException if the configured backup directory doesn't exist.
     * @throws FileExistsException if the backup filename configured to be used is already taken.
     * @throws IllegalXMLCharactersException if the backup data contains illegal characters for XML formatting.
     * @throws Exception for no good reason.
     */
    public String exportData() throws FileNotFoundException, FileExistsException, IllegalXMLCharactersException, Exception;

    /**
     * Returns the path to be used for backing up data.
     * @return the path.
     * @throws FileNotFoundException if configuration of the backup paths forces a path that doesn't exist.
     * @throws FileExistsException if a file already exists at the path to which export should go.
     * @throws IOException if there is a problem with the filesystem when determining the filepath.
     */
    public String getExportFilePath() throws FileNotFoundException, FileExistsException, IOException;
}
