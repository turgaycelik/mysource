AJS.test.require("jira.webresources:jira-events", function() {
    var Events = require('jira/util/events');
    var jQuery = require('jquery');

    module("JIRA Events", {
        setup: function() {
            this.sandbox = sinon.sandbox.create();
        },
        teardown: function() {
            this.sandbox.restore();
        }
    });

    test("Can trigger events", function() {
        var callback = this.sandbox.spy();
        Events.bind("foo", callback);
        Events.trigger("foo", ["bar"]);

        equal(callback.callCount, 1);
        ok(jQuery.inArray(callback.getCall(0).args, "bar"));
    });

    test("Triggers jQuery events", function() {
        var callback = this.sandbox.spy();
        Events.bind("foo", callback);
        Events.trigger("foo");

        ok(callback.getCall(0).args[0] instanceof jQuery.Event);
    });

    test("Bind adds an event", function() {
        var handler = this.sandbox.spy();
        Events.bind("foo", handler);
        Events.trigger("foo");

        equal(handler.callCount, 1, "called when bound");
    });

    test("Unbind removes the event", function() {
        var handler = this.sandbox.spy();
        Events.bind("foo", handler);
        Events.trigger("foo");

        equal(handler.callCount, 1, "called when bound");

        Events.unbind("foo", handler);
        Events.trigger("foo");

        equal(handler.callCount, 1, "should not be called any more");
    });

    test("Binds events to the document", function() {
        var handler = this.sandbox.spy();
        jQuery(document).bind("foo", handler);
        Events.bind("foo", handler);
        Events.trigger("foo");

        equal(handler.callCount, 2);
    });

});
