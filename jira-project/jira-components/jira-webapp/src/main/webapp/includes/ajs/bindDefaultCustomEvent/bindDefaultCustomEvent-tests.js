AJS.test.require("jira.webresources:jira-global");


(function () {
    module("bindDefaultCustomEvent", {
        setup: function () {
            AJS.unbindDefaultCustomEvent("serverError");
        }
    });

    test("event does bind and fire", function () {

        var serverErrorEventFired = false;

        AJS.bindDefaultCustomEvent("serverError", function () {
            serverErrorEventFired = true;
        });

        jQuery(document).trigger("serverError");

        ok(serverErrorEventFired, "Expected default event to fire");
    });


    test("default is prevented", function () {

        var serverErrorEventFired = false,
                otherHandlerFired = true;

        AJS.bindDefaultCustomEvent("serverError", function () {
            serverErrorEventFired = true;
        });

        jQuery(document).bind("serverError", function (e) {
            otherHandlerFired = true;
            e.preventDefault();
        });

        jQuery(document).trigger("serverError");

        ok(!serverErrorEventFired, "Expected default event NOT to fire");
        ok(otherHandlerFired, "Expected other event to fire");
    });


    test("default NOT prevented", function () {

        var serverErrorEventFired = false,
                otherHandlerFired = true;

        AJS.bindDefaultCustomEvent("serverError", function () {
            serverErrorEventFired = true;
        });

        jQuery(document).bind("serverError", function (e) {
            otherHandlerFired = true;
        });

        jQuery(document).trigger("serverError");

        ok(serverErrorEventFired, "Expected default event to fire");
        ok(otherHandlerFired, "Expected default event to fire");
    })
})();