define('jira/dropdown/contain-dropdown', [
    'jquery',
    'exports'
], function(
    jQuery,
    exports
) {
    function containDropdown(dropdown, containerSelector, dynamic) {

        function getDropdownOffset() {
            return dropdown.$.offset().top - jQuery(containerSelector).offset().top;
        }

        var container,
            ddOffset,
            availableArea,
            shadowOffset = 25;

        if (dropdown.$.parents(containerSelector).length !== -1) {

            container = jQuery(containerSelector),
            ddOffset = getDropdownOffset(),
            shadowOffset = 30,
            availableArea = container.outerHeight() - ddOffset - shadowOffset;

            if (availableArea <= parseInt(dropdown.$.prop("scrollHeight"), 10)) {
                containHeight(dropdown, availableArea);
            } else if (dynamic) {
                releaseContainment(dropdown);
            }
            dropdown.reset();
        }
    }

    function containHeight(dropdown, availableArea) {
        dropdown.$.css({
            height: availableArea
        });
        if (dropdown.$.css("overflowY") !== "scroll") {
            dropdown.$.css({
                width: 15 + dropdown.$.prop("scrollWidth"),
                overflowY: "scroll",
                overflowX: "hidden"
            });
        }
    }

    function releaseContainment(dropdown) {
        dropdown.$.css({
            height: "",
            width: "",
            overflowY: "",
            overflowX: ""
        });
    }

    exports = containDropdown;
    exports.containHeight = containHeight;
    exports.releaseContainment = releaseContainment;
});


/** Preserve legacy namespace
    @deprecated AJS.containDropdown */
AJS.namespace("AJS.containDropdown", null, require('jira/dropdown/contain-dropdown'));
AJS.namespace('JIRA.containDropdown', null, require('jira/dropdown/contain-dropdown'));
