define('jira/dialog/edit-preferences-dialog', [
    'jira/dialog/user-profile-dialog',
    'jquery'
], function(
    UserProfileDialog,
    jQuery
) {
    /**
     * @class EditPreferencesDialog
     * @extends UserProfileDialog
     */
    return UserProfileDialog.extend({
        _getDefaultOptions: function () {
            return jQuery.extend(this._super(), {
                notifier: "#userprofile-notify"
            });
        },
        _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
            if (this.serverIsDone)
            {
                this._updatePageSize();
                this._updateEmail();
                this._updateSharing();
                this._updateOwnNotifications();
                // We update this last, because if there any updates we need to reload the page so that they are picked up
                // immediately. We override the magical "reload" function to true so that the super-class knows it needs to reload
                // the page. Damn it! I hate inheritance hierarchies.
                this._updateLocale();
                this._updateTimezone();
                this._updateKeyboardShortcutsNotifications();
                this._updateAutowatch();
                this._super(data, xhr, smartAjaxResult);
            }
        },
        _updatePageSize: function() {
            var pageSize = jQuery("#update-user-preferences-pagesize").val();
            jQuery("#up-p-pagesize").text(pageSize);
        },
        _updateEmail: function() {
            var email = jQuery("option:selected", "#update-user-preferences-mailtype").text();
            jQuery("#up-p-mimetype").text(email);
        },
        _updateSharing: function() {
            var sharing = jQuery("option:selected", "#update-user-preferences-sharing").val();
            if (sharing !== "false"){
                jQuery("#up-p-share-private").show();
                jQuery("#up-p-share-public").hide();
            } else {
                jQuery("#up-p-share-private").hide();
                jQuery("#up-p-share-public").show();
            }
        },
        _updateOwnNotifications: function() {
            var ownNotifications = jQuery("option:selected", "#update-user-preferences-own-notifications").val();
            if (ownNotifications !== "false"){
                jQuery("#up-p-notifications_on").show();
                jQuery("#up-p-notifications_off").hide();
            } else {
                jQuery("#up-p-notifications_on").hide();
                jQuery("#up-p-notifications_off").show();
            }
        },
        _updateLocale: function ()
        {
            var localeNewValue = jQuery.trim(jQuery("option:selected", "#update-user-preferences-locale").text());
            var localeOldValue = jQuery.trim(jQuery("#up-p-locale").text());

            if (localeOldValue !== localeNewValue) {
                this._reload = function() {
                    return true;
                }
            }
        },
        _updateTimezone : function() {
            var current = jQuery("option:selected","#defaultUserTimeZone");
            var timeZoneNewValue = jQuery.trim(current.text());

            var timeZoneRegion = current.val();

            if (timeZoneRegion != 'JIRA') {
               jQuery("#up-p-jira-default").hide();
            } else {
               jQuery("#up-p-jira-default").show();
            }
            jQuery("#up-p-timezone-label").text(timeZoneNewValue);
        },
        _updateKeyboardShortcutsNotifications: function() {
            var kbShortcutsNewValue = jQuery("option:selected", "#update-user-preferences-keyboard-shortcuts").val();
            var kbShortcutsOldValue = jQuery("#up-p-keyboard-shortcuts-enabled").is(":visible") ? "true" : "false";

            if (kbShortcutsOldValue !== kbShortcutsNewValue){
                if (kbShortcutsNewValue !== "false") {
                    jQuery("#up-p-keyboard-shortcuts-enabled").show();
                    jQuery("#up-p-keyboard-shortcuts-disabled").hide();
                }
                else {
                    jQuery("#up-p-keyboard-shortcuts-enabled").hide();
                    jQuery("#up-p-keyboard-shortcuts-disabled").show();
                }
                this._reload = function() {
                    return true;
                }
            }
        },
        _updateAutowatch : function() {
            var autowatchValue = jQuery("option:selected", "#update-user-preferences-autowatch").val();
            if (autowatchValue !== 'false') {
                jQuery("#up-p-autowatch-enabled").show();
                jQuery("#up-p-autowatch-disabled").hide();
            } else {
                jQuery("#up-p-autowatch-enabled").hide();
                jQuery("#up-p-autowatch-disabled").show();
            }
        }
    });
});

AJS.namespace('JIRA.EditPreferencesDialog', null, require('jira/dialog/edit-preferences-dialog'));
