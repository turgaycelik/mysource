/**
 * Initialises project picker frother fields
 *
 * Please not that project fields interact with issue type fields in create issue. This interation is handled in
 * initIssueTypeSelect.js
 */

(function () {

    function createProjectPicker(context) {
        context.find(".project-field").each(function () {
            new AJS.SingleSelect({
                element: this,
                revertOnInvalid: true
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createProjectPicker(context);
        }
    });

})();

