AJS.test.require("com.atlassian.jira.dev.func-test-plugin:sinon");
AJS.test.require("jira.webresources:dialogs", function() {

    var $ = require('jquery');
    var FormDialog = require('jira/dialog/form-dialog');
    var Browser = require('jira/util/browser');
    var lastDialog = null;

    module("JIRA.SmartAjax.showWebSudoDialog", {
        teardown: function() {
            this.sandbox.restore();
        },
        setup: function() {
            var test = this;
            lastDialog = null;

            this.sandbox = sinon.sandbox.create();
            this.sandbox.stub(Browser, "reloadViaWindowLocation");

            this.sandbox.stub(FormDialog.prototype, "show");
            this.sandbox.stub(FormDialog.prototype, "hide");
            this.sandbox.stub(FormDialog.prototype, "_setContent");
            this.sandbox.stub(FormDialog.prototype, "init", function(options) {
                this.options = options || {};
                lastDialog = this;
            });
            FormDialog.prototype.triggerHide = function(reason) {
                $(this).trigger("Dialog.hide", [{
                    find: function() {
                        return {
                            attr: function() {
                                return test.href;
                            }
                        }
                    }
                }, reason]);
            };
        }
    });

    function assertWebSudoDialogSuccess(options) {

        var context = options.context || JIRA.SmartAjax;

        JIRA.SmartAjax.showWebSudoDialog(options);
        var dialog = lastDialog;

        if (options.beforeShow) {
            ok(options.beforeShow.calledOnce, "Before show called.");
            ok(options.beforeShow.firstCall.calledOn(context), "Show called with correct context.");
        }

        ok(dialog.show.calledOnce, "Dialog was shown.");
        if (options.beforeShow) {
            ok(options.beforeShow.calledBefore(dialog.show), "beforeShow called before show.");
        }

        if (options.show) {
            ok(options.show.calledOnce, "Show called.");
            ok(options.show.firstCall.calledOn(context), "Show called with correct context.");
            ok(options.show.calledAfter(dialog.show), "Show called after dialog shown.");
        }

        //This is the main thing we want to test.
        var handler = dialog.options.submitHandler;
        var event = $.Event();
        event.target = $("<form></form>").attr("action", "url").append("<input name='jack' value='jill'/>");

        var closeCallback = this.sandbox.spy();
        var makeRequest = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");

        //Call the handler and made some assertions.
        handler(event, closeCallback);
        ok(event.isDefaultPrevented(), "Default operation prevented.");
        ok(makeRequest.calledOnce, "Made the request for the websudo dialog.");

        //First call first argument is the AJAX options.
        var callArguments = makeRequest.firstCall.args[0];
        equal(callArguments.url, event.target.attr("action"), "Correct Action");
        equal(callArguments.data, event.target.serialize(), "Correct Data");
        equal(callArguments.type, "POST", "Correct type");

        //Complete handler.
        var completeHandler = callArguments.complete;
        var xhr = {
            getResponseHeader: this.sandbox.stub(),
            responseText: "callback"
        };

        //Lets do a failed request.
        completeHandler(xhr);
        ok(!closeCallback.called, "Close callback should not be called.");
        ok(!dialog.hide.called, "Dialog should not be closed.");
        if (options.success) {
            ok(!options.success.called, "Success function should not have been called.");
        }
        ok(dialog._setContent.calledWith(xhr.responseText, true), "Dialog content reset.");

        //Lets simulate success.
        xhr.getResponseHeader.withArgs("X-Atlassian-WebSudo").returns("Has-Authentication");

        completeHandler(xhr);

        if (options.success) {
            ok(options.success.calledOnce, "Success called.");
            ok(!closeCallback.called, "Close callback should not be called.");
            ok(!dialog.hide.called, "Dialog should not be closed.");

            options.success.firstCall.args[0]();
        }

        ok(closeCallback.calledOnce, "Close callback should be called.");
        ok(dialog.hide.calledOnce, "Dialog should be closed.");
    }

    test("not success callback", function() {
        assertWebSudoDialogSuccess.call(this, {});
    });

    test("beforeShow and show success callback", function() {
        assertWebSudoDialogSuccess.call(this, {
            beforeShow: this.sandbox.stub(),
            show: this.sandbox.stub()
        });
    });

    test("beforeShow and show success callback with context", function() {
        assertWebSudoDialogSuccess.call(this, {
            beforeShow: this.sandbox.stub(),
            show: this.sandbox.stub(),
            context: {}
        });
    });

    test("success callback with context", function() {
        assertWebSudoDialogSuccess.call(this, {
            success: this.sandbox.stub(),
            context: {}
        });
    });

    test("success callback without context", function() {
        assertWebSudoDialogSuccess.call(this, {
            success: this.sandbox.stub()
        });
    });

    test("all callbacks without context", function() {
        assertWebSudoDialogSuccess.call(this, {
            beforeShow: this.sandbox.stub(),
            show: this.sandbox.stub(),
            success: this.sandbox.stub()
        });
    });

    test("all callbacks with context", function() {
        assertWebSudoDialogSuccess.call(this, {
            beforeShow: this.sandbox.stub(),
            show: this.sandbox.stub(),
            success: this.sandbox.stub(),
            context: {}
        });
    });

    test("aborted no cancel", function() {
        JIRA.SmartAjax.showWebSudoDialog({});
        var dialog = lastDialog;
        ok(dialog.show.calledOnce, "Dialog was shown.");

        dialog.triggerHide(JIRA.Dialog.HIDE_REASON.cancel);
        dialog.triggerHide(JIRA.Dialog.HIDE_REASON.escape);
    });

    var assertWebSudoDialogCancelled =  function(reason) {
        Browser.reloadViaWindowLocation.reset();
        this.href = "i love web sudo";
        var cancel = this.sandbox.spy();
        var context = {};
        JIRA.SmartAjax.showWebSudoDialog({
            cancel: cancel,
            context: context
        });

        var dialog = lastDialog;
        ok(dialog.show.calledOnce, "Dialog was shown.");

        //Simulate dialog close.
        dialog.triggerHide(reason);

        //We only trigger on the first event.
        ok(cancel.calledOnce, "Cancel trigger should be called once.");
        ok(cancel.alwaysCalledOn(context), "Called on right context.");
        ok(Browser.reloadViaWindowLocation.calledWithExactly(this.href));
    };

    test("aborted with cancel", function() {
        assertWebSudoDialogCancelled.call(this, JIRA.Dialog.HIDE_REASON.cancel);
    });

    test("aborted with escape", function() {
        assertWebSudoDialogCancelled.call(this, JIRA.Dialog.HIDE_REASON.escape);
    });

    test("can prevent redirect by e.preventDefault()", function() {
        Browser.reloadViaWindowLocation.reset();
        JIRA.SmartAjax.showWebSudoDialog({
            cancel: function(e) {
                e.preventDefault();
            }
        });
        ok(!Browser.reloadViaWindowLocation.called);
    });

    test("can prevent redirect by returning false", function() {
        Browser.reloadViaWindowLocation.reset();
        JIRA.SmartAjax.showWebSudoDialog({
            cancel: function() {
                return false;
            }
        });
        ok(!Browser.reloadViaWindowLocation.called);
    });

    module("JIRA.SmartAjax.handleWebSudoError", {
        teardown: function() {
            this.sandbox.restore();
        },
        setup: function() {
            this.sandbox = sinon.sandbox.create();
            this.webSudo = require('jira/ajs/ajax/smart-ajax/web-sudo');
            this.dialog = this.sandbox.stub(this.webSudo, "showWebSudoDialog");
            this.makeRequest = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");
        }
    });

    test("success without delegating handler", function() {

        var options = {};
        var xhr = {};
        var status = "something";
        var result = {};

        this.webSudo.handleWebSudoError(options, undefined, xhr, status, result);
        ok(this.dialog.calledOnce, "Delgate to showWebSudoDialog");

        var dialogOptions = this.dialog.args[0][0];
        ok($.isFunction(dialogOptions.success), "Provided success.");
        ok($.isFunction(dialogOptions.cancel), "Provided cancel.");

        //Check the default success callback.
        var callback = this.sandbox.spy();
        dialogOptions.success.call(undefined, callback);

        ok(callback.calledOnce, "Make sure we close the dialog by default.");
        ok(this.makeRequest.calledOnce, "Make the request again, it might just work this time.");
        ok(this.makeRequest.calledWithExactly(options), "Make the request again, it might just work this time.");

    });

    test("success with delegating handler", function() {
        var success = this.sandbox.spy();
        var options = {};
        var xhr = {};
        var status = "something";
        var result = {};

        this.webSudo.handleWebSudoError(options, {success: success}, xhr, status, result);
        ok(this.dialog.calledOnce, "Delgate to showWebSudoDialog");

        var dialogOptions = this.dialog.args[0][0];
        ok($.isFunction(dialogOptions.success), "Provided success.");
        ok($.isFunction(dialogOptions.cancel), "Provided cancel.");

        //Check the default success callback.
        var callback = this.sandbox.spy();
        dialogOptions.success.call(undefined, callback);

        ok(!callback.calledOnce, "With a callback we don't close the dialog.");
        ok(this.makeRequest.calledOnce, "Make the request again, it might just work this time.");
        ok(this.makeRequest.calledWithExactly(options), "Make the request again, it might just work this time.");
        ok(success.calledOnce, "The success handler should have been called.");

        //Close the dialog.
        success.args[0][0]();
        ok(callback.calledOnce, "The callback has now closed the dialog.");
    });

    test("cancel wihtout handler", function() {
        var options = {};
        var xhr = {};
        var status = "something";
        var result = {};

        this.webSudo.handleWebSudoError(options, undefined, xhr, status, result);
        ok(this.dialog.calledOnce, "Delgate to showWebSudoDialog");

        var dialogOptions = this.dialog.args[0][0];
        ok($.isFunction(dialogOptions.success), "Provided success.");
        ok($.isFunction(dialogOptions.cancel), "Provided cancel.");

        //Check the default success callback.
        dialogOptions.cancel.call(undefined);
    });

    test("cancel with dialog Handler", function() {
        var cancel = this.sandbox.spy();
        var options = {};
        var xhr = {};
        var status = "something";
        var result = {};
        var context = {};

        this.webSudo.handleWebSudoError(options, {cancel: cancel}, xhr, status, result);
        ok(this.dialog.calledOnce, "Delgate to showWebSudoDialog");

        var dialogOptions = this.dialog.args[0][0];
        ok($.isFunction(dialogOptions.success), "Provided success.");
        ok($.isFunction(dialogOptions.cancel), "Provided cancel.");

        //Check the default success callback.
        dialogOptions.cancel.call(context);
        ok(cancel.calledOnce, "Cancel called on the dialog options.");
        ok(cancel.alwaysCalledOn(context), "Called with the right context.");
    });

    test("cancel with ajax handler Handler", function() {
        var cancel = this.sandbox.spy();
        var options = {
            complete: cancel
        };
        var xhr = {};
        var status = "something";
        var result = {};
        var context = {};

        this.webSudo.handleWebSudoError(options, {cancel: ""}, xhr, status, result);
        ok(this.dialog.calledOnce, "Delgate to showWebSudoDialog");

        var dialogOptions = this.dialog.args[0][0];
        ok($.isFunction(dialogOptions.success), "Provided success.");
        ok($.isFunction(dialogOptions.cancel), "Provided cancel.");

        //Check the default success callback.
        dialogOptions.cancel.call(context);
        ok(cancel.calledOnce, "Cancel called on the dialog options.");
        ok(cancel.alwaysCalledOn(context), "Called with the right context.");
        ok(cancel.alwaysCalledWithExactly(xhr, status, result), "Called with right arguments.");
    });

    module("JIRA.SmartAjax.makeWebSudoRequest", {
        teardown: function() {
            this.sandbox.restore();
        },
        setup: function() {
            this.sandbox = sinon.sandbox.create();
            this.makeRequest = this.sandbox.stub(JIRA.SmartAjax, "makeRequest");
            this.webSudo = require('jira/ajs/ajax/smart-ajax/web-sudo');
        }
    });

    test("makeWebSudoRequest with non 401 status code.", function() {
        var error = this.sandbox.spy();
        var promiseError = this.sandbox.spy();

        //make the request.
        var promise = this.webSudo.makeWebSudoRequest({
            error: error,
            copy: "me"
        });

        promise.fail(promiseError);

        ok(this.makeRequest.calledOnce, "makeRequest called.");

        //First time through we get an error.
        var newArgs = this.makeRequest.getCall(0).args;
        equal(newArgs.length, 1, "makeRequest called with 1 argument.");
        var newOptions = newArgs[0];
        equal(newOptions.copy, "me", "Make sure non-error options copied.");

        var xhr = {status: 38, responseText: "ignored"};
        var statusText = "sjdjakdjakda";
        var errorThrown = "4897589475893754983";
        var result = "rehsjfhdskjfhsdkhgtiu4y4";

        newOptions.error(xhr, statusText, errorThrown, result);

        ok(error.calledOnce, "Called Original Error");
        ok(error.getCall(0).calledWithExactly(xhr, statusText, errorThrown, result), "Called error with correct arguments.");

        ok(promiseError.calledOnce, "Called Original Error");
        ok(promiseError.getCall(0).calledWithExactly(xhr, statusText, errorThrown, result), "Called error with correct arguments.");
    });

    test("makeWebSudoRequest with 401 status code but not WebSudo failure.", function() {
        var error = this.sandbox.spy();
        var promiseError = this.sandbox.spy();

        //make the request.
        var promise = this.webSudo.makeWebSudoRequest({
            error: error,
            copy: "me"
        });

        promise.fail(promiseError);

        ok(this.makeRequest.calledOnce, "makeRequest called.");

        //First time through we get an error.
        var newArgs = this.makeRequest.getCall(0).args;
        equal(newArgs.length, 1, "makeRequest called with 1 argument.");
        var newOptions = newArgs[0];
        equal(newOptions.copy, "me", "Make sure non-error options copied.");

        var xhr = {status: 401, responseText: "ignored"};
        var statusText = "sjdjakdjakda";
        var errorThrown = "4897589475893754983";
        var result = "rehsjfhdskjfhsdkhgtiu4y4";

        newOptions.error(xhr, statusText, errorThrown, result);

        ok(error.calledOnce, "Called Original Error Again");
        ok(error.getCall(0).calledWithExactly(xhr, statusText, errorThrown, result), "Called error.");

        ok(promiseError.calledOnce, "Called Original Error Again");
        ok(promiseError.getCall(0).calledWithExactly(xhr, statusText, errorThrown, result), "Called error.");
    });

    test("makeWebSudoRequest with websudo dialog & success", function() {
        this.sandbox.stub(this.webSudo, "handleWebSudoError");

        var success = this.sandbox.spy();
        var promiseSuccess = this.sandbox.spy();
        var complete = this.sandbox.spy();
        var promiseError = this.sandbox.spy();
        var error = this.sandbox.spy();

        //make the request.
        var promise = this.webSudo.makeWebSudoRequest({
            error: error,
            success: success,
            complete: complete,
            copy: "me"
        });

        promise.fail(promiseError).done(promiseSuccess);

        ok(this.makeRequest.calledOnce, "makeRequest called.");

        //First time through we get an error.
        var newArgs = this.makeRequest.getCall(0).args;
        equal(newArgs.length, 1, "makeRequest called with 1 argument.");
        var newOptions = newArgs[0];
        equal(newOptions.copy, "me", "Make sure non-error options copied.");

        //Simulate a websudo error.
        var xhr = {status: 401, responseText: "websudo"};
        var statusText = "sjdjakdjakda";
        var errorThrown = "4897589475893754983";
        var result = "rehsjfhdskjfhsdkhgtiu4y4";

        //Error - then complete like regular XHR.
        newOptions.error(xhr, statusText, errorThrown, result);
        newOptions.complete();

        ok(!success.called, "Nothing should be called while websudo detected open.");
        ok(!promiseSuccess.called, "Nothing should be called while websudo detected open.");
        ok(!complete.called, "Nothing should be called while websudo detected open.");
        ok(!promiseError.called, "Nothing should be called while websudo detected open.");
        ok(!error.called, "Nothing should be called while websudo detected open.");
        ok(this.webSudo.handleWebSudoError.called, "Make sure we open the websudo dialog.");

        var ajaxOptions = this.webSudo.handleWebSudoError.args[0][0];

        var randomArgument = {};
        var randomArgument2 = {};
        var context = {};

        //Lets sumulate success to make sure the success handler works.
        ajaxOptions.success.call(context, randomArgument);
        ajaxOptions.complete.call(context, randomArgument2);

        ok(success.called, "Success should now be called.");
        ok(success.alwaysCalledWithExactly(randomArgument), "Called with correct arguments.");
        ok(success.alwaysCalledOn(context, randomArgument), "Called with correct context.");

        ok(promiseSuccess.called, "Promise success should now be called.");
        ok(promiseSuccess.alwaysCalledWithExactly(randomArgument), "Called with correct arguments.");
        ok(promiseSuccess.alwaysCalledOn(context, randomArgument), "Called with correct context.");

        ok(complete.called, "Complete should now be called.");
        ok(complete.alwaysCalledWithExactly(randomArgument2), "Called with correct arguments.");
        ok(complete.alwaysCalledOn(context, randomArgument2), "Called with correct context.");


        ok(!promiseError.called, "Error should never have been called.");
        ok(!error.called, "Error should never have been called.");
    });
});
