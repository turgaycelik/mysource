(function($) {
    var defaultOpts = {
        type: "image",
        centerOnScroll: true,

        // disable JIRA's keyboard shortcuts when displaying a lightbox
        onStart : AJS.disableKeyboardShortcuts,
        onClosed : AJS.enableKeyboardShortcuts
    };

    function initGallery($ctx) {
        var transition = 'elastic';
        var galleryOpts = $.extend({}, defaultOpts, {
            transitionIn: transition,
            transitionOut: transition
        });

        $ctx.find(".gallery, [id$='_thumb']").each(function() {
            $(this).fancybox(galleryOpts);
        });
        $ctx.find("#attachment_thumbnails .attachment-title").click(function(e) {
            if (e.which === 1) {
                e.preventDefault();
                $(this).closest(".attachment-content").find(".gallery").click();
            }
        });
    }

    function initWorkflow($ctx) {
        $ctx.find("a.issueaction-viewworkflow:not(.new-workflow-designer)").each(function() {
            $(this).fancybox(defaultOpts);
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $ctx, reason) {
        initGallery($ctx);
        initWorkflow($ctx);
    });

    $(function () {
        if (JIRA.Events.PANEL_REFRESHED) {
            JIRA.bind(JIRA.Events.PANEL_REFRESHED, function (e, panel, $new, $existing) {
                if (panel === "attachmentmodule") {
                    initGallery($new);
                } else if (panel === "details-module") {
                    initWorkflow($new);
                }
            });
        }
    });
})(AJS.$);
