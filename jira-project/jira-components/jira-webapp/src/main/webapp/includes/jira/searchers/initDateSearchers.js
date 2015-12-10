jQuery(function () {

    function initDueDateSearcher($ctx) {
        $ctx.find(".js-duedate-searcher").each(function () {
            JIRA.DateSearcher.createDueDateSearcher(this);
        });
    }

    function initResolvedDateSearcher($ctx) {
        $ctx.find(".js-resolutiondate-searcher").each(function () {
            JIRA.DateSearcher.createResolvedDateSearcher(this);
        });
    }

    function initCreatedDateSearcher($ctx) {
        $ctx.find(".js-created-searcher").each(function () {
            JIRA.DateSearcher.createCreatedDateSearcher(this);
        });
    }

    function initUpdatedDateSearcher($ctx) {
        $ctx.find(".js-updated-searcher").each(function () {
            JIRA.DateSearcher.createUpdatedDateSearcher(this);
        });
    }

    function initCustomFieldDateSearcher($ctx) {
        $ctx.find(".js-customdate-searcher").each(function () {
            JIRA.DateSearcher.createCustomDateSearcher(this);
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason === JIRA.CONTENT_ADDED_REASON.criteriaPanelRefreshed) {
            initDueDateSearcher(context);
            initResolvedDateSearcher(context);
            initCreatedDateSearcher(context);
            initUpdatedDateSearcher(context);
            initCustomFieldDateSearcher(context);
        }
    });
});


