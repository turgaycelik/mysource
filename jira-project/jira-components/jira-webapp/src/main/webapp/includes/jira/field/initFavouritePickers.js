JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function(e, $ctx, reason) {
    if (reason == JIRA.CONTENT_ADDED_REASON.pageLoad) {
        JIRA.FavouritePicker.init($ctx);
    }
});
