define('jira/togglelist/toggle-list', [
    'jira/lib/class',
    'jquery'
], function (
    Class,
    jQuery
) {

    /**
     * A generic control for toggling the visibility of some list elements.
     *
     * <h4>Use </h4>
     *
     * <h5>Markup:</h5>
     *
     * <pre>
     * <ul id="my-list">
     *     <li>One</li>
     *     <li>Two</li>
     *     <li>Three</li>
     *     <li>Four</li>
     *     <li>Five</li>
     *     <li>Six</li>
     * </ul>
     * <a id="show-more-of-my-list" href="#">Show more</a>
     * <a id="show-less-of-my-list" href="#">Show less</a>
     * </pre>
     *
     * <h5>JavaScript:</h5>
     *
     * <pre>
     * new ToggleList({
     *     more: $("#my-list li:gt(4)"),
     *     showMoreLink: $("#show-more-of-my-list"),
     *     showLessLink: $("#show-less-of-my-list")
     * });
     * </pre>
     *
     * @class ToggleList
     * @extends Class
     */
    return Class.extend({

        /*
         * @param {Object} options:
         * - {jQuery} more - the elements in the list to be toggled
         * - {jQuery} showMoreLink - the link to toggle showing the "more" elements
         * - {jQuery} showLessLink - the link to toggle hiding the "more" elements
         * - {jQuery} showMoreContainer (optional) - the container of the showMoreLink
         * - {jQuery} showLessContainer (optional) - the container of the showLessLink
         */
        init: function(options) {
            var more = options.more,
                showMoreContainer = options.showMoreContainer || options.showMoreLink,
                showLessContainer = options.showLessContainer || options.showLessLink,
                showMoreLink = options.showMoreLink,
                showLessLink = options.showLessLink;
            
            if (!more || !more.length) {
                // Nothing to toggle, make sure toggle controls are hidden
                showMoreContainer.hide();
                showLessContainer.hide();
                return;
            }

            // Default visibility state
            more.hide();
            showMoreContainer.show();
            showLessContainer.hide();

            var toggle = function(e) {
                e.preventDefault();
                more.toggle();
                showMoreContainer.toggle();
                showLessContainer.toggle();
            };

            showMoreLink.click(toggle);
            showLessLink.click(toggle);
        }
    });
});

AJS.namespace('JIRA.ToggleList', null, require('jira/togglelist/toggle-list'));
