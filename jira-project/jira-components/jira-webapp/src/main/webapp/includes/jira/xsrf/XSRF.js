/**
 * Helper methods to manipulate XSRF tokens on the page.
 *
 * It's possible for a user to recover from a session timeout, as long as
 * she has "Remember me" functionality enabled. If this happens the user may
 * be presented with a form to retry her request, which contains a new XSRF
 * token, but if the form id displayed in a dialog then the background page
 * will still contain references to the stale XSRF token.
 *
 * The functions in this file may be used to update any stale XSRF tokens on
 * the page.
 */
define('jira/xsrf', [
//    'aui',
    'jquery'
], function (
//    AJS,
    $
) {
    // the id of the meta element
    var META_TOKEN_ID = 'atlassian-token';

    // the name in query parameter
    var PARAM_TOKEN_NAME = 'atl_token';

    // the input name in forms
    var INPUT_TOKEN_NAME = 'atl_token';

    /**
     * Returns a string containing the given token formatted as a query param.
     *
     * @param token a string containing a query param name and value pair
     */
    var tokenQueryParam = function(token) {
        return AJS.format('{0}={1}', PARAM_TOKEN_NAME, token);
    };

    /**
     * Replaces the XSRF token in the page's meta element. This is where JS
     * code gets the token from, when necessary.
     *
     * @see atl_token()
     */
    var replaceTokenInMeta = function(oldToken, newToken) {
        var metaTokenSelector = AJS.format('meta#{0}', META_TOKEN_ID);
        $(metaTokenSelector).attr('content', newToken);
    };

    /**
     * Replaces the XSRF token that may be embedded in any link or a elements
     * on the page.
     */
    var replaceTokenInLinks = function(oldToken, newToken) {
        $('a,link').each(function() {
            var link = $(this);
            var href = link.attr('href');

            if (href) {
                link.attr('href', href.replace(tokenQueryParam(oldToken), tokenQueryParam(newToken)));
            }
        })
    };

    /**
     * Replaces the XSRF token in all forms on the page. The token may be
     * present as a query param in the form's action, and also as a hidden
     * form input element.
     */
    var replaceTokenInForms = function(oldToken, newToken) {
        $('form').each(function() {
            var $form = $(this);

            // replace token in the form's action
            var action = $form.attr('action');
            if (action) {
                // need uppercase ACTION for this to work in IE...
                $form.attr('action', action.replace(tokenQueryParam(oldToken), tokenQueryParam(newToken)));
            }

            // also replace hidden input elements within the form
            var formInputSelector = AJS.format('input[name="{0}"][value="{1}"]', INPUT_TOKEN_NAME, oldToken);
            $form.find(formInputSelector).each(function() {
                $(this).attr('value', newToken);
            })
        })
    };

    /**
     * Replaces the XSRF token everywhere on the page. The old token is
     * obtained from the page's <meta> element, and the new token is
     * obtained from the retry form.
     *
     * @param newToken the new, valid token
     * @see atl_token()
     */
     function updateTokenOnPage(newToken) {
        // at this point the old token is still in place
        var oldToken = atl_token();
        if (oldToken !== newToken) {
            replaceTokenInMeta(oldToken, newToken);
            replaceTokenInLinks(oldToken, newToken);
            replaceTokenInForms(oldToken, newToken);
        }
    }

    return {
        updateTokenOnPage: updateTokenOnPage
    }
});


/** Preserve legacy namespace
    @deprecated jira.xsrf */
AJS.namespace("jira.xsrf", null, require('jira/xsrf'));
AJS.namespace("JIRA.XSRF", null, require('jira/xsrf'));
