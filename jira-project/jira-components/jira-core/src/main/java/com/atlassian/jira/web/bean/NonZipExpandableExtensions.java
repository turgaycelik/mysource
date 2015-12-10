package com.atlassian.jira.web.bean;

/**
 * Represents a list of file extensions which should not be expanded as a zip, even if the underlying file is a
 * compressed zip file.
 *
 * @since v4.2
 */
public interface NonZipExpandableExtensions
{
    /**
     * Determines whether the specified extension is contained in the the list of file extensions which should not be
     * expanded as a zip.
     * @param extension The extension to check. Should not be null
     * @return true, the specified extension is contained in the the list of file extensions which should not be
     * expanded as a zip; otherwise, false.
     */
    boolean contains(String extension);

    /**
     * Returns a comma-separated list of non-expandable file extensions.
     *
     * @return list of non-expandable file extensions.
     */
    String getNonExpandableExtensionsList();
}
