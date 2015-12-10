define('jira/dialog/init-workflow-transition-dialogs', [
    'jira/dialog/dialog-register',
    'jira/dialog/dialog-util',
    'jira/dialog/form-dialog',
    'jira/issuenavigator/issue-navigator',
    'aui/tabs',
    'jquery',
    'exports'
], function(
    DialogRegister,
    DialogUtil,
    FormDialog,
    IssueNavigator,
    AuiTabs,
    jQuery,
    exports
) {
    // Workflow transition dialogs
    var workflowLinkSelector = "a.issueaction-workflow-transition";

    exports.init = function() {
        jQuery(document).delegate(workflowLinkSelector, "click", function(event) {
            event.preventDefault();
            var link = jQuery(event.target).closest(workflowLinkSelector);
            var action = parseUri(link.attr('href')).queryKey.action;
            if (action) {
                var id = "workflow-transition-" + action + "-dialog";
                var $trigger = jQuery(this);
                if (!DialogRegister[id]) {
                    /**
                     * we don't pass "url" below as it would break {@link DialogUtil#getDefaultAjaxOptions} which has to
                     * get URL dynamically from triggering DOM element (<a>)
                     */
                    DialogRegister[id] = new FormDialog({
                        id: id,
                        // Action might be in the middle or in the end of 'href' attribute.
                        // In practice only one of these should be invoked.
                        trigger: [
                            'a[href*="action=' + action + '&"].issueaction-workflow-transition',
                            'a[href$="action=' + action + '"].issueaction-workflow-transition'
                        ],
                        widthClass: "large",
                        handleRedirect: true,
                        ajaxOptions: DialogUtil.getDefaultAjaxOptions,
                        onSuccessfulSubmit : DialogUtil.storeCurrentIssueIdOnSucessfulSubmit,
                        delayShowUntil: DialogUtil.BeforeShowIssueDialogHandler.execute,
                        issueMsg : 'thanks_issue_transitioned',
                        onContentRefresh: function () {
                            // initialise AJS tabs for the workflow dialogs
                            AuiTabs.setup();
                        },
                        isIssueDialog: true
                    });
                    DialogRegister[id].$activeTrigger = $trigger; // that's necessary for the first run only
                    // as later on AJS will set it when triggered automatically
                    DialogRegister[id].show();
                }
            }
        });
    }
});
