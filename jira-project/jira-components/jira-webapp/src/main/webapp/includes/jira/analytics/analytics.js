/**
 * Capture analytics events in the JIRA general context.
 */
AJS.toInit(function($) {
    /**
     * Returns the currently selected tab on the Browse Project page
     */
    function getBrowseProjectTab() {
        return $("li.active a.browse-tab").attr("id");
    }

    // Need to defer for debugging support (see analytics-debug.js).
    _.defer(function() {
        if (AJS.EventQueue) {
            // Capture clicks on 'Administer Project' button on Browse Project page
            $(document).delegate("#project-admin-link", "click", function() {
                var selectedTab = getBrowseProjectTab();
                AJS.EventQueue.push({
                    name: "browseproject.administerproject",
                    properties: {
                        selectedtab: selectedTab
                    }
                });
            });

            // Capture clicks on 'Create New Project' button on Browse Projects page
            $(document).delegate("#browse-projects-create-project", "click", function() {
                AJS.EventQueue.push({
                    name: "browseprojects.createproject",
                    properties: {}
                });
            });

            // Capture clicks on the 'create an issue' link on the Issues tab when no issues exist in the project
            $(document).delegate("#no-issues-create-issue", "click", function() {
                AJS.EventQueue.push({
                    name: "browseproject.issuesblankslate.createissue",
                    properties: {}
                });
            });

            // Capture clicks on the issue filter links on the Issues tab
            $(document).delegate("a.issue-filter-link", "click", function() {
                var $el = $(this);
                var id = $el.attr("id").replace("filter_", "");
                var type = $el.attr("data-type");
                AJS.EventQueue.push({
                    name: "browse" + type + ".issuefilter." + id,
                    properties: {}
                });
            });

            $(document).on("click", "#project_import_link_lnk", function() {
                AJS.EventQueue.push({
                    name: "topnav.jim",
                    properties: {}
                });
            });

            $(document).on("click", ".issueaction-viewworkflow", function() {
                var isNew = AJS.$(this).attr("class").indexOf("new-workflow-designer") > -1;
                var version = isNew ? "new" : "old";

                var newEnabled = AJS.DarkFeatures.isEnabled("casper.VIEW_ISSUE");

                AJS.EventQueue.push({
                    name: "issue.viewworkflow",
                    properties: {
                        version: version,
                        newEnabled: newEnabled
                    }
                });
            });

            $.fn.ready(function captureWindowSize() {
                var width, height, shouldCapture = false;
                shouldCapture = shouldCapture || $(document.body).hasClass("page-type-navigator"); // View Issue, Issue Nav

                if (!shouldCapture) return;

                width = $(window).width();
                height = $(window).height();
                AJS.EventQueue.push({
                    name: "browserWindowSize",
                    properties: {
                        width: width,
                        height: height
                    }
                });
            });

        } // if (AJS.EventQueue)
    });
});
