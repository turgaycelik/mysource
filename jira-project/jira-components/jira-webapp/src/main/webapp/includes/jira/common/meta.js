/**
 * JIRA.Meta represents meta-state about the current JIRA page - logged in user, etc.
 *
 * @deprecated add metadata to more appropriate business objects.
 */
AJS.namespace('JIRA.Meta');
(function() {
    var Meta = require('jira/util/data/meta');
    var params = require('aui/params');

    /**
     * @return {Object} the currently logged-in user
     * @deprecated use the {@amd jira/util/users} module instead.
     */
    JIRA.Meta.getLoggedInUser = function() {
        return {
            name: Meta.get('remote-user'),
            fullName: Meta.get('remote-user-fullname')
        };
    };

    /**
     * @return {String} the Project key for the currently-viewed issue. Blank if the current page isn't for an issue or project.
     * @deprecated Doesn't work.
     */
    JIRA.Meta.getProject = function() {
        return params.projectKey;
    };

    /**
     * @return {String} the key of the currently-viewed issue. Blank if the current page isn't for a viewed issue.
     * @deprecated use the {@amd jira/issue} module instead.
     */
    JIRA.Meta.getIssueKey = function() {
        return Meta.get('issue-key');
    };
})();
