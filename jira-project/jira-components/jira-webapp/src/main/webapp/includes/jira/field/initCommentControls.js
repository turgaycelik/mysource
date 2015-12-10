(function ($) {

    function applyCommentControls($contexts) {
        if ($contexts.attr("id") === "addcomment") {
            $contexts = $contexts.find("#issue-comment-add");
        }
        //we have to do this since there may be multiple elements with id=issue-comment-add...
        $contexts.each(function() {
            var $context = $(this);
            var $commentLevel = $context.find("#commentLevel");
            if ($commentLevel.length > 0) {
                new AJS.SecurityLevelSelect($commentLevel);
            }
            $context.find(".wiki-js-prefs").each(function() {
                JIRA.wikiPreview(this, $context).init();
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
        applyCommentControls(context);
    });

})(AJS.$);
