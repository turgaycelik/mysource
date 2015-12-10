package com.atlassian.jira.plugins.share;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Shares entities such as issues, saved searches or unsaved searches via e-mail.
 *
 * @since v5.0
 */
public interface ShareService
{
    /**
     * Confirms that the shareBean param contains either usernames or e-mails and also validates that the user
     * performing this operation has the permission to view other users in this JIRA instance.
     *
     * @param remoteUser The user performing the share operation
     * @param shareBean Bean containing information about who to share with
     * @param issue The issue being shared
     * @return A service result
     */
    ValidateShareIssueResult validateShareIssue(final ApplicationUser remoteUser, final ShareBean shareBean, final Issue issue);

    /**
     * Deprecated - use {@link #validateShareIssue(com.atlassian.jira.user.ApplicationUser, ShareBean, com.atlassian.jira.issue.Issue)} instead.
     */
    @Deprecated
    ValidateShareIssueResult validateShareIssue(final User remoteUser, final ShareBean shareBean, final Issue issue);

    /**
     * Given a valid ValidateShareIssueResult this method will send an e-mail to the users and fire a {@link
     * com.atlassian.jira.plugins.share.event.ShareIssueEvent}
     *
     * @param result a valid validateShareIssueResult.
     */
    void shareIssue(ValidateShareIssueResult result);

    /**
     * Confirms that the shareRequest param contains either usernames or e-mails and also validates that the user
     * performing this operation has the permission to view other users in this JIRA instance.
     *
     * @param remoteUser The user performing the share operation
     * @param shareBean Bean containing information about who to share with
     * @param searchRequest A saved filter being shared or null if a unsaved JQL search is being shared
     * @return A service result
     */
    ValidateShareSearchRequestResult validateShareSearchRequest(final ApplicationUser remoteUser, final ShareBean shareBean, final SearchRequest searchRequest);

    /**
     * Deprecated - use {@link #validateShareSearchRequest(com.atlassian.jira.user.ApplicationUser, ShareBean, com.atlassian.jira.issue.search.SearchRequest)} instead.
     */
    @Deprecated
    ValidateShareSearchRequestResult validateShareSearchRequest(final User remoteUser, final ShareBean shareBean, final SearchRequest searchRequest);

    /**
     * Given a valid ValidateShareIssueResult this method will send an e-mail to the users and fire a {@link
     * com.atlassian.jira.plugins.share.event.ShareJqlEvent} or {@link com.atlassian.jira.plugins.share.event.ShareSearchRequestEvent}
     * depending on if a saved filter was passed to the validate method.
     *
     * @param result a valid validateShareIssueResult.
     */
    void shareSearchRequest(ValidateShareSearchRequestResult result);


    static abstract class ValidateShareResult extends ServiceResultImpl
    {
        protected final ApplicationUser user;
        protected final ShareBean shareBean;

        public ValidateShareResult(ErrorCollection errorCollection, final ApplicationUser remoteUser, final ShareBean shareBean)
        {
            super(errorCollection);
            this.user = remoteUser;
            this.shareBean = shareBean;
        }

        public User getRemoteUser()
        {
            return this.user.getDirectoryUser();
        }

        public ApplicationUser getUser()
        {
            return user;
        }

        public ShareBean getShareBean()
        {
            return shareBean;
        }
    }

    static class ValidateShareIssueResult extends ValidateShareResult
    {

        private final Issue issue;

        public ValidateShareIssueResult(ErrorCollection errorCollection, final ApplicationUser remoteUser, final ShareBean shareBean, final Issue issue)
        {
            super(errorCollection, remoteUser, shareBean);
            this.issue = issue;
        }

        public Issue getIssue()
        {
            return issue;
        }
    }

    static class ValidateShareSearchRequestResult extends ValidateShareResult
    {
        private final SearchRequest searchRequest;

        public ValidateShareSearchRequestResult(ErrorCollection errorCollection, final ApplicationUser remoteUser, final ShareBean shareBean, final SearchRequest SearchRequest)
        {
            super(errorCollection, remoteUser, shareBean);
            this.searchRequest = SearchRequest;
        }

        public SearchRequest getSearchRequest()
        {
            return searchRequest;
        }
    }

}
