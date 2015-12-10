define('jira/dialog/error-dialog', [
    'jira/dialog/dialog',
    'jira/ajs/ajax/ajax-util',
    'jira/lib/class',
    'jira/util/browser',
    'jquery'
], function(
    Dialog,
    AjaxUtil,
    Class,
    Browser,
    $
) {
    /**
     * Displays a dialog with an error message.
     *
     * @class ErrorDialog
     * @extends Class
     */
    var ErrorDialog = Class.extend(
    /** @lends ErrorDialog# */
    {
        /**
         * @constructs
         * @param {string} [options.message] The message to display in the dialog.
         */
        init: function(options) {

            var message = options.message || AJS.I18n.getText("common.forms.ajax.servererror");

            this.dialog = new Dialog({
                id: "error-dialog",
                content: function (ready) {
                    var $el = $(JIRA.Templates.ErrorDialog.serverErrorModalDialog({
                        message: message
                    }));

                    $el.find(".error-dialog-refresh").click(function (e) {
                        e.preventDefault();
                        Browser.reloadViaWindowLocation();
                    });

                    ready($el);
                }
            });

            this.dialog.addHeading(AJS.I18n.getText("common.words.error"));
            this.dialog.handleCancel = $.noop;
        },
        /**
         * Hide the dialog.
         *
         * @returns {ErrorDialog} this object for chaining.
         */
        hide: function () {
            this.dialog.hide();
            return this;
        },

        /**
         * Show the dialog.
         *
         * @returns {ErrorDialog} this object for chaining.
         */
        show: function () {
            this.dialog.show();
            return this;
        }
    });

    /**
     * Displays a dialog with an error message describing the error state of the passed XHR.
     *
     * The dialog will have been displayed to the user.
     *
     * @param {XMLHttpRequest} xhr the XHR to query
     * @returns {JIRA.ErrorDialog} a visible dialog displaying the error.
     */
    ErrorDialog.openErrorDialogForXHR = function (xhr) {
        return new ErrorDialog({
            message: AjaxUtil.getErrorMessageFromXHR(xhr)
        }).show();
    };

    return ErrorDialog;
});

AJS.namespace('JIRA.ErrorDialog', null, require('jira/dialog/error-dialog'));
