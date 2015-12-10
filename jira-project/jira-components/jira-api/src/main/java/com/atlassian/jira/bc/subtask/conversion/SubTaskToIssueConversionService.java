package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.annotations.PublicApi;

/**
 * Service class to reveal all business logic in converting a sub-task to an issue, including validation.
 * This business component should be used by all clients: web, rpc-soap, jelly, etc.
 * No additional methods needed.
 */
@PublicApi
public interface SubTaskToIssueConversionService extends IssueConversionService
{

}
