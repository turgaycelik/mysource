package com.atlassian.jira.bc.dataimport;

import com.atlassian.jira.bc.dataimport.DataImportService;

/**
 * Stores the result of the last import or XmlRestore.
 */
public interface ImportResultStore
{
    void storeResult(DataImportService.ImportResult importResult);

    DataImportService.ImportResult getLastResult();

    void clear();
}
