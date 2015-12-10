AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:jira-analytics");

(function($) {
    AJS.EventQueue = {};

    module("analytics", {
        setup: function() {
            this.pushSpy = sinon.spy();

            AJS.EventQueue = {
                push: this.pushSpy
            };
        },

        teardown: function() {
            AJS.EventQueue = null;
        },

        assertAfterClick: function(html, expectedName, expectedProperties) {
            var element = AJS.$(html);
            element.appendTo("#qunit-fixture");
            element.click();

            ok(this.pushSpy.calledOnce, "Event added only once");

            var event = this.pushSpy.args[0][0];

            equal(event.name, expectedName, "Check event name");

            if (expectedProperties) {
                _.each(expectedProperties, function(expectedValue, key) {
                    equal(event.properties[key], expectedValue, "Check property '" + key + "'");
                });
            }

            this.pushSpy.reset();
        }
    });

    test("View workflow on view issue page", function() {
        var isEnabled = sinon.stub().returns(false);

        AJS.DarkFeatures = {
            isEnabled: isEnabled
        };

        this.assertAfterClick("<a class='issueaction-viewworkflow' href='#'></a>",
                "issue.viewworkflow", { version: "old", newEnabled: false });

        this.assertAfterClick("<a class='issueaction-viewworkflow new-workflow-designer' href='#'></a>",
                "issue.viewworkflow", { version: "new", newEnabled: false });

        isEnabled.returns(true);

        this.assertAfterClick("<a class='issueaction-viewworkflow' href='#'></a>",
                "issue.viewworkflow", { version: "old", newEnabled: true });

        this.assertAfterClick("<a class='issueaction-viewworkflow new-workflow-designer' href='#'></a>",
                "issue.viewworkflow", { version: "new", newEnabled: true });
    });
})(AJS.$);