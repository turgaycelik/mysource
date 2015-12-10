AJS.test.require("jira.webresources:dialogs", function () {

require([
    'jira/dialog/error-dialog',
    'jira/util/browser',
    'jquery',
    'underscore'
], function(
    ErrorDialog,
    Browser,
    $,
    _
) {

    var ErrorDialogDriver = function () {};

    _.extend(ErrorDialogDriver.prototype, {
        isVisible: function () {
            return this.el().is(":visible");
        },
        message: function() {
            return $.trim(this.el().find(".error").text());
        },
        refresh: function () {
            var $refresh = this.el().find(".error-dialog-refresh");
            if (!$refresh.length) {
                throw "Could not find refresh button.";
            } else {
                $refresh.click();
            }
        },
        el: function () {
            return $("#error-dialog");
        }
    });

    module("JIRA.ErrorDialog", {
        setup: function () {
            this.driver = new ErrorDialogDriver();
            this.sandbox = sinon.sandbox.create();
        },
        teardown: function () {
            this.sandbox.restore();
        }
    });

    test("Dialog displays correct error message", function () {
        var message = "Error";
        var dialog = new ErrorDialog({
            message : message
        });

        dialog.show();
        equal(this.driver.message(), message, "Dialog displaying correct error message.");
        dialog.hide();
    });

    test("Dialog hide/show.", function () {
        var message = "Error";
        var dialog = new ErrorDialog({
            message : message
        });

        ok(!this.driver.isVisible(), "Dialog should be hidden by default.");
        dialog.show();
        ok(this.driver.isVisible(), "Dialog should now be visible.");
        dialog.hide();
        ok(!this.driver.isVisible(), "Dialog should be hidden again.");
    });

    test("openErrorDialogForXHR displays error dialog.", function () {
        var dialog = ErrorDialog.openErrorDialogForXHR({
            status: 401,
            responseText: JSON.stringify({errorMessages: ["abc"]})
        });

        dialog.show();
        equal(this.driver.message(), "abc", "Dialog displaying correct error message.");
        dialog.hide();
    });

    test("refresh does page reload.", function () {
        var reloader = this.sandbox.stub(Browser, "reloadViaWindowLocation");

        var dialog = ErrorDialog.openErrorDialogForXHR({
            status: 401,
            responseText: JSON.stringify({errorMessages: ["abc"]})
        }).show();

        this.driver.refresh();
        ok(reloader.calledOnce, "Refresh does a page pop.");
        dialog.hide();
    });

});
});