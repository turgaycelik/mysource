package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;

/**
 * Interface for fields which can be modified using the rest api.
 *
 * @since v5.0
 */
@PublicApi
public interface RestFieldOperations
{
    /**
     * @return the RestFieldOperationsHandler for this field.
     */
    RestFieldOperationsHandler getRestFieldOperation();

    /**
     * Return The default data for this system field.
     * May be null if there is no default.
     * @param issueCtx            Issue (This should really only need to be an issue context,  but for historical reasons we need an issue object.
     */
    JsonData getJsonDefaultValue(IssueContext issueCtx);

}
