define('jira/util/navigator', [
    'internal/util/navigator',
    'jquery'
], function(
    internalNavigator,
    jQuery
) {
    /**
     * @fileOverview Can retrieve names and version numbers for the user's
     * browser and platform / operating system. This module should be used
     * sparingly; you should always prefer feature-detection over checking
     * properties defined here.
     */

    var Navigator = jQuery.extend({}, internalNavigator);

    // Was missing from internal navigator at time of writing.
    Navigator.isOpera = function() {
        return jQuery.browser.opera === true;
    };

    return Navigator;
});
