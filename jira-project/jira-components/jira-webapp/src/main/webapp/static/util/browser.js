define('jira/util/browser', [
    'jira/util/elements',
    'aui/params',
    'jquery',
    'exports'
], function(
    Elements,
    params,
    jQuery,
    exports
) {
    /**
     * @fileOverview this module is for checking, manipulating or affecting
     * the (behaviour of the) browser itself.
     *
     * This is not for checking the browser's name or version. You should be
     * using feature-detection for that stuff. Worst case, use the
     * 'jira/util/navigator' module for those kinds of things.
     */

    /**
     * Determines if you can access the contents of an iframe. This is only possible if the iframe's src has the same
     * domain, port and protocal as the parent window.
     *
     * @param {HTMLElement, jQuery} iframe
     * @return {Boolean}
     */
    exports.canAccessIframe = function canAccessIframe(iframe) {
        var $iframe = jQuery(iframe);

        return !/^(http|https):\/\//.test($iframe.attr("src")) ||
                (params.baseURL && (jQuery.trim($iframe.attr("src")).indexOf(params.baseURL) === 0));
    };

    function preventScrolling(e) {
        var keyCode = e.keyCode,
            keys = jQuery.ui.keyCode;

        // Don't bind to keypress. (JRA-25079)
        if (e.type == "keypress") return;

        // Check if an arrow key was pressed.
        if (keyCode === keys.DOWN || keyCode === keys.UP || keyCode === keys.LEFT || keyCode === keys.RIGHT) {
            var $el = jQuery(e.target);
            if (Elements.consumesKeyboardEvents($el)) {
                // do nothing
            } else {
                e.preventDefault();
            }
        }
    }

    exports.disableKeyboardScrolling = function disableKeyboardScrolling() {
        jQuery(document.body).bind("keydown", preventScrolling);
    };

    exports.enableKeyboardScrolling = function enableKeyboardScrolling() {
        jQuery(document.body).unbind("keydown", preventScrolling);
    };

    /**
     * Determine whether Selenium is running.
     *
     * Note: This does not detect WebDriver. Please use very sparingly!
     *
     * @return {boolean}
     */
    exports.isSelenium = function () {
        return window.name.toLowerCase().indexOf("selenium") >= 0;
    };

    /**
     * Selenium marks the page with magic markers like :
     *
     * var marker = 'selenium' + new Date().getTime();
     * window.location[marker] = true;
     *
     * So a window.reload() causes this all to go away and keeps Selenium happy
     * about knowing when a page has been loaded.
     *
     * However this is bad for humans in that if the original page is a POST, then
     * on IE and FF, they get a Confirm RE-POST dialog.  So we do this different on
     * Selenium than we do for humans.  Both are meant to be the same result.
     *
     * @param [url] if this is passed in then it will be used otherwise window.location.href will be used
     */
    exports.reloadViaWindowLocation = function reloadViaWindowLocation(url) {
        exports.reloadViaWindowLocation._delegate(url, window.location);
    };

    /**
     * Delegate for reloadViaWindowLocation. Here for testing as the window.location
     * object cannot easily be mocked out.
     *
     * @param url the url to redirect to is passed
     * @param location the window.location object.
     */
    exports.reloadViaWindowLocation._delegate = (function() {
        var secondsSinceMidnight = function()
        {
            var now = new Date();
            var midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
            var secs = (now.getTime() - midnight.getTime()) / 1000;
            return Math.max(Math.floor(secs), 1);
        };

        var MAGIC_PARAM = 'jwupdated';
        var MAGIC_PARAM_RE = /(jwupdated=[0-9]*)/;
        /*
         * If the url has a # on it then the browser wont ever leave the page.  So we want to insert
         * an updated parameter so that window.location will cause a page reload.  We put a unique value
         * into the url so that it is truly unique!
         */
        var makeHashUrlsUnique = function(url)
        {
            var hashIndex = url.indexOf('#');
            if (hashIndex == -1)
            {
                return url;
            }
            var firstQuestionMark = url.indexOf('?');
            var magicParamValue = MAGIC_PARAM + '=' + secondsSinceMidnight();
            if (firstQuestionMark == -1)
            {
                // if we have no parameters, we can insert them before the #
                url = url.replace('#', '?' + magicParamValue + '#');
            }
            else
            {
                // if we already have a magic marker then just replace that
                if (MAGIC_PARAM_RE.test(url))
                {
                    url = url.replace(MAGIC_PARAM_RE, magicParamValue);
                }
                else
                {
                    url = url.replace('?', '?' + magicParamValue + '&');
                }
            }
            return url;
        };

        return function(url, location) {
            var targetUrl = url || location.href;
            var uniqueUrl = makeHashUrlsUnique(targetUrl);

            if (targetUrl === location.href) {
                //If we are reloading the page then we don't what to record any history of the URL change
                //as it was just a cache buster.
                location.replace(uniqueUrl);
            } else {
                location.assign(uniqueUrl);
            }
        };
    })();

});
