/**
 * Helper methods for showing Gravatar-related help text.
 */
;(function() {
    AJS.namespace('JIRA.GravatarUtil');
    JIRA.GravatarUtil.showGravatarHelp = function(data) {
        // response is in the form of  { entry: [] }
        if (typeof(data) !== 'undefined' && typeof(data.entry) !== 'undefined') {
            // hide the "sign up" text and show the "log in" text
            AJS.$('.gravatar-signup-text').addClass('hidden');
            AJS.$('.gravatar-login-text').removeClass('hidden');
        }
    };

    var displayGravatarHelp = function() {
        var gravatarJsonUrl = AJS.$('#gravatar_json_url');
        if (gravatarJsonUrl.length) {
            // use JSONP to determine whether the user has a Gravatar
            AJS.$.ajax(gravatarJsonUrl.val(), {
                dataType: 'jsonp',
                success: JIRA.GravatarUtil.showGravatarHelp
            });
        }
    };

    AJS.$(document).ready(function() {
        if (AJS.$('#gravatar_help_params')) {
            displayGravatarHelp();
        }
    });
}());
