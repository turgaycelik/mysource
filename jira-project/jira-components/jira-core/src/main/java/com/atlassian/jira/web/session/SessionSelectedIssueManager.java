package com.atlassian.jira.web.session;

import com.atlassian.jira.util.NonInjectableComponent;

/**
 * Provides access to getting and setting the selected issue (stored as a {@link Long}) in session.
 *
 * @since v4.2
 * @see SessionSearchObjectManagerFactory#createSelectedIssueManager()
 * @see SessionSearchObjectManagerFactory#createSelectedIssueManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createSelectedIssueManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 */
@NonInjectableComponent
public interface SessionSelectedIssueManager extends SessionSearchObjectManager<SessionSelectedIssueManager.SelectedIssueData>
{
    public static class SelectedIssueData
    {
        private final Long selectedIssueId;
        private final int selectedIssueIndex;
        private final Long nextIssueId;

        public SelectedIssueData(final Long selectedIssueId, final int selectedIssueIndex, final Long nextIssueId)
        {
            this.selectedIssueId = selectedIssueId;
            this.selectedIssueIndex = Math.max(0, selectedIssueIndex);
            this.nextIssueId = nextIssueId;
        }

        public Long getSelectedIssueId()
        {
            return selectedIssueId;
        }

        /**
         * The 0-based index, with respect to the entire search results, of the selected issue.
         */
        public int getSelectedIssueIndex()
        {
            return selectedIssueIndex;
        }

        public Long getNextIssueId()
        {
            return nextIssueId;
        }
    }
}
