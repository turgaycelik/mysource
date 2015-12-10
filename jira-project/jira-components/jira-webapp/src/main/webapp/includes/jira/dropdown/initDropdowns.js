;(function() {
    var DropdownFactory = require('jira/dropdown/dropdown-factory');
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var $ = require('jquery');

    $(function () {
        DropdownFactory.bindNavigatorOptionsDds();
        DropdownFactory.bindConfigDashboardDds();
    });

    Events.bind("Issue.subtasksRefreshed", function (e, ctx) {
        DropdownFactory.bindIssueActionsDds(ctx);
    });

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, ctx) {
        DropdownFactory.bindIssueActionsDds(ctx);
        DropdownFactory.bindGenericDropdowns(ctx);
    });

})();
