package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.util.ErrorCollection;

import java.util.List;
import java.util.Set;

/**
 * The RestFieldOperationsHandler is called by the issue rest source to handle the update issue request.
 *
 * @since v5.0
 */
@PublicSpi
public interface RestFieldOperationsHandler
{
    /**
     * Returns a list of operation names which are supported by this field.
     *
     * @return a set of supported Operations
     */
    Set<String> getSupportedOperations();

    /**
     * This method has to set the field value(s) in the {@link com.atlassian.jira.issue.IssueInputParameters} based on the operations.
     * The issue service will validate the new field values in the IssueInputParameters, thus all permission checks are done by the issue service.
     *
     *
     * @param issueCtx the Issue Context , i.e. Project IssueType
     * @param issue the issue (may be null when creating an issue)
     * @param fieldId The Id of the field being processed.
     * @param inputParameters the inputparameters containing all updatd field values.
     * @param operations the operations to perform for this field. Has to be one of the supported operations.
     *
     * @return contains errors, if there was a problem when setting the field values on the IssueInputParameters. No errors if it was able to update the IssueInputParameters.
     */
    ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations);

}
