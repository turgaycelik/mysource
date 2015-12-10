define('jira/issuenavigator/issue-navigator', [
    'jira/data/session-storage',
    'jira/util/browser',
    'jquery'
], function(
    SessionStorage,
    Browser,
    jQuery
) {
    /**
     * Represents an the Issue Navigator page.  This class should be used to retrieve information from the
     * issue navigator such as the currently selected row, currently selected issue key and so on.
     *
     * @class IssueNavigator
     */
    var IssueNavigator = {
        /**
         * Checks if we are currently viewing the issue navigator.
         *
         * @method isNavigator
         * @return {Boolean} true if the current page is the issue navigator, false otherwise
         */
        isNavigator: function() {
            return jQuery("#isNavigator").length === 1;
        },

        /**
         * Sets a message to be displayed when the navigator has been reloaded
         *
         * @param options
         */
        setIssueUpdatedMsg: function (options) {

            options = options || {};

            var issueMsg = options.issueMsg,
                issueId = options.issueId,
                issueKey = options.issueKey;

            if (!issueId) {
                issueId = this.getSelectedIssueId();
                issueKey = this.getSelectedIssueKey();
            }

            if (issueId) {
                SessionStorage.setItem('selectedIssueId', issueId);
            }

            if (issueKey) {
                SessionStorage.setItem('selectedIssueKey', issueKey);
            }

            if (issueMsg) {
                SessionStorage.setItem('selectedIssueMsg', issueMsg);
            }
        },

        /**
         * Reloads the issue navigator
         */
        reload: function () {
            Browser.reloadViaWindowLocation();
        },

        /**
         * Checks if any row is currently selected on the issue navigator. This can be the case for
         * an empty searchr, or if keyboard shortcuts are disabled.
         *
         * @method isRowSelected
         * @return {Boolean} true if a selected issue row exists, false otherwise
         */
        isRowSelected: function() {
            return IssueNavigator.get$focusedRow().length !== 0;
        },

        /**
         * Returns a jQuery wrapped object representing the currently selected issue row.
         *
         * @method getFocusedRow
         * @return {jQuery} the jQuery wrapped issue row representing the currently selected row
         */
        get$focusedRow: function() {
            return jQuery("#issuetable tr.issuerow.focused");
        },

        /**
         * Gets the index of the focused issue.
         *
         * @method getFocsuedIssueIndex
         * @return {Number} - The index of the focused issue in the current search result set.
         */
        getFocsuedIssueIndex: function() {
            var rowIndex = jQuery("#issuetable").find("tr.issuerow").index(this.get$focusedRow());
            var searchOffset = parseInt(jQuery('.results-count-start').first().text(), 10) - 1;
            return rowIndex + searchOffset;
        },

        /**
         * Returns the issue key for the currently selected row.
         *
         * @method getSelectedIssueKey
         * @return {String} The issue key for the currently focused row or undefined if none exists.
         */
        getSelectedIssueKey: function() {
            var $focusedRow = IssueNavigator.get$focusedRow();
            if ($focusedRow.length !== 0) {
                return $focusedRow.attr("data-issuekey");
            }
            return undefined;
        },

        /**
         * Returns the issue id for the currently selected row.
         *
         * @method getSelectedIssueId
         * @return {String} The issue id for the currently focused row or undefined if none exists.
         */
        getSelectedIssueId: function() {
            return IssueNavigator.get$focusedRow().attr("rel");
        },

        /**
         * Returns the issue id for the next row after the currently selected row.
         *
         * Note: It is a known issue that no id will be returned when the last issue on the page is
         * focused. In future, the return value in this situation may change.
         *
         * @method getNextIssueId
         * @return {String} The issue id for the next issue after the currently focused row or undefined if none exists.
         */
        getNextIssueId: function() {
            return IssueNavigator.get$focusedRow().next("tr.issuerow").attr("rel");
        }
    };

    return IssueNavigator;
});

/** Preserve legacy namespace
    @deprecated jira.app.issuenavigator */
AJS.namespace("jira.app.issuenavigator", null, require('jira/issuenavigator/issue-navigator'));
AJS.namespace('JIRA.IssueNavigator', null, require('jira/issuenavigator/issue-navigator'));
