define('jira/userhover/userhover', [
    'jira/ajs/dropdown/dropdown-factory',
    'aui/inline-dialog',
    'jquery'
], function(
    DropdownFactory,
    InlineDialog,
    jQuery
) {
    /**
     * Inline dialogs for user infomation:
     *
     * @addon JIRA.userhover
     *   -- To reduce unneeded HTTP requests (JRADEV-2207) we need to circumvent AJS.InlineDialog's default
     *      behaviour. Thus, we use the noBind = true option, and manually control when to show/hide the
     *      dialog popups.
     */
    var userhover = function() {};

    userhover.INLINE_DIALOG_OPTIONS = {
        urlPrefix: contextPath + "/secure/ViewUserHover!default.jspa?decorator=none&username=",
        showDelay: 400,
        closeOthers: false,
        noBind: true,
        hideCallback: function () {
            this.popup.remove(); // clean up so we don't have thousands of popups around in the dom
        }
    };

    userhover.show = function(trigger) {
        clearTimeout(jQuery.data(trigger, "AJS.InlineDialog.delayId") || 0);
        jQuery.data(trigger, "AJS.InlineDialog.hasUserAttention", true);
        if (jQuery.data(trigger, "AJS.InlineDialog") || userhover._locked) {
            // This or another user hover dialog is already visible.
            return;
        }
        jQuery.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
            // Don't show the dialog if the trigger has been detached or removed.
            if (jQuery(trigger).closest("html").length === 0) {
                userhover.hide(trigger);
                return;
            }

            jQuery.data(trigger, "AJS.InlineDialog", InlineDialog(
                jQuery(trigger),
                "user-hover-dialog-" + new Date().getTime(),
                function($contents, _, showPopup) {
                    // Call the InlineDialog's url function with its expected arguments.
                    userhover._fetchDialogContents($contents, trigger, showPopup);
                },
                userhover.INLINE_DIALOG_OPTIONS
            )).show();
        }, userhover.INLINE_DIALOG_OPTIONS.showDelay));
    };

    userhover.hide = function(trigger, showDelay) {
        clearTimeout(jQuery.data(trigger, "AJS.InlineDialog.delayId") || 0);
        jQuery.data(trigger, "AJS.InlineDialog.hasUserAttention", false);
        var dialog = jQuery.data(trigger, "AJS.InlineDialog");
        if (dialog && !userhover._locked) {
            if (typeof showDelay !== "number") {
                showDelay = userhover.INLINE_DIALOG_OPTIONS.showDelay;
            }
            if (showDelay >= 0) {
                // Hide the dialog after the given delay period.
                jQuery.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
                    dialog.hide();
                    jQuery.data(trigger, "AJS.InlineDialog", null);
                }, showDelay));
            } else {
                // Hide the dialog immediately.
                dialog.hide();
                jQuery.data(trigger, "AJS.InlineDialog", null);
            }
        }
    };

    userhover._locked = false;

    userhover._fetchDialogContents = function($contents, trigger, showPopup) {
        jQuery.get(userhover.INLINE_DIALOG_OPTIONS.urlPrefix + encodeURIComponent(trigger.getAttribute("rel")), function(html) {
            if (jQuery.data(trigger, "AJS.InlineDialog.hasUserAttention")) {
                $contents.html(html);
                $contents.css("overflow", "visible");
                jQuery(DropdownFactory.createDropdown({
                    trigger: $contents.find(".aui-dd-link"),
                    content: $contents.find(".aui-list")
                })).bind({
                    "showLayer": function() {
                        userhover._locked = true;
                    },
                    "hideLayer": function() {
                        userhover._locked = false;
                        if (!jQuery.data(trigger, "AJS.InlineDialog.hasUserAttention")) {
                            userhover.hide(trigger);
                        }
                    }
                });
                showPopup();
                // Wait for the popup's show animation to complete before binding event handlers
                // on $contents. This ensures the popup doesn't get in the way when the mouse
                // moves over it quickly.
                jQuery.data(trigger, "AJS.InlineDialog.delayId", setTimeout(function() {
                    $contents.bind({
                        "mousemove": function() {
                            userhover.show(trigger);
                        },
                        "mouseleave": function() {
                            userhover.hide(trigger);
                        }
                    });
                }, userhover.INLINE_DIALOG_OPTIONS.showDelay));
            }
        });
    };

    return userhover;
});

/** Preserve legacy namespace
    @deprecated jira.app.userhover */
AJS.namespace("jira.app.userhover", null, require('jira/userhover/userhover'));
AJS.namespace('JIRA.userhover', null, require('jira/userhover/userhover'));
