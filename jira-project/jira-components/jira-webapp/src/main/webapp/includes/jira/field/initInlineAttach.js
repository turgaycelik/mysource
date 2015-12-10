;(function() {
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var Reasons = require('jira/util/events/reasons');
    require('jira/jquery/plugins/attachment/inline-attach'); // Ensure jQuery plugin is defined)

    /**
     * @param {jQuery} context
     */
    function createInlineAttach(context) {
        context.find("input[type=file]:not('.ignore-inline-attach')").inlineAttach();
    }

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== Reasons.panelRefreshed) {
            createInlineAttach(context);
        }
    });
})();
