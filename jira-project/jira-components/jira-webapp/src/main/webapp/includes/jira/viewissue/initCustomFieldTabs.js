(function ($, scope) {
    var activateTab = function(tabName) {
        var tabTrigger;
        if (typeof tabName !== "string" && tabName.length > 0) return;
        if (tabName[0] !== "#") tabName = "#" + tabName; // Ensure there's a hash. It'll work for both jQuery + href selectors

        console.log("activating tab", tabName);
        tabTrigger = $("a[href='" + tabName +"']", scope);
        if (tabTrigger.length) {
            AJS.tabs.change(tabTrigger);
        }
    };

    var revealer = function(e) {
        var $containingTab = $(e.target).closest(".tabs-pane");
        if ($containingTab.length > 0) {
            activateTab($containingTab.attr("id"));
        }
    };

    $(function () {
        if (JIRA.Events.NEW_CONTENT_ADDED) {
            JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $context) {
                // Bind the revealer to new content.
                var $customFieldModule = $context.find(scope);
                $customFieldModule.unbind("reveal");
                $customFieldModule.bind("reveal", revealer);

                // Set up and tabs in the new content.
                AJS.tabs.setup();
            });
        }

        // Preserve the active custom field tab when refreshing the details panel.
        if (JIRA.Events.PANEL_REFRESHED) {
            JIRA.bind(JIRA.Events.PANEL_REFRESHED, function (e, panel, $new, $existing) {
                if (panel === "details-module") {
                    var $activeTab = $existing.find(scope).find(".active-tab");
                    if ($activeTab.length === 1) {
                        activateTab($activeTab.find("a").attr("href"));
                    }
                }
            });
        }

        $(scope).bind("reveal", revealer);
    });
})(AJS.$, "#customfieldmodule");