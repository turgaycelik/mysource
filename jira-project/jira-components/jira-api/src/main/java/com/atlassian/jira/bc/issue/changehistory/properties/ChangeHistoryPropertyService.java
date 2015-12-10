package com.atlassian.jira.bc.issue.changehistory.properties;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.issue.changehistory.ChangeHistory;

/**
 * The service used to add, update, retrieve and delete properties from {@link com.atlassian.jira.issue.changehistory.ChangeHistory}s.
 * Each method of this service ensures that the user has permission to perform the operation. For each operation an appropriate event is published.
 *
 * @since JIRA 6.3
 */
@ExperimentalApi
public interface ChangeHistoryPropertyService extends EntityPropertyService<ChangeHistory>
{
}
