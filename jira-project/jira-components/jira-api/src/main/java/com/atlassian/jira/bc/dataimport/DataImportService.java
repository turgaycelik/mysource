package com.atlassian.jira.bc.dataimport;

import java.io.Serializable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * Performs an XML import used both during setup and xml restore.  XML import is an action only system administrators
 * can perform.
 * <p/>
 * This service is responsible for performing some basic validation of the input as well as more advanced validation of
 * the actual data provided. An import consists of the following steps:
 * <ul>
 *     <li>Basic input validation ({@link #validateImport(com.atlassian.crowd.embedded.api.User, DataImportParams)}</li>
 *     <li>Initial parse of the XML file for validation purposes</li>
 *     <li>Checks to ensure the build number of the data is not too old or newer than the current buildnumber</li>
 *     <li>Checks to ensure the license is valid (either from the user provided input or from the data)</li>
 *     <li>Checks to ensure the indexing & attachment directories exist and are writable</li>
 *     <li>If there's any errors at this stage the import will be aborted</li>
 *     <li>Otherwise we start to perform the import</li>
 *     <li>Shutdown the scheduler and flush the current mailqueue</li>
 *     <li>Remove all entities from the database</li>
 *     <li>Parse the XML file again this time creating entities in the database</li>
 *     <li>Refresh the ofbiz id sequencer</li>
 *     <li>Refresh picocontainer including the plugins system</li>
 *     <li>Run the DB consistency checker</li>
 *     <li>Perform a DB upgrade if necessary</li>
 *     <li>Run a reindex of the data</li>
 *     <li>Restart the scheduler</li>
 * </ul>
 *
 * @since v4.4
 */
public interface DataImportService
{

    /**
     * Given the currently loggedInUser and import params this method does some basic validation of the input. This
     * method returns quickly.  It does *not* parse the entire XML file.  It can be used to check that the user has
     * entered all the required input correctly for the front-end.
     * <p/>
     * In particular this method checks if the user has systemadmin permission.  If this method is called from Setup (as
     * indicated by the DataImportParams) the permission check is skipped.  Further it will validate that the import xml
     * file exists and verify that the license is valid (if one was provided).
     *
     * @param loggedInUser The currently logged in user (may be null during setup)
     * @param params The {@link DataImportParams} contain information provided by the user during import or setup
     * @return A validation result with the input as well as any errors.
     */
    ImportValidationResult validateImport(final User loggedInUser, final DataImportParams params);

    /**
     * Performs the actual import given a valid validation result.  This method is slow! It performs the majority of the
     * tasks outlined in the interface definition. Note that the method will be sped up by setting the system property
     * jira.dangermode to "true" BUT WARNING, if you do this and the import fails, JIRA may be left in an unusable state
     * which is why this method is only provided for speeding up import operations in the context of automated testing.
     *
     *
     * @param loggedInUser The currently logged in user (may be null during setup)
     * @param result A valid validation result containing the {@link com.atlassian.jira.bc.dataimport.DataImportParams} provided
     * @param taskProgressSink A task progress counter that can be used to indicate how much longer the import has to go.
     *        if no progress needs to be recorded simply provide a {@link TaskProgressSink#NULL_SINK}
     * @return The import result potentially containing an errorcollection and more specific overall error result
     */
    ImportResult doImport(final User loggedInUser, final ImportValidationResult result, TaskProgressSink taskProgressSink);

    /**
     * Returned by the {@link ImportResult} from a call to {@link DataImportService#doImport(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.dataimport.DataImportService.ImportValidationResult, TaskProgressSink)}
     */
    enum ImportError
    {
        /**
         * No error happened.
         */
        NONE,

        /**
         * An error occurred when the data was being upgraded to the latest version.
         */
        UPGRADE_EXCEPTION,

        /**
         * The license provided (either via the input or from the data) was an old version 1 license that is no longer
         * compatible with this version of JIRA.
         */
        V1_LICENSE_EXCEPTION,

        /**
         * The index or attachment path provided by the data was not valid.  Meaning it didn't exist or the directory
         * was not writable.
         */
        CUSTOM_PATH_EXCEPTION,

        /**
         * The data being imported is from a newer version of JIRA.
         */
        DOWNGRADE_FROM_ONDEMAND
    }


    /**
     * An import validation result returned by the {@link DataImportService#validateImport(com.atlassian.crowd.embedded.api.User,
     * DataImportParams)} method.  This result contains the import parameters passed in as well as any errors in the
     * provided errorcollection.
     */
    public static class ImportValidationResult extends ServiceResultImpl
    {
        private final DataImportParams params;

        public ImportValidationResult(ErrorCollection errorCollection, final DataImportParams params)
        {
            super(errorCollection);
            this.params = params;
        }

        public DataImportParams getParams()
        {
            return params;
        }
    }

    /**
     * Represents the result of performing an import.  Clients should use the provided {@link ImportResult.Builder} to
     * construct an instance of this class.  It contains a general collection of errors to display in the UI.
     * <p/>
     * It can also return a more specific error message via the {@link ImportError} to indicate a large problem during
     * the import which will result in JIRA being locked after the import.
     */
    public static class ImportResult extends ServiceResultImpl implements Serializable
    {
        private final ImportError error;
        private String specificErrorMessage;
        // This is a nasty hack but we do not need to pass these across the network.  Data import is not actually
        // supported in a cluster.
        private transient DataImportParams params;

        private ImportResult(DataImportParams params, ErrorCollection errorCollection, ImportError error, String specificErrorMessage)
        {
            super(errorCollection);
            this.params = params;
            this.error = error;
            this.specificErrorMessage = specificErrorMessage;
        }

        public ImportError getImportError()
        {
            return error;
        }

        public String getSpecificErrorMessage()
        {
            return specificErrorMessage;
        }

        public boolean isValid()
        {
            return super.isValid() && error.equals(ImportError.NONE);
        }

        public DataImportParams getParams()
        {
            return params;
        }

        public static class Builder
        {
            private final ErrorCollection errors = new SimpleErrorCollection();
            private ImportError error = ImportError.NONE;
            private String specificErrorMessage;
            private final DataImportParams params;

            public Builder(final DataImportParams params)
            {
                this.params = params;
            }

            public ErrorCollection getErrorCollection()
            {
                return errors;
            }

            public void setSpecificError(ImportError error, String specificErrorMessage)
            {
                this.error = error;
                this.specificErrorMessage = specificErrorMessage;
            }

            public ImportResult build()
            {
                return new ImportResult(params, errors, error, specificErrorMessage);
            }

            public boolean isValid()
            {
                return !errors.hasAnyErrors() && error.equals(ImportError.NONE);
            }
        }
    }
}
