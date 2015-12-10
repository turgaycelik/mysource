package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.scheme.Scheme;

import java.util.Collection;

/**
 * This defines a interface for an object that is able to take a collection of {@link com.atlassian.jira.scheme.Scheme} objects
 * and determine if these objects are the same (i.e. - all the {@link com.atlassian.jira.scheme.SchemeEntity}'s in the scheme
 * are the same). This will then take all the same schemes and create a new 'distilled' scheme from
 * these. This object will then be able to persist this new scheme and update all project associations
 * from the original 'undistilled' schemes to the newly created 'distilled' scheme.
 */
public interface SchemeDistiller
{

    /**
     * This method does the work of analyzing and distilling, if possible, the passed in {@link com.atlassian.jira.scheme.Scheme}'s.
     * @param schemes a collection of {@link com.atlassian.jira.scheme.Scheme} objects representing the schemes that you would like to
     * try to 'distill'.
     * @return a DistilledSchemeResults object which represent the result of the 'distilling' operation.
     * There will be one DistilledSchemeResults for each successfully distilled set of schemes. You can access the
     * 'undistilled' schemes via the object See the javadoc for {@link DistilledSchemeResults} for full details.
     * The object will contain empty collections if null or an empty collection is passed in.
     */
    public DistilledSchemeResults distillSchemes (Collection schemes);

    /**
     * This method will persist the result of the 'distilling' operation. The newly created scheme will be saved
     * to persistent store and the project associations will be updated to point to the new scheme.
     * @param distilledSchemeResult
     * @return the new Scheme that was created and assoicated. This will return a null scheme if the DistilledSchemeResult.getType() is
     * not of a Notification or Permission type.
     * @exception DataAccessException if something goes wrong at the db level.
     */
    public Scheme persistNewSchemeMappings (DistilledSchemeResult distilledSchemeResult) throws DataAccessException;

    /**
     * This method will return a SchemeRelationships object for the internal collections of distilled
     * scheme results. These will show if each scheme entity type matches each other, or not at all.
     * @param distilledSchemeResults
     * @return a collection of SchemeRelationships objects.
     */
    public SchemeRelationships getSchemeRelationships(DistilledSchemeResults distilledSchemeResults);
}
