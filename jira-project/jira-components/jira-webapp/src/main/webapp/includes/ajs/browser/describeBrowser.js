/**
 * Uses CSS classNames on the HTML tag to describe user agent. CSS developers use these classNames to apply
 * CSS style that work around browser incompatibilities
 */
define('jira/ajs/browser/describe-browser', [
    'jira/util/navigator',
    'jquery'
], function (
    Navigator,
    jQuery
) {

    function browserName() {
        if (Navigator.isIE()) return "msie";
        if (Navigator.isSafari()) return "webkit safari";
        if (Navigator.isChrome()) return "webkit chrome";
        if (Navigator.isMozilla()) return "mozilla";
        if (Navigator.isOpera()) return "opera";
        return "";
    }

    function describeBrowser() {
        var classNames = [];
        var browser = browserName();
        var version = Navigator.majorVersion();

        classNames.push(browser);

        if (Navigator.isIE()) {
            classNames.push(browser + "-" + version);

            while (version > 6) {
                --version;
                classNames.push(browser + "-gt-" + version);
            }
        }

        jQuery("html").addClass(classNames.join(" "));
    }

    return describeBrowser;

});

AJS.namespace('AJS.describeBrowser', null, require('jira/ajs/browser/describe-browser'));
