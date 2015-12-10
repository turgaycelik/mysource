package com.atlassian.jira.bc.project.property;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.entity.property.EntityWithKeyPropertyService;
import com.atlassian.jira.project.Project;


/**
 * The service used to add, update, retrieve and delete properties from {@link com.atlassian.jira.project.Project}'s. Each method of this service
 * ensures that the user has permission to perform the operation. For each operation an appropriate event is published.
 *
 * @since v6.2
 */
@ExperimentalApi
public interface ProjectPropertyService extends EntityWithKeyPropertyService<Project>
{
}