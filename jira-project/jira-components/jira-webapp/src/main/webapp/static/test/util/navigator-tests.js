AJS.test.require("jira.webresources:util-lite", function() {
    var Navigator = require('jira/util/navigator');

    module("Navigator#isIE", {
        setup: function() {
            this.sandbox = sinon.sandbox.create();
        },
        teardown: function() {
            this.sandbox.restore();
        }
    });

    test("has an API for checking the browser's family", function() {
        equal(typeof Navigator.isIE, "function", "Internet Explorer");
        equal(typeof Navigator.isWebkit, "function", "Webkit-based");
        equal(typeof Navigator.isSafari, "function", "Safari");
        equal(typeof Navigator.isChrome, "function", "Google Chrome");
        equal(typeof Navigator.isMozilla, "function", "Mozilla (Firefox)");
        equal(typeof Navigator.isOpera, "function", "Opera");
    });

    test("has an API for checking the browser's version", function() {
        equal(typeof Navigator.majorVersion, "function", "Can check browser's major version number");
        equal(typeof Navigator.majorVersion(), "number", "returns a number");
    });
});
