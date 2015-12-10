;(function() {

    var CreateProjectField = require('jira/field/create-project-field');
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var Reasons = require('jira/util/events/reasons');
    var $ = require('jquery');

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== Reasons.panelRefreshed) {
            context.find("#add-project-fields").each(function () {
                new CreateProjectField({
                    element: $(this)
                });
            });
        }
    });
})();
