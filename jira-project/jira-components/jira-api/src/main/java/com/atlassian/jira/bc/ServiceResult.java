package com.atlassian.jira.bc;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.ErrorCollection;

/**
 * This interface defines a service method call result in JIRA that can contain human readable errors. New service
 * methods should prefer the generic {@link ServiceOutcome}.
 *
 * @since v4.0
 * @see ServiceOutcome
 */
@PublicApi
public interface ServiceResult
{
    /**
     * @return true if there are no errors, false otherwise.
     */
    boolean isValid();

    /**
     * @return an {@link ErrorCollection} that contains any errors that may have happened as a result of the validations.
     */
    ErrorCollection getErrorCollection();
}