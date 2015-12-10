define('jira/dialog/screenshot-window', [
    'jira/dialog/dialog',
    'jira/ajs/layer/inline-layer',
    'jquery'
], function(
    Dialog,
    InlineLayer,
    jQuery
) {
    /**
     * It's not a dialog so much as it is a new popup window.
     *
     * @class ScreenshotDialog
     * @param {Object} options
     * @constructor
     */
    return function(options) {
        this.$trigger = jQuery(options.trigger);
        jQuery(document).delegate(options.trigger, "click", function(e) {
            e.preventDefault();

            // hide any dialogs & dropdowns
            if (Dialog.current) {
                Dialog.current.hide();
            }
            if (InlineLayer.current) {
                InlineLayer.current.hide();
            }

            window.open(jQuery(this).attr("href") + "&decorator=popup", "screenshot", "width=800,height=700,scrollbars=yes,status=yes");
        });
    };
});

/** Preserve legacy namespace
    @deprecated jira.app.attachments.screenshot.ScreenshotWindow */
AJS.namespace("jira.app.attachments.screenshot.ScreenshotWindow", null, require('jira/dialog/screenshot-window'));
AJS.namespace('JIRA.ScreenshotDialog', null, require('jira/dialog/screenshot-window'));
