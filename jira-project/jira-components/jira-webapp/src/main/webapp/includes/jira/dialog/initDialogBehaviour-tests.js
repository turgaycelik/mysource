AJS.test.require("jira.webresources:jira-global");

require([
    'jquery',
    'jira/dialog/dialog-register',
    'jira/dialog/form-dialog',
    'jira/dialog/init-dialog-behaviour',
    'jira/issue'
], function(
    $,
    DialogRegister,
    FormDialog,
    DialogBehaviour,
    Issue
) {
    module("initDialogBehaviour", {
        setup: function () {
            this.sandbox = sinon.sandbox.create();
            this.sandbox.stub(Issue, "getIssueId").returns(null);
        },
        teardown: function () {
            delete DialogRegister.testDialog;
            this.sandbox.restore();
        }
    });

    test("Non Issue Dialog will not prevent beforeShow event", function() {
        DialogRegister.testDialog = new FormDialog();
        DialogBehaviour.init();

        var beforeShowEvent = new $.Event("beforeShow");
        $(DialogRegister.testDialog).trigger(beforeShowEvent);

        ok(!beforeShowEvent.isDefaultPrevented(), "default should not have been prevented for non issue dialog");
    });

    test("Issue Dialog will prevent beforeShow event", function() {
        DialogRegister.testDialog = new FormDialog({isIssueDialog:true});
        DialogBehaviour.init();

        var beforeShowEvent = new $.Event("beforeShow");
        $(DialogRegister.testDialog).trigger(beforeShowEvent);

        ok(beforeShowEvent.isDefaultPrevented(), "default should have been prevented for issue dialog");
    });

    test("Issue Dialog will not prevent beforeShow event when there's a selected issue", function() {
        Issue.getIssueId.returns(10000);
        DialogRegister.testDialog = new FormDialog({isIssueDialog:true});
        DialogBehaviour.init();

        var beforeShowEvent = new $.Event("beforeShow");
        $(DialogRegister.testDialog).trigger(beforeShowEvent);

        ok(!beforeShowEvent.isDefaultPrevented(), "default should not have been prevented for issue dialog when there's an issue.");
    });
});