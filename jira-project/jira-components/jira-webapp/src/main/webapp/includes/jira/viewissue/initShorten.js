;(function() {
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var jQuery = require('jquery');
    require('jira/jquery/plugins/shorten/shorten');

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, $ctx) {
        jQuery(".shorten", $ctx).shorten();
    });
})();
