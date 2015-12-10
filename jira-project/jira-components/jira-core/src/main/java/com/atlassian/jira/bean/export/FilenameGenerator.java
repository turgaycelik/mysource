package com.atlassian.jira.bean.export;

import java.io.File;
import java.io.IOException;

/**
 * Strategy pattern for abstracting filename generation such as in unique
 * filenames or timestamp-based ones.
 */
public interface FilenameGenerator
{
    /**
     * Attempts to generate a File with a name using the implementor's chosen
     * strategy. This contract only pertains to the generation of the File object,
     * not to the creation of the actual physical file on disk.
     * @param basepath absolute or relative prefix path for the filename.
     * @return The full path to a prospective file.
     * @throws IOException if a filename cannot be generated for the given basepath.
     */
    public File generate(String basepath) throws IOException;
}
