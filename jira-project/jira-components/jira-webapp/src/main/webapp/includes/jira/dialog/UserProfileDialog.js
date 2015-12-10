define('jira/dialog/user-profile-dialog', [
    'jira/dialog/form-dialog',
    'jquery'
], function(
    FormDialog,
    jQuery
) {
    /**
     * @class UserProfileDialog
     * @extends FormDialog
     */
    return FormDialog.extend({
        _getDefaultOptions: function () {
            return jQuery.extend(this._super(), {
                notifier: "#userdetails-notify"
            });
        },
        _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
            if (this.serverIsDone) {
                if (this.options.autoClose) {
                    this.hide();
                }
                this._reloadOrNotify();
            }
        },
        _reload: function () {
            return false;
        },
        show: function () {
            this._super();
            this._hideNotifier();
        },
        _reloadOrNotify: function () {
            if (this._reload()) {
                window.location.reload();
            } else {
                this._showNotifier();
            }
        },
        _showNotifier: function () {
            jQuery(this.options.notifier).removeClass("hidden");
        },
        _hideNotifier: function () {
            jQuery(this.options.notifier).addClass("hidden");
        }
    });
});

/** Preserve legacy namespace
    @deprecated AJS.FormPopup */
AJS.namespace("AJS.UserProfilePopup", null, require('jira/dialog/user-profile-dialog'));
AJS.namespace('JIRA.UserProfileDialog', null, require('jira/dialog/user-profile-dialog'));
