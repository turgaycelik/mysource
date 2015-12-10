/**
 * @fileOverview A module that wraps all the browser+os checks
 * you'd want to do via checking the window.navigator properties.
 *
 * TODO JDEV-28436: Code is borrowed (i.e., copy-pasted) from a Bower component until CAS-751 is resolved.
 *
 * @see {@literal Bower component: https://stash.atlassian.com/projects/FE/repos/navigator/browse/lib/internal/util/navigator.js}
 * @see {@literal Stash's implementation: https://stash.atlassian.com/projects/STASH/repos/stash/browse/webapp/default/src/main/webapp/static/util/navigator.js}
 */
define('internal/util/navigator', [
    'jquery',
    'underscore',
    'exports'
], function (
    $,
    _,
    exports
) {

    "use strict";

    // Avoid using this file at all costs,
    // Prefer using util/feature-detect

    var userAgent = window.navigator.userAgent;
    var platform = window.navigator.platform;

    function _isTrident (userAgent) {
        return (/\bTrident\b/).test(userAgent);
    }

    var isChrome = _.once(function() {
        return (/Chrome/).test(exports._getUserAgent());
    });

    /**
     * Is this browser IE?
     * @function
     */
    var isIE = _.once(function () {
        return _isTrident(exports._getUserAgent());
    });

    var isMozilla = _.once(function () {
        return $.browser.mozilla;
    });

    var isSafari = _.once(function () {
        return $.browser.safari && !isChrome();
    });

    var isWebkit = _.once(function () {
        return $.browser.webkit;
    });

    var majorVersion = _.once(function () {
        return parseInt($.browser.version, 10);
    });

    var isLinux = _.once(function () {
        return exports._getPlatform().indexOf('Linux') !== -1;
    });

    var isMac = _.once(function () {
        return exports._getPlatform().indexOf('Mac') !== -1;
    });

    var isWin = _.once(function () {
        return exports._getPlatform().indexOf('Win') !== -1;
    });

    exports.isChrome = isChrome;
    exports.isIE = isIE;
    exports.isMozilla = isMozilla;
    exports.isSafari = isSafari;
    exports.isWebkit = isWebkit;
    exports.majorVersion = majorVersion;

    exports.isLinux = isLinux;
    exports.isMac = isMac;
    exports.isWin = isWin;

    exports._isTrident = _isTrident; // Exposed for testing
    exports._getUserAgent = function() { return userAgent; };
    exports._getPlatform =  function() { return platform; };
});
