package com.atlassian.jira.bc.scheme.distiller;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResults;
import com.atlassian.jira.scheme.distiller.SchemeRelationships;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;

/**
 * This is a service wrapper around the manager implementation.  All validation will be carried out in the
 * service.
 */
public interface SchemeDistillerService
{
      /**
     * This method does the work of analyzing and distilling, if possible, the passed in {@link com.atlassian.jira.scheme.Scheme}'s.
     * @param schemes a collection of {@link com.atlassian.jira.scheme.Scheme} objects representing the schemes that you would like to
     * try to 'distill'.
     * @return a DistilledSchemeResults object which represent the result of the 'distilling' operation.
     * There will be one DistilledSchemeResults for each successfully distilled set of schemes. You can access the
     * 'undistilled' schemes via the object See the javadoc for {@link com.atlassian.jira.scheme.distiller.DistilledSchemeResults} for full details.
     * The object will contain empty collections if null or an empty collection is passed in.
     */
    public DistilledSchemeResults distillSchemes(User user, Collection schemes, ErrorCollection errorCollection);

    /**
     * This method will persist the result of the 'distilling' operation. The newly created scheme will be saved
     * to persistent store and the project associations will be updated to point to the new scheme.
     * @param distilledSchemeResult
     * @return the new Scheme that was created and assoicated. This will return a null scheme if the DistilledSchemeResult.getType() is
     * not of a Notification or Permission type.
     * @exception com.atlassian.jira.exception.DataAccessException if something goes wrong at the db level.
     */
    public Scheme persistNewSchemeMappings(User user, DistilledSchemeResult distilledSchemeResult, ErrorCollection errorCollection) throws DataAccessException;

    /**
     * This method will return a SchemeRelationships object for the internal collections of distilled
     * scheme results. These will show if each scheme entity type matches each other, or not at all.
     * @param distilledSchemeResults
     * @return a collection of SchemeRelationships objects.
     */
    public SchemeRelationships getSchemeRelationships(User user, DistilledSchemeResults distilledSchemeResults, ErrorCollection errorCollection);

    /**
     * This method checks if the new scheme name passed in already exists. If the scheme name exists an error will be added to
     * the error collection. The error will be added for the passed in fieldName if it is non-null.
     * @param newSchemeName
     */
    public void isValidNewSchemeName(User user, String fieldName, String newSchemeName, String schemeType, ErrorCollection errorCollection);
}
