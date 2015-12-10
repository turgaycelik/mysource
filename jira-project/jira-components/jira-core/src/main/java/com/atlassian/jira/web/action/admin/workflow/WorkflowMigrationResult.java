/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import java.io.Serializable;
import java.util.Map;

import com.atlassian.jira.util.ErrorCollection;

/**
 * This class communicates the results of migrating a project to a new workflow scheme.
 * <p>
 * The result could be either {@link #SUCCESS} or {@link #TERMINATED}. This object also communicates
 * if there were any errors detected <strong>before</strong> migrating <strong>any</strong> issues to the new workflow or
 * the number of issues that failed during migration. If there were failed issues, this object contains the issue
 * ids and keys of failed issues.
 * </p>
 * <p>
 * The workflow migration is made to tolerate several failures. This ensures that if migrating a project with a large
 * number of issues and only a few (a number lower than an agreed threshold) of them fail, the migration proceeds and
 * does not leave the project in an inconsistent state.
 * </p>
 * <p>
 * Therefore, even if the result is {@link #SUCCESS}, the object could contain information about one or more failed
 * issues. To determine the number of failed issues, call {@link #getNumberOfFailedIssues()} method. To get a
 * {@link java.util.Map} of issue ids to issue key mappings call {@link #getFailedIssues()} method.
 * </p>
 * <p>
 * If there were no failures, (a perfect migration). The {@link #getNumberOfFailedIssues()} method will return 0 and the
 * {@link #getFailedIssues()} method will return an empty map.
 * </p>
 * <p>
 * If the result is {@link #SUCCESS} the object should <strong>never</strong> have any error messages. Error messages
 * are detected before the migration begins. If any errors are detected a {@link #TERMINATED} result code should be
 * returned.
 * </p>
 * <p>
 * If the result is {@link #TERMINATED}, then the object can either contain one or more errors that were detected
 * <strong>before</strong> any of the issues were migrated, or <strong>no</strong> errors and one or more failed issues.
 * At <strong>no time</strong> can the object have errors as well as failed issues.
 * </p>
 * <p>
 * If {@link #TERMINATED} is returned and the object contains one or more errors, then the migration did not
 * actually occur as there were errors found with the data before migrating any issues.
 * </p>
 * <p>
 * If failed issues are returned, then no errors were found before the migration, but the migration terminated before
 * completion as there were too many failures (more failures than the agreed threshold). In this case, the project is
 * likely left in an inconsistent state and users should be warned.
 * </p>
 */
public interface WorkflowMigrationResult extends Serializable
{
    /**
     * The workflow migration has succeeded. However, there could still have been failures migrating
     * several issues. See {@link #getNumberOfFailedIssues()} and {@link #getFailedIssues()}.
     */
    static int SUCCESS = 0;

    /**
     * The workfow migration either did not occur as there were problems found with the data before
     * starting the migration. In this, there will be one or more errors. See {@link #getErrorCollection()}.
     * <p>
     * Otherwise, the migration could have been stopped before completion as too many failures were encountered.
     * In this case {@link #getNumberOfFailedIssues()} should return a positive number and {@link #getFailedIssues()}
     * should return the issue ids and issue keys.
     * </p>
     */
    static int TERMINATED = 10;

    /**
     * Returns the result of the workflow migration.
     * @return Either {@link #SUCCESS} or {@link #TERMINATED}
     */
    int getResult();

    /**
     * Returns a collection of errors that were found before starting the migration.
     */
    ErrorCollection getErrorCollection();

    /**
     * The number of issues that failed during the migration, or until the migration was terminated.
     * @return 0 if no failures were encountered, or if the migration did not start due to found errors
     * (see {@link #getNumberOfFailedIssues()}). Or the number of failures during the migration, or until the
     * migration was terminated.
     */
    int getNumberOfFailedIssues();

    /**
     * A {@link java.util.Map} of issue ids to issue keys of failed issues.
     * @return an empty map if {@link #getNumberOfFailedIssues()} is 0 or a mapping of issue id to issue key
     * for every failed issue during the migration.
     */
    Map<Long, String> getFailedIssues();
}
