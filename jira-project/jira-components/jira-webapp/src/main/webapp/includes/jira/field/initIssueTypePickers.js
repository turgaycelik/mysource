/**
 * Initialises issuetype picker frother fields
 *
 * Please not that project fields interact with issue type fields in create issue. This interation is handled in
 * initIssueTypeSelect.js
 */

(function () {

    function createissueTypePicker(context) {
        context.find(".issuetype-field").each(function () {

            var $select = jQuery(this);

            // Remove redundant "please select" option
            $select.bind("reset", function () {
                $select.find("option[value='']").remove();
            }).trigger("reset");

            new AJS.SingleSelect({
                element: this,
                revertOnInvalid: true
            });

        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createissueTypePicker(context);
        }
    });

})();

