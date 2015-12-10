;(function() {
    var ProjectEditKey = require('jira/project/project-edit-key');
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var Reasons = require('jira/util/events/reasons');
    var $ = require('jquery');

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== Reasons.panelRefreshed) {
            context.find("#edit-project-fields").each(function (index, form) {
                var $form = $(form);
                var projectEditKey = new ProjectEditKey($form);
                var $link = $form.find("#edit-project-key-toggle");
                $link.bind("click", function () {projectEditKey.toggle()});
            });
        }
    });
})();
