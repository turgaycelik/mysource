(function($) {

    function createShareUserPicker(ctx) {
        $(".share-user-picker", ctx).each(function () {
            var control = new JIRA.MultiUserListPicker({
                layerId: $(this).attr('id') + '-layer',
                element: $(this),
                freeEmailInput: true
            });
            $(document).trigger('ready.multi-select.share-user', control);
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createShareUserPicker(context);
        }
    });

}(AJS.$));