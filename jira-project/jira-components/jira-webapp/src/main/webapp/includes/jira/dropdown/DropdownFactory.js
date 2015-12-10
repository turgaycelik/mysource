define('jira/dropdown/dropdown-factory', [
    'jira/ajs/dropdown/dropdown',
    'jquery',
    'exports'
], function (
    Dropdown,
    $,
    exports
) {
    exports.bindGenericDropdowns = function bindGenericDropdowns(ctx) {

        $(".js-default-dropdown", ctx).each(function () {

            var $trigger = $(this),
                $content = $trigger.next(".aui-list"),
                alignment = $trigger.attr("data-alignment") || AJS.RIGHT,
                hasDropdown = !!$trigger.data("hasDropdown");

            if ($content.length == 0) {
                console.warn("Dropdown init failied. Could not find content. Printing culprit...");
                console.log($trigger);
            }

            if (!hasDropdown) {
                $trigger.data("hasDropdown", true);
                new Dropdown({
                    trigger: $trigger,
                    content: $content,
                    alignment: alignment,
                    setMaxHeightToWindow: $trigger.attr("data-contain-to-window"),
                    hideOnScroll: $trigger.attr("data-hide-on-scroll") || ".issue-container"
                });
            }
        });
    };

    /**
     * Binds issue action (cog) dropdowns
     * @param ctx
     */
    exports.bindIssueActionsDds = function (ctx) {
        var trigger = $(".issue-actions-trigger", ctx);
        trigger.each(function () {
            var $trigger = $(this);
            new Dropdown({
                hideOnScroll: ".issue-container",
                trigger: $trigger,
                ajaxOptions: {
                    formatError: function () {
                        JIRA.Issues.Api.showInlineIssueLoadError();
                    },
                    dataType: "json",
                    cache: false,
                    formatSuccess: JIRA.FRAGMENTS.issueActionsFragment
                },
                onerror: function (instance) {
                    //Sometimes the layerController is left in a initializing state (race condition?)
                    //Reset it here just in case.
                    instance.layerController.initialized = true;
                    instance.hide();
                }
            });
            $trigger.addClass("trigger-happy")
        });
    };

    /**
     * Binds dropdowns that control the views & columns in issue navigator
     */
    exports.bindNavigatorOptionsDds = function () {
        var $navigatorOptions = $("#navigator-options");

        Dropdown.create({
            trigger: $navigatorOptions.find(".aui-dd-link"),
            content: $navigatorOptions.find(".aui-list"),
            alignment: AJS.RIGHT
        });
        $navigatorOptions.find("a.aui-dd-link").linkedMenu();
    };

    /**
     * Binds all the dropdowns that support the dashboard chrome
     */
    exports.bindConfigDashboardDds = function () {
        $("#dashboard").find(".aui-dd-parent").dropDown("Standard", {
            trigger: "a.aui-dd-link"
        });
    };
});

AJS.namespace('JIRA.Dropdowns', null, require('jira/dropdown/dropdown-factory'));
