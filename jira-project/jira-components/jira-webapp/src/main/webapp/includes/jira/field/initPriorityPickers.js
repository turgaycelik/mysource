(function ($) {

    function createPriorityPicker(context) {
        context.find("select#priority").each(function (i, el) {
            new AJS.SingleSelect({
                element: el,
                revertOnInvalid: true
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createPriorityPicker(context);
        }
    });

})(AJS.$);