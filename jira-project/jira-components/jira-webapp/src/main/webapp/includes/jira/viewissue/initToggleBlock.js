AJS.$(function() {
    new JIRA.ToggleBlock({
        blockSelector: ".toggle-wrap",
        triggerSelector: ".mod-header .toggle-title",
        storageCollectionName: "block-states",
    });

    // When we refresh the issue page we also need make sure we restore twixi block state
    if (JIRA.Events.ISSUE_REFRESHED) {
        JIRA.bind(JIRA.Events.ISSUE_REFRESHED, function () {
            if (JIRA.Events.REFRESH_TOGGLE_BLOCKS) {
                JIRA.trigger(JIRA.Events.REFRESH_TOGGLE_BLOCKS);
            }
        });
    }
});