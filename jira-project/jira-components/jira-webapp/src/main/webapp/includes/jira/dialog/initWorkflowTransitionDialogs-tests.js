AJS.test.require("jira.webresources:jira-global");

(function() {
    module("initDialog", {
        setup: function() {
            this.server = sinon.fakeServer.create();

            this.server.respondWith("GET", /.*WorkflowUIDispatcher.*/, [200, { "Content-Type": "text/html" }, "-"]);
        },

        teardown: function() {
            if (this.dialog) {
                this.dialog.hide();
            }

            this.server.restore();
        },

        testTransitionLink: function(linkHtml, elementToClickId, actionId) {
            AJS.$(linkHtml).appendTo(AJS.$("#qunit-fixture"));

            AJS.$("#" + elementToClickId).click();

            this.server.respond();

            this.dialog = JIRA.Dialogs["workflow-transition-" + actionId + "-dialog"];

            ok(this.dialog, "Transition dialog shown");
            equal(this.dialog.$activeTrigger.attr("id"), "action_id_" + actionId, "Link is the $activeTrigger of the transition dialog");
        }
    });

    test("Workflow transition link handler called when clicked on a transition link", function() {
        var linkHtml = "<a id='action_id_1' class='issueaction-workflow-transition' href='/secure/WorkflowUIDispatcher.jspa?id=1&action=1'>Test 1</a>";

        this.testTransitionLink(linkHtml, "action_id_1", "1");
    });

    test("Workflow transition link handler called when clicked on a span inside transition link", function() {
        var linkHtml = "<a id='action_id_2' class='issueaction-workflow-transition' href='/secure/WorkflowUIDispatcher.jspa?id=2&action=2'><span id='action_span_id_2' class='trigger-label'>Test 2</span></a>";

        this.testTransitionLink(linkHtml, "action_span_id_2", "2");
    });
})();