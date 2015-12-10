(function () {

    function findProjectAndIssueTypeSelectAndConvertToPicker(ctx) {

        var $ctx = ctx || jQuery("body"),
            $project = $ctx.find(".project-field, .project-field-readonly"),
            $issueTypeSelect = $ctx.find(".issuetype-field"),
            $projectIssueTypes = $ctx.find(".project-issue-types"),
            $defaultProjectIssueTypes = $ctx.find(".default-project-issue-type");

        $project.each(function (i) {
            new JIRA.ProjectIssueTypeSelect({
                project: jQuery(this),
                issueTypeSelect: $issueTypeSelect.eq(i),
                projectIssueTypesSchemes: $projectIssueTypes.eq(i),
                issueTypeSchemeIssueDefaults: $defaultProjectIssueTypes.eq(i)
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            findProjectAndIssueTypeSelectAndConvertToPicker(context);
        }
    });

})();