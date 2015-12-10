package com.atlassian.jira.security.plugin;

import javax.annotation.Nullable;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Implementations of this module are supposed to provide the decision if the user has the permission to the project.
 */
public interface ProjectPermissionOverride
{
    enum Decision {DENY, ABSTAIN}

    /**
     * The implementation of this method is supposed to either deny permissions or abstain from decision if the user
     * has permissions to the selected project.
     * It is not allowed to override global permissions and BROWSE permission to the project.
     *
     * @param projectPermissionKey identifier of the project permission.
     * @param project project to which permissions are overridden. This can be null when check is performed for anonymous user.
     * @param applicationUser whose permissions are going to be overriden.
     * @return the decision.
     */
    Decision hasPermission(ProjectPermissionKey projectPermissionKey, Project project, @Nullable ApplicationUser applicationUser);

    /**
     * The implementation of this method should return a description explaining how does the permission overriding
     * affects if the user has permissions to the selected project.
     *
     * @param projectPermissionKey identifier of the project permission.
     * @param project project to which permissions are checked.
     * @param applicationUser whose permissions are checked. This can be null when check is performed for anonymous user.
     * @return the reason.
     */
    Reason getReason(ProjectPermissionKey projectPermissionKey, Project project, @Nullable ApplicationUser applicationUser);

    /**
     * Justification of the decision made by {@link #hasPermission(ProjectPermissionKey, com.atlassian.jira.project.Project, com.atlassian.jira.user.ApplicationUser)} method.
     */
    @ExperimentalApi
    class Reason
    {
        /**
         * Key of the i18n entry for summary of the decision.
         */
        private final String summary;
        /**
         * Key of the i18n detailed explanation of the decision.
         */
        private final String details;

        public Reason(final String summary, final String details)
        {
            this.summary = summary;
            this.details = details;
        }

        public String getSummary()
        {
            return summary;
        }

        public String getDetails()
        {
            return details;
        }
    }

}
