package com.atlassian.jira.web.action.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

import java.util.List;

/**
 * A utility interface to help determine which issue types are being created most often for a project/user context.
 *
 * Note that none of these methods ever deal in sub tasks, only "full" issue types are considered.
 *
 * @since v4.0
 */
public interface PopularIssueTypesUtil
{
    /**
     * Returns the most popular non-subtask issue types used for issue creation in the specified project and by the
     * specified user, within a designated time frame.
     *
     * If the user is not specified, or has not created any issues in that time frame, this method returns the most
     * popular issue types for the entire project.
     *
     * The number of issue types returned by this method is hard-coded in the implementation. If the number of popular
     * issue types available is less than that, the remainder of the list will be filled out with any available
     * issue types from the project, in the order of which they are defined in the issue type scheme.
     *
     * @param project the project to query
     * @param user the reporter to query; use null if the reporter is not important
     * @return the list of popular issue types, limited to a hard-coded number; never null.
     */
    List<IssueType> getPopularIssueTypesForProject(Project project, User user);

    /**
     * Returns the set difference between all available issue types for a project, and the popular issue types for a
     * project/user context.
     *
     * @param project the project to query
     * @param user the reporter to query; use null if the reporter is not important
     * @return the rest of the issue types; never null.
     */
    List<IssueType> getOtherIssueTypesForProject(Project project, User user);

    /**
     * Returns a holder that contains both - popular and other issue Types.  It is basically the conglomerate result of:
     * {@link #getPopularIssueTypesForProject(com.atlassian.jira.project.Project, User)}
     * And
     * {@link #getOtherIssueTypesForProject(com.atlassian.jira.project.Project, User)}
     *
     * This is more performant than the two seperate calls.
     *
     * @param project the project to query
     * @param user the reporter to query; use null if the reporter is not important
     * @return a list of popular issue types and a list of other issues (with popular removed)
     */
    PopularIssueTypesHolder getPopularAndOtherIssueTypesForProject(Project project, User user);

    public static class PopularIssueTypesHolder
    {
        private final List<IssueType> popularIssueTypes;
        private final List<IssueType> otherIssueTypes;


        public PopularIssueTypesHolder(List<IssueType> popularIssueTypes, List<IssueType> otherIssueTypes)
        {
            this.popularIssueTypes = popularIssueTypes;
            this.otherIssueTypes = otherIssueTypes;
        }

        public List<IssueType> getPopularIssueTypes()
        {
            return popularIssueTypes;
        }

        public List<IssueType> getOtherIssueTypes()
        {
            return otherIssueTypes;
        }
    }
}
