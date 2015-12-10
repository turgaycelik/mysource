AJS.test.require("jira.webresources:util-lite", function() {
    var Navigator, describeBrowser;
    var $html = jQuery("html");

    module("describeBrowser", {
        setup: function() {
            this.sandbox = sinon.sandbox.create();

            Navigator = require('jira/util/navigator');
            describeBrowser = require('jira/ajs/browser/describe-browser');

            this.sandbox.stub(Navigator, "isIE").returns(false);
            this.sandbox.stub(Navigator, "isMozilla").returns(false);
            this.sandbox.stub(Navigator, "isSafari").returns(false);
            this.sandbox.stub(Navigator, "isChrome").returns(false);
            this.sandbox.stub(Navigator, "isWebkit").returns(false);
            this.sandbox.stub(Navigator, "majorVersion");
            this.sandbox.stub(Navigator, "isOpera").returns(false);
        },
        teardown: function() {
            this.sandbox.restore();
        },
        assertIEVersionClasses: function(ieVersion) {
            ok($html.hasClass("msie"), "has class 'msie'");
            for(var i = 6; i < ieVersion; i++) {
                ok(!$html.hasClass("msie-" + i), "no class 'msie-" + i + "'");
            }
            ok($html.hasClass("msie-" + ieVersion), "has class 'msie-" + ieVersion + "'");
        },
        assertIEGreaterThanClasses : function(ieVersion) {
            for(var i = 6; i < ieVersion; i++) {
                ok($html.hasClass("msie-gt-" + i), "has class 'msie-gt-" + i + "'");
            }
            ok(!$html.hasClass("msie-gt-5"), "no class 'msie-gt-5'");
            ok(!$html.hasClass("msie-gt-" + ieVersion), "no class 'msie-gt-" + ieVersion + "'");
        }
    });

    QUnit.testStart = function () {
        $html.removeAttr("class");
    };

    test("Internet Explorer 12", function () {
        Navigator.isIE.returns(true);
        Navigator.majorVersion.returns(12);
        describeBrowser();

        this.assertIEVersionClasses(12);
        this.assertIEGreaterThanClasses(12);
    
        ok(!$html.hasClass("mozilla"), 'ie11 or greater not reported as mozilla');
    });

    test("Internet Explorer 11", function () {
        Navigator.isIE.returns(true);
        Navigator.majorVersion.returns(11);
        describeBrowser();
    
        this.assertIEVersionClasses(11);
        this.assertIEGreaterThanClasses(11);
    
        ok(!$html.hasClass("mozilla"), 'ie11 or greater not reported as mozilla');
    });

    test("Internet Explorer 10", function () {
        Navigator.isIE.returns(true);
        Navigator.majorVersion.returns(10);
        describeBrowser();

        this.assertIEVersionClasses(10);
        this.assertIEGreaterThanClasses(10);
    });

    test("Internet Explorer 9", function () {
        Navigator.isIE.returns(true);
        Navigator.majorVersion.returns(9);
        describeBrowser();

        this.assertIEVersionClasses(9);
        this.assertIEGreaterThanClasses(9);
    });

    test("Internet Explorer 8", function () {
        Navigator.isIE.returns(true);
        Navigator.majorVersion.returns(8);
        describeBrowser();

        this.assertIEVersionClasses(8);
        this.assertIEGreaterThanClasses(8);
    });

    test("Firefox", function () {
        Navigator.isMozilla.returns(true);
        describeBrowser();

        ok($html.hasClass("mozilla"));
        ok(!/-gt-/gi.test($html.attr("class")), "Expected no version greater than classes expected (IE only)");
    });

    test("Safari", function () {
        Navigator.isSafari.returns(true);
        Navigator.isWebkit.returns(true);
        describeBrowser();

        equal($html.hasClass("webkit"), true);
        equal($html.hasClass("safari"), true);
        equal($html.hasClass("chrome"), false);
        ok(!/-gt-/gi.test($html.attr("class")), "Expected no version greater than classes expected (IE only)");
    });

    test("Chrome", function () {
        Navigator.isChrome.returns(true);
        Navigator.isWebkit.returns(true);
        describeBrowser();

        equal($html.hasClass("webkit"), true);
        equal($html.hasClass("safari"), false);
        equal($html.hasClass("chrome"), true);
        ok(!/-gt-/gi.test($html.attr("class")), "Expected no version greater than classes expected (IE only)");
    });

    test("Opera", function () {
        Navigator.isOpera.returns(true);
        describeBrowser();

        ok($html.hasClass("opera"));
        ok(!/-gt-/gi.test($html.attr("class")), "Expected no version greater than classes expected (IE only)");
    });

    test("No browsers detected", function () {
        this.sandbox.restore();
        describeBrowser();

        ok(/opera|webkit|mozilla|msie/gi.test($html.attr("class")), "Expected to fall back to running browser string if not supplied");
    });
});
