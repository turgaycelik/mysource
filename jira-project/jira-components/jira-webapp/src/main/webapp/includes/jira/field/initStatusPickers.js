(function () {

    function createCategoryPicker(context) {
        context.find("select#statusCategory").each(function (i, el) {
            new JIRA.StatusCategorySingleSelect({
                element: el,
                revertOnInvalid: true
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            createCategoryPicker(context);
        }
    });

})();
