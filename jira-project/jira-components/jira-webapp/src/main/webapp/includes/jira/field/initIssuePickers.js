;(function() {
    var IssuePicker = require('jira/field/issue-picker');
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var Reasons = require('jira/util/events/reasons');
    var $ = require('jquery');

    function initIssuePicker(el) {
        $(el || document.body).find('.aui-field-issuepicker').each(function () {
            new IssuePicker({
                element: $(this),
                userEnteredOptionsMsg: AJS.I18n.getText('linkissue.enter.issue.key'),
                uppercaseUserEnteredOnSelect: true
            });
        });
    }

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== Reasons.panelRefreshed) {
            initIssuePicker(context);
        }
    });
})();
