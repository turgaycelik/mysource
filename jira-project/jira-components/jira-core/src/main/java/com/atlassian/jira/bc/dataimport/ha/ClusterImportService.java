package com.atlassian.jira.bc.dataimport.ha;

/**
 * When an import takes place on any other node in the cluster this service
 * is responsible for syncing back the state
 *
 * @since v6.1
 */
public interface ClusterImportService
{
    /**
     * Perform import operations required on the destination node.
     * @param fileName Name of the index backup file to be imported.
     */
    void doImport(String fileName);

    void prepareImport();
}
