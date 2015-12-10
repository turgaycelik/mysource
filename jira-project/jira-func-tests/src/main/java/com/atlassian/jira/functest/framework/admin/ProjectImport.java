package com.atlassian.jira.functest.framework.admin;

import java.io.File;

/**
 * Framework for executing project imports.
 *
 * @since v4.1
 */
public interface ProjectImport
{
    File doImportToSummary(String backupFileName, String currentSystemXML, final String attachmentPath);
}
